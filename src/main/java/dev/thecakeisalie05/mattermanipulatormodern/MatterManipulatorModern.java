package dev.thecakeisalie05.mattermanipulatormodern;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(MatterManipulatorModern.MODID)
public final class MatterManipulatorModern {
    public static final String MODID = "matter_manipulator_modern";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredItem<MatterManipulatorMk3Item> MK3 = ITEMS.register(
        "matter_manipulator_mk3", () -> new MatterManipulatorMk3Item(
            new net.minecraft.world.item.Item.Properties().stacksTo(1)
        )
    );

    public MatterManipulatorModern(IEventBus bus) {
        ITEMS.register(bus);
        NeoForge.EVENT_BUS.addListener(PasteScheduler::onServerTick);
        NeoForge.EVENT_BUS.addListener(ManipulatorCommands::register);
    }
}
