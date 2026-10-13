package dev.thecakeisalie05.mattermanipulatormodern;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * v0.1 alpha: creative-operator only. Captures vanilla blockstate + serialized block entity data.
 * Never use this as a survival resource copier. Data cannot be assumed portable between mods.
 */
public final class MatterManipulatorMk3Item extends Item {
    public static final int MAX_CAPTURE_BLOCKS = 8192;
    private static final int MAX_SERIALIZED_CHARACTERS = 750_000;

    public MatterManipulatorMk3Item(Properties properties) {
        super(properties);
    }

    public static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static void write(ItemStack stack, CompoundTag data) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    public static boolean authorized(ServerPlayer player) {
        return player.isCreative() && player.hasPermissions(2);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        if (!(context.getPlayer() instanceof ServerPlayer player)) return InteractionResult.PASS;
        if (!authorized(player)) {
            player.sendSystemMessage(Component.literal("Matter Manipulator alpha requires Creative mode and operator permissions."));
            return InteractionResult.FAIL;
        }
        ItemStack stack = context.getItemInHand();
        CompoundTag root = data(stack);
        BlockPos point = context.getClickedPos();

        if (player.isShiftKeyDown()) {
            if (!root.contains("corner")) {
                root.put("corner", NbtUtils.writeBlockPos(point));
                root.putString("corner_dimension", level.dimension().location().toString());
                write(stack, root);
                tell(player, "Corner A: " + point.toShortString() + ". Sneak-right-click corner B to capture.");
                return InteractionResult.SUCCESS;
            }
            String savedDimension = root.getString("corner_dimension");
            if (!savedDimension.equals(level.dimension().location().toString())) {
                tell(player, "Capture cancelled: the two corners must be in the same dimension.");
                root.remove("corner");
                root.remove("corner_dimension");
                write(stack, root);
                return InteractionResult.FAIL;
            }
            BlockPos first = NbtUtils.readBlockPos(root, "corner").orElse(point);
            root.remove("corner");
            root.remove("corner_dimension");
            boolean ok = capture(level, first, point, root, player);
            write(stack, root);
            return ok ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }

        if (!root.contains("snapshot") || root.getInt("schema") != 2) {
            tell(player, "No current blueprint. Sneak-right-click two opposite corners of a build.");
            return InteractionResult.FAIL;
        }
        BlockPos destination = point.relative(context.getClickedFace());
        if (PasteScheduler.queue(player, level, destination, root)) {
            tell(player, "Queued paste at " + destination.toShortString() + ". Use /mmm status for settings.");
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    private static boolean capture(ServerLevel level, BlockPos a, BlockPos b, CompoundTag root, ServerPlayer player) {
        BlockPos min = new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
        BlockPos max = new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
        int w = max.getX() - min.getX() + 1, h = max.getY() - min.getY() + 1, d = max.getZ() - min.getZ() + 1;
        long count = (long) w * h * d;
        if (count < 1 || count > MAX_CAPTURE_BLOCKS) {
            tell(player, "Capture volume " + count + " is above the alpha limit of " + MAX_CAPTURE_BLOCKS + " blocks.");
            return false;
        }
        ListTag cells = new ListTag();
        for (BlockPos position : BlockPos.betweenClosed(min, max)) {
            if (!level.isLoaded(position)) {
                tell(player, "Capture aborted: the complete source must be loaded.");
                return false;
            }
            CompoundTag cell = new CompoundTag();
            cell.putInt("x", position.getX() - min.getX());
            cell.putInt("y", position.getY() - min.getY());
            cell.putInt("z", position.getZ() - min.getZ());
            cell.put("state", NbtUtils.writeBlockState(level.getBlockState(position)));
            BlockEntity blockEntity = level.getBlockEntity(position);
            if (blockEntity != null) {
                try {
                    cell.put("block_entity", blockEntity.saveWithFullMetadata(level.registryAccess()));
                } catch (Exception ex) {
                    tell(player, "Capture aborted: failed to serialize block entity at " + position.toShortString());
                    return false;
                }
            }
            cells.add(cell);
        }
        // This prototype stores the blueprint in ItemStack custom data; keep it below packet limits.
        if (cells.toString().length() > MAX_SERIALIZED_CHARACTERS) {
            tell(player, "Blueprint too data-heavy for the alpha's item-backed storage.");
            return false;
        }
        root.put("snapshot", cells);
        root.putInt("width", w);
        root.putInt("height", h);
        root.putInt("depth", d);
        root.putInt("schema", 2);
        root.putInt("turns", 0);
        root.putBoolean("mirror", false);
        tell(player, "Captured " + count + " blocks (" + w + " x " + h + " x " + d + "), including serializable machine data.");
        return true;
    }

    private static void tell(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal("[MMM] " + message));
    }
}
