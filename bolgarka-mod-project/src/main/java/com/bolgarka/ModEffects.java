package com.bolgarka;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, BolgarkaMod.MODID);

    public static final DeferredHolder<MobEffect, BleedingEffect> BLEEDING = EFFECTS.register("bleeding", BleedingEffect::new);
}
