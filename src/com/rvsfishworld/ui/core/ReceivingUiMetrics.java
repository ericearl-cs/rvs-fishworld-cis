package com.rvsfishworld.ui.core;

import com.rvsfishworld.ui.FoxProTheme;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

public final class ReceivingUiMetrics {
    public static final int COMMAND_BAR_WIDTH = CisScale.scale(128);
    public static final int ORDER_BUTTON_HEIGHT = CisScale.scale(30);
    public static final int TEXT_FIELD_HEIGHT = CisScale.scale(24);
    public static final int GRID_ROW_HEIGHT = CisScale.scale(24);
    public static final Font CONTROL_FONT = FoxProTheme.FONT;
    public static final Font CONTROL_FONT_BOLD = FoxProTheme.FONT_BOLD;

    private ReceivingUiMetrics() {
    }

    public static void applyFormFont(Component component) {
        if (component == null) {
            return;
        }
        component.setFont(CONTROL_FONT);
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyFormFont(child);
            }
        }
    }
}
