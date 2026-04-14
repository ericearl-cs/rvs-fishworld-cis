package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.dao.LookupDAO;
import com.rvsfishworld.model.LookupItem;
import com.rvsfishworld.model.ProformaLine;
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
public class ProformaLineDialog extends FoxProChildDialog {
    private final LookupDAO lookupDAO = new LookupDAO();
    private final JTextField txtTransShipper = FoxProTheme.createTextField(8);
    private final JTextField txtBoxNo = FoxProTheme.createTextField(6);
    private final JTextField txtProductCode = FoxProTheme.createTextField(10);
    private final JTextField txtDescription = FoxProTheme.createTextField(28);
    private final JTextField txtQuantity = FoxProTheme.createTextField(6);
    private final JTextField txtPrice = FoxProTheme.createTextField(8);
    private final JTextField txtSupplierCode = FoxProTheme.createTextField(10);
    private boolean saved;
    private ProformaLine line;

    public ProformaLineDialog(Window owner, ProformaLine existing) {
        super(owner, existing == null ? "Add Proforma Line" : "Edit Proforma Line", 650, 300);
        this.line = existing == null ? new ProformaLine() : copy(existing);
        setContentPane(buildContent());
        loadLine();
    }

    public boolean isSaved() {
        return saved;
    }

    public ProformaLine getLine() {
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

        addField(form, gbc, 0, "Tran-Shipper", txtTransShipper, null);
        addField(form, gbc, 1, "Box No.", txtBoxNo, null);
        addField(form, gbc, 2, "Product", txtProductCode, buildLookupButton());
        addField(form, gbc, 3, "Description", txtDescription, null);
        addField(form, gbc, 4, "Qty. Order", txtQuantity, null);
        addField(form, gbc, 5, "Selling Price", txtPrice, null);
        addField(form, gbc, 6, "Supplier", txtSupplierCode, null);

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

    private JButton buildLookupButton() {
        JButton button = FoxProTheme.createLookupButton();
        button.addActionListener(e -> openProductLookup());
        return button;
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

    private void loadLine() {
        txtTransShipper.setText(defaultString(line.getTransShipperCode()));
        txtBoxNo.setText(defaultString(line.getBoxNo()));
        txtProductCode.setText(defaultString(line.getProductCode()));
        txtDescription.setText(defaultString(line.getDescription()));
        txtQuantity.setText(Integer.toString(Math.max(0, line.getQuantity())));
        txtPrice.setText(line.getPrice() == null ? "0.00" : line.getPrice().toPlainString());
        txtSupplierCode.setText(defaultString(line.getSupplierCode()));
    }

    private void openProductLookup() {
        LookupDialog dialog = new LookupDialog(this, "Product Lookup", lookupDAO::findProducts);
        dialog.setVisible(true);
        LookupItem item = dialog.getSelectedItem();
        if (item == null) {
            return;
        }
        txtProductCode.setText(item.getCode());
        txtDescription.setText(item.getName());
    }

    private void onSave() {
        if (txtProductCode.getText().isBlank()) {
            CisDialogs.showError(this, "Product code is required.");
            return;
        }
        try {
            line.setTransShipperCode(txtTransShipper.getText().trim());
            line.setBoxNo(txtBoxNo.getText().trim());
            line.setProductCode(txtProductCode.getText().trim());
            line.setDescription(txtDescription.getText().trim());
            line.setQuantity(Integer.parseInt(txtQuantity.getText().trim()));
            line.setPrice(new BigDecimal(txtPrice.getText().trim()));
            line.setSupplierCode(txtSupplierCode.getText().trim());
            line.recompute();
            saved = true;
            dispose();
        } catch (Exception e) {
            CisDialogs.showError(this, "Invalid line values: " + e.getMessage());
        }
    }

    private ProformaLine copy(ProformaLine source) {
        ProformaLine copy = new ProformaLine();
        copy.setId(source.getId());
        copy.setLineNo(source.getLineNo());
        copy.setTransShipperCode(source.getTransShipperCode());
        copy.setBoxNo(source.getBoxNo());
        copy.setProductCode(source.getProductCode());
        copy.setDescription(source.getDescription());
        copy.setSupplierCode(source.getSupplierCode());
        copy.setQuantity(source.getQuantity());
        copy.setPrice(source.getPrice());
        copy.setTotalPrice(source.getTotalPrice());
        return copy;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
