package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import java.awt.BorderLayout;
import java.awt.Window;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class SalesInvoicePrintDialog extends FoxProChildDialog {
    public SalesInvoicePrintDialog(Window owner, String previewText) {
        super(owner, "Sales Invoice Print Preview", 760, 560);
        setContentPane(buildContent(previewText));
    }

    private JScrollPane buildContent(String previewText) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(FoxProTheme.FONT);
        area.setText(previewText == null ? "" : previewText);
        area.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        return scroll;
    }
}
