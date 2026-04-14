package com.rvsfishworld.ui.chrome;

import com.rvsfishworld.ui.FoxProTheme;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;

public final class ReceivingCisChrome {
    private ReceivingCisChrome() {
    }

    public static void apply(JInternalFrame frame) {
        frame.getContentPane().setBackground(FoxProTheme.PANEL);
    }

    public static void apply(JDialog dialog) {
        dialog.getContentPane().setBackground(FoxProTheme.PANEL);
    }

    public static void apply(JComponent component) {
        component.setBackground(FoxProTheme.PANEL);
    }
}
