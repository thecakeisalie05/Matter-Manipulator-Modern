package dev.thecakeisalie05.mattermanipulatormodern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Creative/operator-only time-sliced paste. Preflights all placements, saves a rollback
 * snapshot, writes block states and then BE data. No survival material accounting yet.
 */
public final class PasteScheduler {
    private static final int PER_TICK = 64;
    private static final Map<UUID, Task> QUEUE = new HashMap<>();

    private PasteScheduler() {}

    public static boolean queue(ServerPlayer player, ServerLevel level, BlockPos origin, CompoundTag data) {
        if (!MatterManipulatorMk3Item.authorized(player)) return false;
        if (QUEUE.containsKey(player.getUUID())) {
            tell(player, "Finish your previous paste before starting another.");
            return false;
        }
        ListTag source = data.getList("snapshot", 10);
        int w = data.getInt("width"), h = data.getInt("height"), d = data.getInt("depth");
        if (w < 1 || h < 1 || d < 1 || (long) w * h * d != source.size()
            || source.size() > MatterManipulatorMk3Item.MAX_CAPTURE_BLOCKS) {
            tell(player, "Blueprint is corrupt or unsupported.");
            return false;
        }

        int quarterTurns = Math.floorMod(data.getInt("turns"), 4);
        boolean mirror = data.getBoolean("mirror"), overwrite = data.getBoolean("overwrite");
        boolean pasteAir = data.getBoolean("paste_air");
        List<PlannedCell> plan = new ArrayList<>(source.size());
        List<OldCell> rollback = new ArrayList<>(source.size());

        try {
            for (int i = 0; i < source.size(); i++) {
                CompoundTag cell = source.getCompound(i);
                int x = cell.getInt("x"), y = cell.getInt("y"), z = cell.getInt("z");
                if (y < 0 || y >= h) throw new IllegalArgumentException("Invalid blueprint coordinate");
                int[] t = TransformMath.map(x, y, z, w, d, mirror, quarterTurns);
                BlockPos target = origin.offset(t[0], t[1], t[2]);
                if (!level.isInWorldBounds(target) || !level.getWorldBorder().isWithinBounds(target)
                    || !level.isLoaded(target)) {
                    throw new IllegalArgumentException("Destination out of bounds or not loaded: " + target.toShortString());
                }

                CompoundTag stateTag = cell.getCompound("state");
                ResourceLocation stateId = ResourceLocation.tryParse(stateTag.getString("Name"));
                if (stateId == null || !BuiltInRegistries.BLOCK.containsKey(stateId)) {
                    throw new IllegalArgumentException("Missing source block: " + stateTag.getString("Name"));
                }
                BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), stateTag);
                if (mirror) state = state.mirror(Mirror.FRONT_BACK);
                Rotation r = switch (quarterTurns) {
                    case 1 -> Rotation.CLOCKWISE_90;
                    case 2 -> Rotation.CLOCKWISE_180;
                    case 3 -> Rotation.COUNTERCLOCKWISE_90;
                    default -> Rotation.NONE;
                };
                state = state.rotate(r);

                boolean hasEntity = cell.contains("block_entity", 10);
                if (hasEntity && (mirror || quarterTurns != 0)) {
                    throw new IllegalArgumentException("Rotation/mirroring of machines is not yet safe: reset transform with /mmm rotate none and /mmm mirror off.");
                }
                if (state.isAir() && !pasteAir) continue;

                BlockState existing = level.getBlockState(target);
                if (!overwrite && !existing.isAir()) {
                    throw new IllegalArgumentException("Target occupied: " + target.toShortString() + ". /mmm overwrite on is destructive.");
                }

                CompoundTag entityData = hasEntity ? cell.getCompound("block_entity").copy() : null;
                BlockEntity oldBE = level.getBlockEntity(target);
                CompoundTag oldNBT = oldBE == null ? null : oldBE.saveWithFullMetadata(level.registryAccess());
                plan.add(new PlannedCell(target.immutable(), state, entityData));
                rollback.add(new OldCell(target.immutable(), existing, oldNBT));
            }
        } catch (Exception ex) {
            tell(player, "Paste not started: " + ex.getMessage());
            return false;
        }

        if (plan.isEmpty()) {
            tell(player, "Nothing to place (check air and overwrite settings).");
            return false;
        }

        Task job = new Task(level.dimension(), origin.immutable(), plan, rollback);
        QUEUE.put(player.getUUID(), job);
        level.playSound(null, origin, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 1f);
        tell(player, "Placing " + plan.size() + " cells. Paste is server queued and can roll back on failure.");
        return true;
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (QUEUE.isEmpty()) return;
        Iterator<Map.Entry<UUID, Task>> iterator = QUEUE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Task> entry = iterator.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            Task job = entry.getValue();
            ServerLevel level = event.getServer().getLevel(job.dimension);
            if (level == null) {
                iterator.remove();
                continue;
            }
            if (player == null || !MatterManipulatorMk3Item.authorized(player)) {
                restore(level, job);
                iterator.remove();
                continue;
            }
            try {
                int placed = 0;
                while (placed < PER_TICK && job.index < job.plan.size()) {
                    PlannedCell cell = job.plan.get(job.index);
                    if (!level.isLoaded(cell.pos)) throw new IllegalStateException("Destination chunk unloaded");
                    if (job.phase == 0) {
                        if (!level.setBlock(cell.pos, cell.state, 3)) {
                            throw new IllegalStateException("Block placement rejected at " + cell.pos.toShortString());
                        }
                    } else if (cell.nbt != null) {
                        BlockEntity be = level.getBlockEntity(cell.pos);
                        if (be == null) throw new IllegalStateException("Block entity unavailable at " + cell.pos.toShortString());
                        CompoundTag nbt = cell.nbt.copy();
                        nbt.putInt("x", cell.pos.getX());
                        nbt.putInt("y", cell.pos.getY());
                        nbt.putInt("z", cell.pos.getZ());
                        be.loadWithComponents(nbt, level.registryAccess());
                        be.setChanged();
                        level.sendBlockUpdated(cell.pos, cell.state, cell.state, 3);
                    }
                    job.index++;
                    placed++;
                }
                if (job.index >= job.plan.size()) {
                    if (job.phase == 0) {
                        job.index = 0;
                        job.phase = 1;
                    } else {
                        tell(player, "Paste finished: " + job.plan.size() + " cells. Verify machine and ME configurations; raw mod network IDs are not remapped.");
                        level.playSound(null, job.origin, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.2f);
                        iterator.remove();
                    }
                }
            } catch (Exception ex) {
                restore(level, job);
                tell(player, "Paste failed; attempted rollback: " + ex.getMessage());
                iterator.remove();
            }
        }
    }

    private static void restore(ServerLevel level, Task job) {
        // Best effort: restored BEs depend on third-party block hooks; don't claim atomicity.
        for (OldCell old : job.rollback) {
            try {
                if (level.isLoaded(old.pos)) level.setBlock(old.pos, old.state, 3);
            } catch (Exception ignored) { }
        }
        for (OldCell old : job.rollback) {
            if (old.nbt == null) continue;
            try {
                BlockEntity be = level.getBlockEntity(old.pos);
                if (be != null) {
                    CompoundTag nbt = old.nbt.copy();
                    be.loadWithComponents(nbt, level.registryAccess());
                    be.setChanged();
                }
            } catch (Exception ignored) { }
        }
    }

    private static void tell(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal("[MMM] " + text));
    }

    private record PlannedCell(BlockPos pos, BlockState state, CompoundTag nbt) {}
    private record OldCell(BlockPos pos, BlockState state, CompoundTag nbt) {}

    private static final class Task {
        final ResourceKey<Level> dimension;
        final BlockPos origin;
        final List<PlannedCell> plan;
        final List<OldCell> rollback;
        int index;
        int phase;

        Task(ResourceKey<Level> dimension, BlockPos origin, List<PlannedCell> plan, List<OldCell> rollback) {
            this.dimension = dimension;
            this.origin = origin;
            this.plan = plan;
            this.rollback = rollback;
        }
    }
}
