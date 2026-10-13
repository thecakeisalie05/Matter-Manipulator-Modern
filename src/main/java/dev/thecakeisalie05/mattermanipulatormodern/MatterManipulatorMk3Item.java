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
 * Creative/operator-only proof of concept. NOT the finished survival Mk III.
 * Holds a bounded block-state + block-entity snapshot in ItemStack custom data.
 */
public final class MatterManipulatorMk3Item extends Item {
    private static final int MAX_CAPTURE_BLOCKS = 4096;
    private static final int MAX_CAPTURE_SERIALIZED_CHARACTERS = 750_000;

    public MatterManipulatorMk3Item(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        if (!(context.getPlayer() instanceof ServerPlayer player)) return InteractionResult.PASS;

        if (!player.isCreative() || !player.hasPermissions(2)) {
            player.sendSystemMessage(Component.literal("Pre-alpha: manipulator requires creative mode and operator permission."));
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (player.isShiftKeyDown()) {
            if (!root.contains("origin_x")) {
                root.putInt("origin_x", pos.getX());
                root.putInt("origin_y", pos.getY());
                root.putInt("origin_z", pos.getZ());
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
                player.sendSystemMessage(Component.literal("Corner A marked: " + pos.toShortString()
                    + ". Sneak-right-click corner B to capture."));
            } else {
                BlockPos start = new BlockPos(root.getInt("origin_x"), root.getInt("origin_y"), root.getInt("origin_z"));
                root.remove("origin_x");
                root.remove("origin_y");
                root.remove("origin_z");
                if (capture(level, start, pos, root, player)) {
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
                } else {
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (!root.contains("snapshot")) {
            player.sendSystemMessage(Component.literal("No snapshot. Sneak-right-click two opposite corners first."));
            return InteractionResult.FAIL;
        }

        BlockPos destination = pos.relative(context.getClickedFace());
        ListTag snapshot = root.getList("snapshot", 10);
        if (PasteScheduler.queue(player, level, destination, snapshot.copy())) {
            player.sendSystemMessage(Component.literal("Queued " + snapshot.size() + " blocks for placement at "
                + destination.toShortString() + ". Creative prototype: inspect machines after pasting."));
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean capture(ServerLevel level, BlockPos a, BlockPos b, CompoundTag root, ServerPlayer player) {
        BlockPos min = new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
        BlockPos max = new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
        long count = ((long) max.getX() - min.getX() + 1) *
            ((long) max.getY() - min.getY() + 1) * ((long) max.getZ() - min.getZ() + 1);
        if (count > MAX_CAPTURE_BLOCKS || count < 1) {
            player.sendSystemMessage(Component.literal("Capture limited to " + MAX_CAPTURE_BLOCKS + " blocks for now."));
            return false;
        }
        ListTag cells = new ListTag();
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (!level.isLoaded(pos)) {
                player.sendSystemMessage(Component.literal("Aborted: source contains unloaded chunks."));
                return false;
            }
            CompoundTag cell = new CompoundTag();
            cell.putInt("x", pos.getX() - min.getX());
            cell.putInt("y", pos.getY() - min.getY());
            cell.putInt("z", pos.getZ() - min.getZ());
            cell.put("state", NbtUtils.writeBlockState(level.getBlockState(pos)));
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null) {
                try {
                    cell.put("block_entity", be.saveWithFullMetadata(level.registryAccess()));
                } catch (Exception error) {
                    player.sendSystemMessage(Component.literal("Capture aborted: block entity could not be serialized at " + pos.toShortString()));
                    return false;
                }
            }
            cells.add(cell);
        }
        // Bound serialized data to reduce excessive packet/item metadata sizes.
        if (cells.toString().length() > MAX_CAPTURE_SERIALIZED_CHARACTERS) {
            player.sendSystemMessage(Component.literal("Capture too large: snapshot data limit exceeded."));
            return false;
        }
        root.put("snapshot", cells);
        root.putInt("schema", 1);
        player.sendSystemMessage(Component.literal("Captured " + count + " blocks, including serializable block-entity data."));
        return true;
    }
}
