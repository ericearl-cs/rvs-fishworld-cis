package com.rvsfishworld.ui.master;

import com.rvsfishworld.db.Database;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.core.CisScale;
import com.rvsfishworld.ui.core.CisTheme;
import java.awt.Window;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class ProductPrintDialog extends FoxProChildDialog {
    private final String currentProductCode;
    private final JRadioButton currentRecord = new JRadioButton("Current Record", true);
    private final JRadioButton allRecords = new JRadioButton("All Records");
    private final JRadioButton priceA = new JRadioButton("Price A", true);
    private final JRadioButton priceB = new JRadioButton("Price B");
    private final JRadioButton local = new JRadioButton("LOCAL");

    public ProductPrintDialog(Window owner, String currentProductCode) {
        super(owner, "Product Printing", CisScale.scale(340), CisScale.scale(180));
        this.currentProductCode = currentProductCode == null ? "" : currentProductCode;
        setResizable(false);
        setContentPane(buildContent());
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(null);
        root.setBackground(CisTheme.PANEL);

        JPanel scopePanel = groupBox(18, 22, 118, 54);
        addRadio(scopePanel, currentRecord, 10, 8);
        addRadio(scopePanel, allRecords, 10, 26);
        ButtonGroup scopeGroup = new ButtonGroup();
        scopeGroup.add(currentRecord);
        scopeGroup.add(allRecords);

        JPanel pricePanel = groupBox(176, 22, 80, 68);
        addRadio(pricePanel, priceA, 10, 8);
        addRadio(pricePanel, priceB, 10, 26);
        addRadio(pricePanel, local, 10, 44);
        ButtonGroup priceGroup = new ButtonGroup();
        priceGroup.add(priceA);
        priceGroup.add(priceB);
        priceGroup.add(local);

        JButton preview = button("View", 18, 108);
        preview.addActionListener(e -> showPreview());
        root.add(preview);

        JButton toFile = button("To File", 92, 108);
        toFile.addActionListener(e -> writePreviewToFile());
        root.add(toFile);

        JButton exit = button("Exit", 206, 108);
        exit.addActionListener(e -> dispose());
        root.add(exit);

        root.add(scopePanel);
        root.add(pricePanel);
        return root;
    }

    private JPanel groupBox(int x, int y, int width, int height) {
        JPanel panel = new JPanel(null);
        panel.setBackground(CisTheme.PANEL);
        panel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panel.setBounds(CisScale.scale(x), CisScale.scale(y), CisScale.scale(width), CisScale.scale(height));
        return panel;
    }

    private void addRadio(JPanel panel, JRadioButton button, int x, int y) {
        button.setBackground(CisTheme.PANEL);
        button.setFont(FoxProTheme.FONT);
        button.setBounds(CisScale.scale(x), CisScale.scale(y), CisScale.scale(100), CisScale.scale(16));
        panel.add(button);
    }

    private JButton button(String text, int x, int y) {
        JButton button = CisTheme.createFormButton(text, CisScale.scale(62), CisScale.scale(24));
        button.setBounds(CisScale.scale(x), CisScale.scale(y), CisScale.scale(70), CisScale.scale(24));
        return button;
    }

    private void showPreview() {
        PreviewDialog dialog = new PreviewDialog(this, buildPreviewText());
        dialog.setVisible(true);
    }

    private void writePreviewToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("product-print.txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.writeString(Path.of(chooser.getSelectedFile().toURI()), buildPreviewText());
            CisDialogs.showInfo(this, "File written:\n" + chooser.getSelectedFile().getAbsolutePath());
        } catch (IOException e) {
            CisDialogs.showError(this, "Unable to write file: " + e.getMessage());
        }
    }

    private String buildPreviewText() {
        String priceColumn = priceA.isSelected() ? "price_a" : priceB.isSelected() ? "price_b" : "local_sales_price";
        String priceLabel = priceA.isSelected() ? "PRICE A" : priceB.isSelected() ? "PRICE B" : "LOCAL";
        StringBuilder text = new StringBuilder("PRODUCT PRINTING - ").append(priceLabel).append("\n\n");
        text.append(String.format("%-10s %-38s %12s%n", "CODE", "DESCRIPTION", priceLabel));
        text.append("---------------------------------------------------------------------\n");

        String sql = allRecords.isSelected()
                ? "SELECT product_code, description, " + priceColumn + " FROM products WHERE is_active = TRUE ORDER BY product_code"
                : "SELECT product_code, description, " + priceColumn + " FROM products WHERE is_active = TRUE AND product_code = ? ORDER BY product_code";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (!allRecords.isSelected()) {
                ps.setString(1, currentProductCode);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    text.append(String.format(
                            "%-10s %-38s %12.2f%n",
                            rs.getString(1),
                            shorten(rs.getString(2), 38),
                            rs.getBigDecimal(3)));
                }
            }
        } catch (Exception e) {
            text.append("\nERROR: ").append(e.getMessage());
        }
        return text.toString();
    }

    private String shorten(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }

    private static class PreviewDialog extends FoxProChildDialog {
        PreviewDialog(Window owner, String text) {
            super(owner, "Product Print Preview", CisScale.scale(560), CisScale.scale(420));
            JTextArea area = new JTextArea(text);
            area.setEditable(false);
            area.setFont(new java.awt.Font("Courier New", java.awt.Font.PLAIN, CisScale.scale(10)));
            JScrollPane scroll = new JScrollPane(area);
            scroll.setBorder(null);
            setContentPane(scroll);
        }
    }
}
