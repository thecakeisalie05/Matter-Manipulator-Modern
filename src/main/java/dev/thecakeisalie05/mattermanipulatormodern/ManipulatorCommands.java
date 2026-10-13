package dev.thecakeisalie05.mattermanipulatormodern;

import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Operator tools for release 0.1; UI and normal survival gameplay will follow later. */
public final class ManipulatorCommands {
    private ManipulatorCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("mmm").requires(src -> src.hasPermission(2))
                .then(Commands.literal("status").executes(ctx -> execute(ctx.getSource().getPlayerOrException(), "status")))
                .then(Commands.literal("reset").executes(ctx -> execute(ctx.getSource().getPlayerOrException(), "reset")))
                .then(Commands.literal("rotate")
                    .then(Commands.literal("cw").executes(ctx -> execute(ctx.getSource().getPlayerOrException(), "cw")))
                    .then(Commands.literal("ccw").executes(ctx -> execute(ctx.getSource().getPlayerOrException(), "ccw")))
                    .then(Commands.literal("none").executes(ctx -> execute(ctx.getSource().getPlayerOrException(), "norotation"))))
                .then(Commands.literal("mirror")
                    .then(Commands.literal("on").executes(ctx -> execute(ctx.getSource().getPlayerOrException(), "mirroron")))
                    .then(Commands.literal("off").executes(ctx -> execute(ctx.getSource().getPlayerOrException(), "mirroroff"))))
                .then(Commands.literal("overwrite")
                    .then(Commands.literal("on").executes(ctx -> execute(ctx.getSource().getPlayerOrException(), "overwriteon")))
                    .then(Commands.literal("off").executes(ctx -> execute(ctx.getSource().getPlayerOrException(), "overwriteoff"))))
                .then(Commands.literal("air")
                    .then(Commands.literal("on").executes(ctx -> execute(ctx.getSource().getPlayerOrException(), "airon")))
                    .then(Commands.literal("off").executes(ctx -> execute(ctx.getSource().getPlayerOrException(), "airoff"))))
        );
    }

    private static int execute(ServerPlayer player, String action) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof MatterManipulatorMk3Item)) stack = player.getOffhandItem();
        if (!(stack.getItem() instanceof MatterManipulatorMk3Item)) {
            say(player, "Hold the Matter Manipulator Mk III in either hand.");
            return 0;
        }
        if (!MatterManipulatorMk3Item.authorized(player)) {
            say(player, "Requires Creative mode and operator permissions.");
            return 0;
        }
        CompoundTag data = MatterManipulatorMk3Item.data(stack);
        switch (action) {
            case "reset" -> {
                data.remove("snapshot");
                data.remove("corner");
                data.remove("corner_dimension");
                data.remove("schema");
                data.remove("width");
                data.remove("height");
                data.remove("depth");
                say(player, "Capture and selections reset.");
            }
            case "cw" -> data.putInt("turns", Math.floorMod(data.getInt("turns") + 1, 4));
            case "ccw" -> data.putInt("turns", Math.floorMod(data.getInt("turns") - 1, 4));
            case "norotation" -> data.putInt("turns", 0);
            case "mirroron" -> data.putBoolean("mirror", true);
            case "mirroroff" -> data.putBoolean("mirror", false);
            case "overwriteon" -> data.putBoolean("overwrite", true);
            case "overwriteoff" -> data.putBoolean("overwrite", false);
            case "airon" -> data.putBoolean("paste_air", true);
            case "airoff" -> data.putBoolean("paste_air", false);
            case "status" -> { /* fall through to status output */ }
        }
        MatterManipulatorMk3Item.write(stack, data);
        int w = data.getInt("width"), h = data.getInt("height"), d = data.getInt("depth");
        boolean ready = data.contains("snapshot");
        say(player, "Blueprint: " + (ready ? (w + "x" + h + "x" + d) : "none")
            + "; rotation=" + (Math.floorMod(data.getInt("turns"), 4) * 90) + " deg"
            + "; mirror=" + data.getBoolean("mirror")
            + "; overwrite=" + data.getBoolean("overwrite")
            + "; copy-air=" + data.getBoolean("paste_air"));
        return Command.SINGLE_SUCCESS;
    }

    private static void say(ServerPlayer player, String msg) {
        player.sendSystemMessage(Component.literal("[MMM] " + msg));
    }
}
