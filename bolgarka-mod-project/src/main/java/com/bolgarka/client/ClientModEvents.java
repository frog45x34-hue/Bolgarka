package com.bolgarka.client;

import com.bolgarka.AngleGrinderItem;
import com.bolgarka.BolgarkaMod;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Чтобы внешний вид болгарки менялся в зависимости от установленного диска. */
@EventBusSubscriber(modid = BolgarkaMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                BolgarkaMod.ANGLE_GRINDER.get(),
                ResourceLocation.fromNamespaceAndPath(BolgarkaMod.MODID, "disc"),
                // значение зажимается в диапазон 0..1, поэтому variant / 10
                (stack, level, entity, seed) -> AngleGrinderItem.getDisc(stack).variant() / 10.0F));
    }
}
