package com.bolgarka;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, BolgarkaMod.MODID);

    /** Какой диск сейчас стоит на болгарке (id предмета-диска). Если пусто - железный. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> DISC =
            COMPONENTS.register("disc", () -> DataComponentType.<ResourceLocation>builder()
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC)
                    .build());
}
