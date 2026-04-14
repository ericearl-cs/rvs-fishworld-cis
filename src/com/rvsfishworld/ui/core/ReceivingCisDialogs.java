package com.rvsfishworld.ui.core;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Restored print-preview helper used by browse/report shells.
 */
public final class ReceivingCisDialogs {
    private ReceivingCisDialogs() {
    }

    public static void showPrintPreview(Component owner, String title, String text) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(owner), title, JDialog.ModalityType.APPLICATION_MODAL);
        JTextArea area = new JTextArea(text == null ? "" : text, 24, 80);
        area.setEditable(false);
        area.setCaretPosition(0);
        JButton close = new JButton("Close");
        close.addActionListener(e -> dialog.dispose());
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.add(new JScrollPane(area), BorderLayout.CENTER);
        dialog.add(close, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }
}
