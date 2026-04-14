package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class SalesInvoicePrintDialog extends FoxProChildDialog {
    public SalesInvoicePrintDialog(Window owner, String previewText) {
        super(owner, "Sales Invoice Print Preview", 760, 560);
        setContentPane(buildContent(previewText));
    }

    private JPanel buildContent(String previewText) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(FoxProTheme.FONT);
        area.setText(previewText == null ? "" : previewText);
        area.setCaretPosition(0);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actions.setBackground(FoxProTheme.PANEL);
        JButton toFile = FoxProTheme.createButton("To File");
        JButton exit = FoxProTheme.createButton("Exit");
        toFile.addActionListener(e -> writePreview(area.getText()));
        exit.addActionListener(e -> dispose());
        actions.add(toFile);
        actions.add(exit);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.add(new JScrollPane(area), BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    private void writePreview(String previewText) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("sales-invoice-preview.txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.writeString(Path.of(chooser.getSelectedFile().getAbsolutePath()), previewText);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage(), "Write Failed", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}
