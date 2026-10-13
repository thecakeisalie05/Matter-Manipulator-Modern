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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Bounded creative-only placement engine with two phases (states, then BE data).
 * Not a survival placement/rollback/permission implementation.
 */
public final class PasteScheduler {
    private static final int PER_TICK = 64;
    private static final Map<UUID, Task> QUEUE = new HashMap<>();

    private PasteScheduler() {}

    public static boolean queue(ServerPlayer player, ServerLevel level, BlockPos origin, ListTag cells) {
        if (cells.isEmpty() || cells.size() > 4096) {
            player.sendSystemMessage(Component.literal("Invalid snapshot."));
            return false;
        }
        for (int i = 0; i < cells.size(); i++) {
            CompoundTag cell = cells.getCompound(i);
            BlockPos pos = origin.offset(cell.getInt("x"), cell.getInt("y"), cell.getInt("z"));
            if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos) || !level.isLoaded(pos)) {
                player.sendSystemMessage(Component.literal("Paste cancelled: destination out of bounds or unloaded."));
                return false;
            }
        }
        QUEUE.put(player.getUUID(), new Task(level.dimension(), origin.immutable(), cells));
        level.playSound(null, origin, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.0F);
        return true;
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (QUEUE.isEmpty()) return;
        Iterator<Map.Entry<UUID, Task>> iterator = QUEUE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Task> pair = iterator.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(pair.getKey());
            Task task = pair.getValue();
            ServerLevel level = event.getServer().getLevel(task.dimension);
            if (player == null || level == null || !player.isCreative() || !player.hasPermissions(2)) {
                iterator.remove();
                continue;
            }
            int processed = 0;
            try {
                while (processed < PER_TICK && task.index < task.cells.size()) {
                    CompoundTag cell = task.cells.getCompound(task.index);
                    BlockPos target = task.origin.offset(cell.getInt("x"), cell.getInt("y"), cell.getInt("z"));
                    if (!level.isLoaded(target)) throw new IllegalStateException("Destination chunk unloaded");
                    if (task.phase == 0) {
                        BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), cell.getCompound("state"));
                        level.setBlock(target, state, 3);
                    } else if (cell.contains("block_entity")) {
                        BlockEntity be = level.getBlockEntity(target);
                        if (be != null) {
                            CompoundTag data = cell.getCompound("block_entity").copy();
                            data.putInt("x", target.getX());
                            data.putInt("y", target.getY());
                            data.putInt("z", target.getZ());
                            be.loadWithComponents(data, level.registryAccess());
                            be.setChanged();
                        }
                    }
                    task.index++;
                    processed++;
                }
                if (task.index == task.cells.size()) {
                    if (task.phase == 0) {
                        task.phase = 1;
                        task.index = 0;
                    } else {
                        player.sendSystemMessage(Component.literal("Creative paste finished (" + task.cells.size() + " blocks). Verify machine, network, and security data."));
                        level.playSound(null, task.origin, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.2F);
                        iterator.remove();
                    }
                }
            } catch (Exception ex) {
                player.sendSystemMessage(Component.literal("Paste halted: " + ex.getMessage() + ". Partial changes may remain."));
                iterator.remove();
            }
        }
    }

    private static final class Task {
        private final ResourceKey<Level> dimension;
        private final BlockPos origin;
        private final ListTag cells;
        private int index;
        private int phase;
        private Task(ResourceKey<Level> dimension, BlockPos origin, ListTag cells) {
            this.dimension = dimension;
            this.origin = origin;
            this.cells = cells;
        }
    }
}
