package com.bolgarka;

/**
 * Порядок, в котором у моба отваливаются части тела.
 * heightFactor - на какой доле высоты моба появляются частицы (брызги).
 */
public enum Limb {
    RIGHT_ARM(0.70),
    LEFT_ARM(0.70),
    RIGHT_LEG(0.25),
    LEFT_LEG(0.25),
    HEAD(0.90);

    public final double heightFactor;

    Limb(double heightFactor) {
        this.heightFactor = heightFactor;
    }
}
