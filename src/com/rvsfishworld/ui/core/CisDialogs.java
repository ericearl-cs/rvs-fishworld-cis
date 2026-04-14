package com.rvsfishworld.ui.core;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Minimal dialog helpers restored during the source recovery phase.
 */
public final class CisDialogs {
    public static final int YES = JOptionPane.YES_OPTION;
    public static final int NO = JOptionPane.NO_OPTION;
    public static final int CANCEL = JOptionPane.CANCEL_OPTION;
    public static final int CLOSED = JOptionPane.CLOSED_OPTION;

    private CisDialogs() {
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static int askYesNo(Component parent, String title, String message) {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION);
    }

    public static int askYesNoCancel(Component parent, String title, String message) {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_CANCEL_OPTION);
    }
}
