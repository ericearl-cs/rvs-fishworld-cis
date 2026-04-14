package com.rvsfishworld.ui.core;

import java.awt.Dimension;

public final class CisScale {
    private static final double SCALE = 1.15d;

    private CisScale() {
    }

    public static int scale(int value) {
        return (int) Math.round(value * SCALE);
    }

    public static Dimension scale(Dimension size) {
        return new Dimension(scale(size.width), scale(size.height));
    }
}
