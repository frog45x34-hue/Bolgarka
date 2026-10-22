package com.bolgarka;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.joml.Vector3f;

/** Кровотечение: урон каждые полсекунды, игнорирует броню. Чем выше уровень, тем больнее. */
public class BleedingEffect extends MobEffect {
    public BleedingEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel level) {
            entity.hurt(level.damageSources().magic(), 1.0F + amplifier * 0.5F);
            // чтобы кровотечение не блокировало удары болгарки
            entity.invulnerableTime = 0;
            level.sendParticles(new DustParticleOptions(new Vector3f(0.7F, 0.05F, 0.05F), 1.0F),
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                    5, 0.15, 0.2, 0.15, 0.01);
            // от кровопотери конечности тоже продолжают отваливаться
            if (entity instanceof Mob) {
                AngleGrinderItem.updateCutStage(level, entity);
            }
        }
        return true;
    }
}
