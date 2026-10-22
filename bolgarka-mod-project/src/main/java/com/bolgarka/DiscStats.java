package com.bolgarka;

/**
 * Свойства диска.
 *
 * @param damage      бонус к урону болгарки (у игрока базово 1.0, у железного диска итого 2.0)
 * @param wear        сколько прочности болгарки тратится за один удар
 * @param bleedTicks  длительность кровотечения в тиках (0 = нет), 20 тиков = 1 секунда
 * @param fireSeconds сколько секунд моб горит (0 = нет)
 * @param lifesteal   доля урона, которая лечит игрока (0 = нет)
 * @param xpBonus     дополнительный опыт за убийство (0 = нет)
 */
public record DiscStats(double damage, int wear, int bleedTicks, int fireSeconds, float lifesteal, int xpBonus) {
}
