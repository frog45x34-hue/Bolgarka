package com.bolgarka;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class AngleGrinderItem extends Item {

    /** Скорость атаки: база игрока 4.0 + 6.0 = 10 ударов в секунду (КД почти нет). */
    private static final double SPEED_BONUS = 6.0;
    /** Максимальный уровень кровотечения (0 = I, 2 = III). */
    private static final int MAX_BLEED_LEVEL = 2;

    public AngleGrinderItem(Properties properties) {
        super(properties);
    }

    public static ItemAttributeModifiers createAttributes(double damageBonus) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damageBonus, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, SPEED_BONUS, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    /** Какой диск сейчас стоит на болгарке. Если ничего не записано - железный. */
    public static DiscItem getDisc(ItemStack grinder) {
        ResourceLocation id = grinder.get(ModComponents.DISC.get());
        if (id != null && BuiltInRegistries.ITEM.get(id) instanceof DiscItem disc) {
            return disc;
        }
        return BolgarkaMod.IRON_DISC.get();
    }

    // ---------------------------------------------------------------------
    // Смена диска: болгарка в основной руке, диск в левой руке, ПКМ
    // ---------------------------------------------------------------------
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack grinder = player.getItemInHand(hand);
        ItemStack offhand = player.getOffhandItem();

        if (hand == InteractionHand.MAIN_HAND && offhand.getItem() instanceof DiscItem newDisc) {
            if (!level.isClientSide) {
                ItemStack oldDisc = new ItemStack(getDisc(grinder));

                grinder.set(ModComponents.DISC.get(), BuiltInRegistries.ITEM.getKey(newDisc));
                grinder.set(DataComponents.ATTRIBUTE_MODIFIERS, createAttributes(newDisc.stats().damage()));
                offhand.shrink(1);

                if (!player.getInventory().add(oldDisc)) {
                    player.drop(oldDisc, false);
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.5F, 1.8F);
            }
            return InteractionResultHolder.sidedSuccess(grinder, level.isClientSide());
        }
        return InteractionResultHolder.pass(grinder);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.bolgarka.current_disc", getDisc(stack).getDescription())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.bolgarka.grinder_hint").withStyle(ChatFormatting.DARK_GRAY));
    }

    // ---------------------------------------------------------------------
    // Удар
    // ---------------------------------------------------------------------
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = attacker.level();
        if (level.isClientSide) {
            return true;
        }

        DiscItem disc = getDisc(stack);
        DiscStats stats = disc.stats();

        // Болгарка пилит без "кадров неуязвимости" и без отбрасывания,
        // чтобы моб не улетал из-под диска.
        target.invulnerableTime = 0;
        Vec3 motion = target.getDeltaMovement();
        target.setDeltaMovement(0.0, Math.min(motion.y, 0.0), 0.0);
        target.hurtMarked = true;

        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS,
                0.9F, 1.5F + level.random.nextFloat() * 0.3F);

        // --- особые свойства дисков ---
        if (stats.bleedTicks() > 0) {
            MobEffectInstance current = target.getEffect(ModEffects.BLEEDING);
            int level0 = current == null ? 0 : Math.min(current.getAmplifier() + 1, MAX_BLEED_LEVEL);
            target.addEffect(new MobEffectInstance(ModEffects.BLEEDING, stats.bleedTicks(), level0));
        }
        if (stats.fireSeconds() > 0 && !target.fireImmune()) {
            target.setRemainingFireTicks(stats.fireSeconds() * 20);
        }
        if (stats.lifesteal() > 0 && attacker instanceof Player) {
            attacker.heal((float) (attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * stats.lifesteal()));
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                    6, 0.25, 0.3, 0.25, 0.3);

            if (target instanceof Mob) {
                updateCutStage(serverLevel, target);
            }
            if (stats.xpBonus() > 0 && target.isDeadOrDying()) {
                ExperienceOrb.award(serverLevel, target.position(), stats.xpBonus());
            }
        }

        stack.hurtAndBreak(stats.wear(), attacker, EquipmentSlot.MAINHAND);
        return true;
    }

    /**
     * Чем меньше здоровья у моба, тем больше частей отпилено:
     * правая рука -> левая рука -> правая нога -> левая нога -> (смерть) голова.
     */
    static void updateCutStage(ServerLevel level, LivingEntity target) {
        Limb[] limbs = Limb.values();
        int oldStage = target.hasData(ModAttachments.CUT_STAGE) ? target.getData(ModAttachments.CUT_STAGE) : 0;

        int newStage;
        if (target.isDeadOrDying()) {
            newStage = limbs.length;
        } else {
            float lost = 1.0F - target.getHealth() / target.getMaxHealth();
            newStage = Math.max(1, Math.min(limbs.length - 1, (int) Math.ceil(lost * limbs.length)));
        }

        if (newStage <= oldStage) {
            return;
        }
        target.setData(ModAttachments.CUT_STAGE, newStage);

        for (int i = oldStage; i < newStage; i++) {
            Limb limb = limbs[i];
            level.sendParticles(new DustParticleOptions(new Vector3f(0.7F, 0.05F, 0.05F), 1.3F),
                    target.getX(), target.getY() + target.getBbHeight() * limb.heightFactor, target.getZ(),
                    16, 0.2, 0.15, 0.2, 0.02);
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 0.7F);
        }

        // Без ног моб еле ползёт
        if (!target.isDeadOrDying() && newStage >= 3) {
            int amplifier = newStage >= 4 ? 4 : 1;
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 30, amplifier, false, false));
        }
    }
}
