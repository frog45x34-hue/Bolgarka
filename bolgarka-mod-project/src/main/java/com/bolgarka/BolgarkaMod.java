package com.bolgarka;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(BolgarkaMod.MODID)
public class BolgarkaMod {
    public static final String MODID = "bolgarka";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<Item> ANGLE_GRINDER = ITEMS.registerItem(
            "angle_grinder",
            AngleGrinderItem::new,
            new Item.Properties()
                    .durability(1500)
                    .rarity(Rarity.RARE)
                    .attributes(AngleGrinderItem.createAttributes(1.0)));

    // ---- Диски -----------------------------------------------------------
    //                                                              урон  износ кровь огонь вампир опыт
    public static final DeferredItem<DiscItem> IRON_DISC = disc("iron_disc", 0, new DiscStats(1.0, 1, 0, 0, 0.0F, 0), false);
    public static final DeferredItem<DiscItem> GOLDEN_DISC = disc("golden_disc", 1, new DiscStats(0.0, 2, 0, 0, 0.0F, 8), false);
    public static final DeferredItem<DiscItem> DIAMOND_DISC = disc("diamond_disc", 2, new DiscStats(3.0, 1, 0, 0, 0.0F, 0), false);
    public static final DeferredItem<DiscItem> SAW_DISC = disc("saw_disc", 3, new DiscStats(1.5, 2, 120, 0, 0.0F, 0), false);
    public static final DeferredItem<DiscItem> FIRE_DISC = disc("fire_disc", 4, new DiscStats(1.0, 1, 0, 5, 0.0F, 0), false);
    public static final DeferredItem<DiscItem> VAMPIRE_DISC = disc("vampire_disc", 5, new DiscStats(1.5, 2, 0, 0, 0.3F, 0), false);
    public static final DeferredItem<DiscItem> NETHERITE_DISC = disc("netherite_disc", 6, new DiscStats(5.0, 1, 80, 0, 0.0F, 0), true);

    private static DeferredItem<DiscItem> disc(String name, int variant, DiscStats stats, boolean fireproof) {
        Item.Properties props = new Item.Properties().stacksTo(16);
        if (fireproof) {
            props = props.fireResistant();
        }
        return ITEMS.registerItem(name, p -> new DiscItem(p, variant, stats), props);
    }

    public BolgarkaMod(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModComponents.COMPONENTS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ANGLE_GRINDER);
            event.accept(IRON_DISC);
            event.accept(GOLDEN_DISC);
            event.accept(DIAMOND_DISC);
            event.accept(SAW_DISC);
            event.accept(FIRE_DISC);
            event.accept(VAMPIRE_DISC);
            event.accept(NETHERITE_DISC);
        }
    }
}
