package com.rvsfishworld.ui.transaction;

import java.awt.BorderLayout;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class LocalSalesPrintDialog extends JDialog {
    public LocalSalesPrintDialog(Window owner, String previewText) {
        super(owner, "Local Sales Print", ModalityType.APPLICATION_MODAL);
        JTextArea area = new JTextArea(previewText == null ? "" : previewText, 24, 72);
        area.setEditable(false);
        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        setLayout(new BorderLayout(8, 8));
        add(new JScrollPane(area), BorderLayout.CENTER);
        add(close, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }
}
