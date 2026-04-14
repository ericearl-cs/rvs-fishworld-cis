package com.rvsfishworld.ui;

import com.rvsfishworld.dao.LookupDAO;
import com.rvsfishworld.model.LookupItem;
import com.rvsfishworld.model.ReceivingLine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ReceivingLineDialog extends JDialog {
    private final LookupDAO lookupDAO = new LookupDAO();

    private final JTextField txtGroupCode = FoxProTheme.createTextField(6);
    private final JTextField txtGroupName = FoxProTheme.createTextField(20);
    private final JTextField txtProductCode = FoxProTheme.createTextField(12);
    private final JTextField txtProductDescription = FoxProTheme.createTextField(34);
    private final JTextField txtQtyDelivered = FoxProTheme.createTextField(10);
    private final JTextField txtQtyDoa = FoxProTheme.createTextField(10);
    private final JTextField txtQtyRejected = FoxProTheme.createTextField(10);
    private final JTextField txtQtyBought = FoxProTheme.createTextField(10);
    private final JTextField txtUnitCost = FoxProTheme.createTextField(16);
    private final JTextField txtTotalCost = FoxProTheme.createTextField(16);
    private final JCheckBox chkStop = new JCheckBox("STOP");

    private boolean accepted;
    private ReceivingLine line;
    private long productId;

    public ReceivingLineDialog(Window owner, ReceivingLine existing) {
        super(owner, existing == null ? "Receiving Line" : "Edit Receiving Line", ModalityType.APPLICATION_MODAL);
        FoxProTheme.applyGlobalFont();
        setSize(860, 430);
        setLocationRelativeTo(owner);
        setResizable(false);
        buildUi();
        bindEvents();
        bindEnterAsTab();
        if (existing == null) {
            resetDefaults();
        } else {
            loadExisting(existing);
        }
    }

    private void resetDefaults() {
        txtGroupCode.setText("");
        txtGroupName.setText("");
        txtProductCode.setText("");
        txtProductDescription.setText("");
        txtQtyDelivered.setText("");
        txtQtyDoa.setText("");
        txtQtyRejected.setText("");
        txtQtyBought.setText("");
        txtUnitCost.setText("");
        txtTotalCost.setText("");
        chkStop.setSelected(false);
    }

    private void buildUi() {
        txtGroupName.setEditable(false);
        txtProductDescription.setEditable(false);
        txtQtyBought.setEditable(false);
        txtUnitCost.setEditable(false);
        txtTotalCost.setEditable(false);
        chkStop.setBackground(FoxProTheme.PANEL);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildButtons(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(FoxProTheme.PANEL);

        JButton btnGroupLookup = FoxProTheme.createLookupButton();
        JButton btnProductLookup = FoxProTheme.createLookupButton();
        btnGroupLookup.addActionListener(e -> fillGroupName());
        btnProductLookup.addActionListener(e -> openProductLookup());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(panel, gbc, 0, "GROUP", txtGroupCode, btnGroupLookup, txtGroupName);
        addRow(panel, gbc, 1, "PRODUCT", txtProductCode, btnProductLookup, txtProductDescription);
        addRow(panel, gbc, 2, "Qty. Del.", txtQtyDelivered, null, null);
        addRow(panel, gbc, 3, "Qty. D.O.A.", txtQtyDoa, null, null);
        addRow(panel, gbc, 4, "Qty. Rejected", txtQtyRejected, null, null);
        addRow(panel, gbc, 5, "Qty. Bought", txtQtyBought, null, null);
        addRow(panel, gbc, 6, "Unit Cost", txtUnitCost, null, null);
        addRow(panel, gbc, 7, "Total Cost", txtTotalCost, null, null);

        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.weightx = 0;
        panel.add(chkStop, gbc);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        panel.setBackground(FoxProTheme.PANEL);

        JButton btnAccept = FoxProTheme.createButton("Accept");
        JButton btnExit = FoxProTheme.createButton("Exit");
        btnAccept.addActionListener(e -> onAccept());
        btnExit.addActionListener(e -> dispose());

        panel.add(btnAccept);
        panel.add(btnExit);
        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field, JComponent extra, JComponent third) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.28;
        panel.add(field, gbc);

        if (extra != null) {
            gbc.gridx = 2;
            gbc.weightx = 0;
            panel.add(extra, gbc);
        }

        if (third != null) {
            gbc.gridx = 3;
            gbc.weightx = 1;
            panel.add(third, gbc);
        }
    }

    private void bindEnterAsTab() {
        JComponent[] fields = {txtGroupCode, txtProductCode, txtQtyDelivered, txtQtyDoa, txtQtyRejected, chkStop};
        for (JComponent field : fields) {
            InputMap inputMap = field.getInputMap(JComponent.WHEN_FOCUSED);
            ActionMap actionMap = field.getActionMap();
            inputMap.put(KeyStroke.getKeyStroke("ENTER"), "focusNext");
            actionMap.put("focusNext", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (field == txtGroupCode) {
                        fillGroupName();
                    } else if (field == txtProductCode) {
                        lookupProductByCode();
                    }
                    field.transferFocus();
                }
            });
        }
    }

    private void bindEvents() {
        registerRecomputeOnChange(txtQtyDelivered);
        registerRecomputeOnChange(txtQtyDoa);
        registerRecomputeOnChange(txtQtyRejected);
    }

    private void registerRecomputeOnChange(JTextField field) {
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { recompute(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { recompute(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { recompute(); }
        });
    }

    private void fillGroupName() {
        String code = txtGroupCode.getText().trim();
        if (code.isEmpty()) {
            txtGroupName.setText("");
            return;
        }
        String clean = code.replaceFirst("^0+", "");
        txtGroupName.setText(clean.isEmpty() ? "GROUP 0" : "GROUP " + clean);
    }

    private void openProductLookup() {
        LookupDialog dialog = new LookupDialog(this, "Product Lookup", lookupDAO::findProducts);
        dialog.setVisible(true);
        LookupItem item = dialog.getSelectedItem();
        if (item != null) {
            productId = item.getId();
            txtProductCode.setText(item.getCode());
            txtProductDescription.setText(item.getName());
            fillSuggestedUnitCost();
            recompute();
        }
    }

    private void lookupProductByCode() {
        String code = txtProductCode.getText().trim();
        if (code.isEmpty()) {
            productId = 0;
            txtProductDescription.setText("");
            txtUnitCost.setText("");
            txtTotalCost.setText("");
            return;
        }
        LookupItem item = lookupDAO.findProductByCode(code);
        if (item == null) {
            productId = 0;
            txtProductDescription.setText("");
            txtUnitCost.setText("");
            txtTotalCost.setText("");
            return;
        }
        productId = item.getId();
        txtProductDescription.setText(item.getName());
        fillSuggestedUnitCost();
        recompute();
    }

    private void fillSuggestedUnitCost() {
        if (productId <= 0) {
            txtUnitCost.setText("");
            return;
        }
        BigDecimal cost = lookupDAO.findSuggestedUnitCost(productId);
        if (cost != null && cost.compareTo(BigDecimal.ZERO) > 0) {
            txtUnitCost.setText(cost.setScale(4, RoundingMode.HALF_UP).toPlainString());
        } else {
            txtUnitCost.setText("");
        }
    }

    private void recompute() {
        BigDecimal delivered = toDecimal(txtQtyDelivered.getText());
        BigDecimal doa = toDecimal(txtQtyDoa.getText());
        BigDecimal rejected = toDecimal(txtQtyRejected.getText());
        BigDecimal unitCost = toDecimal(txtUnitCost.getText());

        if (isBlank(txtQtyDelivered.getText()) && isBlank(txtQtyDoa.getText()) && isBlank(txtQtyRejected.getText())) {
            txtQtyBought.setText("");
            txtTotalCost.setText(isBlank(txtUnitCost.getText()) ? "" : "0.0000");
            return;
        }

        BigDecimal bought = delivered.subtract(doa).subtract(rejected);
        if (bought.compareTo(BigDecimal.ZERO) < 0) {
            bought = BigDecimal.ZERO;
        }

        txtQtyBought.setText(bought.setScale(0, RoundingMode.HALF_UP).toPlainString());
        if (isBlank(txtUnitCost.getText())) {
            txtTotalCost.setText("");
        } else {
            BigDecimal total = bought.multiply(unitCost).setScale(4, RoundingMode.HALF_UP);
            txtTotalCost.setText(total.toPlainString());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void onAccept() {
        if (productId <= 0) {
            JOptionPane.showMessageDialog(this, "Product is required.");
            return;
        }
        if (toDecimal(txtQtyDelivered.getText()).compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Qty. Del. must be greater than 0.");
            return;
        }

        if (line == null) {
            line = new ReceivingLine();
        }

        line.setGroupCode(txtGroupCode.getText().trim());
        line.setProductId(productId);
        line.setProductCode(txtProductCode.getText().trim());
        line.setProductDescription(txtProductDescription.getText().trim());
        line.setQtyDelivered(toDecimal(txtQtyDelivered.getText()));
        line.setQtyDoa(toDecimal(txtQtyDoa.getText()));
        line.setQtyRejected(toDecimal(txtQtyRejected.getText()));
        line.setRejectReason("");
        line.setTank("");
        line.setQtyBought(toDecimal(txtQtyBought.getText()));
        line.setUnitCost(toDecimal(txtUnitCost.getText()));
        line.setTotalCost(toDecimal(txtTotalCost.getText()));
        line.setStopFlag(chkStop.isSelected());
        line.recompute();

        accepted = true;
        dispose();
    }

    private void loadExisting(ReceivingLine existing) {
        line = existing;
        productId = existing.getProductId();
        txtGroupCode.setText(existing.getGroupCode());
        fillGroupName();
        txtProductCode.setText(existing.getProductCode());
        txtProductDescription.setText(existing.getProductDescription());
        txtQtyDelivered.setText(existing.getQtyDelivered().stripTrailingZeros().toPlainString());
        txtQtyDoa.setText(existing.getQtyDoa().stripTrailingZeros().toPlainString());
        txtQtyRejected.setText(existing.getQtyRejected().stripTrailingZeros().toPlainString());
        txtQtyBought.setText(existing.getQtyBought().stripTrailingZeros().toPlainString());
        txtUnitCost.setText(existing.getUnitCost().setScale(4, RoundingMode.HALF_UP).toPlainString());
        txtTotalCost.setText(existing.getTotalCost().setScale(4, RoundingMode.HALF_UP).toPlainString());
        chkStop.setSelected(existing.isStopFlag());
    }

    private BigDecimal toDecimal(String value) {
        try {
            String clean = value == null || value.isBlank() ? "0" : value.trim();
            return new BigDecimal(clean);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public boolean isAccepted() {
        return accepted;
    }

    public ReceivingLine getLine() {
        return line;
    }
}
