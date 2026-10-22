package com.bolgarka;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Сменный диск для болгарки. */
public class DiscItem extends Item {
    private final int variant;
    private final DiscStats stats;

    /** variant - номер вида диска (0 = железный). Нужен, чтобы менять внешний вид болгарки. */
    public DiscItem(Properties properties, int variant, DiscStats stats) {
        super(properties);
        this.variant = variant;
        this.stats = stats;
    }

    public int variant() {
        return variant;
    }

    public DiscStats stats() {
        return stats;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.bolgarka.damage", stats.damage()).withStyle(ChatFormatting.BLUE));
        if (stats.bleedTicks() > 0) {
            tooltip.add(Component.translatable("tooltip.bolgarka.bleed").withStyle(ChatFormatting.DARK_RED));
        }
        if (stats.fireSeconds() > 0) {
            tooltip.add(Component.translatable("tooltip.bolgarka.fire").withStyle(ChatFormatting.GOLD));
        }
        if (stats.lifesteal() > 0) {
            tooltip.add(Component.translatable("tooltip.bolgarka.lifesteal").withStyle(ChatFormatting.RED));
        }
        if (stats.xpBonus() > 0) {
            tooltip.add(Component.translatable("tooltip.bolgarka.xp").withStyle(ChatFormatting.GREEN));
        }
        if (stats.wear() > 1) {
            tooltip.add(Component.translatable("tooltip.bolgarka.wear").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("tooltip.bolgarka.disc_hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
