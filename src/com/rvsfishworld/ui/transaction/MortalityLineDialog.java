package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.dao.LookupDAO;
import com.rvsfishworld.model.LookupItem;
import com.rvsfishworld.model.MortalityLine;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.generic.LookupDialog;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class MortalityLineDialog extends FoxProChildDialog {
    private final LookupDAO lookupDAO = new LookupDAO();
    private final JTextField txtProductCode = FoxProTheme.createTextField(10);
    private final JTextField txtDescription = FoxProTheme.createTextField(28);
    private final JTextField txtArea = FoxProTheme.createTextField(16);
    private final JTextField txtQuantity = FoxProTheme.createTextField(6);
    private final JTextField txtAverageCost = FoxProTheme.createTextField(8);
    private boolean saved;
    private MortalityLine line;

    public MortalityLineDialog(Window owner, MortalityLine existing, String defaultArea) {
        super(owner, existing == null ? "Add Mortality Line" : "Edit Mortality Line", 620, 280);
        this.line = existing == null ? new MortalityLine() : copy(existing);
        if (this.line.getArea().isBlank()) {
            this.line.setArea(defaultArea);
        }
        setContentPane(buildContent());
        loadLine();
    }

    public boolean isSaved() {
        return saved;
    }

    public MortalityLine getLine() {
        return line;
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(FoxProTheme.PANEL);
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(form, gbc, 0, "Product", txtProductCode, buildLookupButton());
        addField(form, gbc, 1, "Description", txtDescription, null);
        addField(form, gbc, 2, "Area", txtArea, null);
        addField(form, gbc, 3, "Qty.", txtQuantity, null);
        addField(form, gbc, 4, "Average Cost", txtAverageCost, null);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.setBackground(FoxProTheme.PANEL);
        JButton save = FoxProTheme.createButton("Save");
        JButton cancel = FoxProTheme.createButton("Cancel");
        save.addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());
        buttons.add(save);
        buttons.add(cancel);

        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        return root;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field, JComponent extra) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        panel.add(field, gbc);
        if (extra != null) {
            gbc.gridx = 2;
            gbc.weightx = 0;
            panel.add(extra, gbc);
        }
    }

    private JButton buildLookupButton() {
        JButton button = FoxProTheme.createLookupButton();
        button.addActionListener(e -> openProductLookup());
        return button;
    }

    private void loadLine() {
        txtProductCode.setText(defaultString(line.getProductCode()));
        txtDescription.setText(defaultString(line.getDescription()));
        txtArea.setText(defaultString(line.getArea()));
        txtQuantity.setText(Integer.toString(Math.max(0, line.getQuantity())));
        txtAverageCost.setText(line.getAverageCost() == null ? "0.00" : line.getAverageCost().toPlainString());
    }

    private void openProductLookup() {
        LookupDialog dialog = new LookupDialog(this, "Product Lookup", lookupDAO::findProducts);
        dialog.setVisible(true);
        LookupItem item = dialog.getSelectedItem();
        if (item != null) {
            txtProductCode.setText(item.getCode());
            txtDescription.setText(item.getName());
        }
    }

    private void onSave() {
        if (txtProductCode.getText().isBlank()) {
            CisDialogs.showError(this, "Product code is required.");
            return;
        }
        try {
            line.setProductCode(txtProductCode.getText().trim());
            line.setDescription(txtDescription.getText().trim());
            line.setArea(txtArea.getText().trim());
            line.setQuantity(Integer.parseInt(txtQuantity.getText().trim()));
            line.setAverageCost(new BigDecimal(txtAverageCost.getText().trim()));
            line.recompute();
            saved = true;
            dispose();
        } catch (Exception e) {
            CisDialogs.showError(this, "Invalid mortality line values: " + e.getMessage());
        }
    }

    private MortalityLine copy(MortalityLine source) {
        MortalityLine copy = new MortalityLine();
        copy.setId(source.getId());
        copy.setProductCode(source.getProductCode());
        copy.setDescription(source.getDescription());
        copy.setArea(source.getArea());
        copy.setQuantity(source.getQuantity());
        copy.setAverageCost(source.getAverageCost());
        copy.setTotalCost(source.getTotalCost());
        return copy;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
