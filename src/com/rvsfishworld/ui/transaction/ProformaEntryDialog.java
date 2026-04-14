package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.dao.GenericDocumentDAO;
import com.rvsfishworld.dao.LookupDAO;
import com.rvsfishworld.model.LookupItem;
import com.rvsfishworld.model.ProformaLine;
import com.rvsfishworld.model.ProformaRecord;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.generic.LookupDialog;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class ProformaEntryDialog extends FoxProChildDialog {
    private final GenericDocumentDAO documentDAO = new GenericDocumentDAO();
    private final LookupDAO lookupDAO = new LookupDAO();
    private ProformaRecord record;
    private boolean saved;

    private final JTextField txtCustomerCode = FoxProTheme.createTextField(10);
    private final JTextField txtCustomerName = FoxProTheme.createTextField(28);
    private final JTextField txtSalesmanCode = FoxProTheme.createTextField(8);
    private final JTextField txtSalesmanName = FoxProTheme.createTextField(22);
    private final JTextField txtBranchCode = FoxProTheme.createTextField(8);
    private final JTextField txtBranchName = FoxProTheme.createTextField(22);
    private final JTextField txtProformaNo = FoxProTheme.createTextField(12);
    private final JTextField txtDate = FoxProTheme.createTextField(10);
    private final JTextField txtAdjustment = FoxProTheme.createTextField(8);
    private final JTextField txtPreparedBy = FoxProTheme.createTextField(10);
    private final JTextField txtApprovedBy = FoxProTheme.createTextField(10);
    private final JTextField txtPackingCharges = FoxProTheme.createTextField(8);
    private final JTextField txtGrandTotal = FoxProTheme.createTextField(10);
    private final JTextField txtTotalPayables = FoxProTheme.createTextField(10);

    private final DefaultTableModel lineModel = new DefaultTableModel(
            new Object[]{"Tran-Shipper", "BOX", "Product No.", "Description", "Qty. Order", "Selling Price", "Total Price", "Supplier"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable lineTable = new JTable(lineModel);

    public ProformaEntryDialog(Window owner) {
        this(owner, new ProformaRecord());
    }

    public ProformaEntryDialog(Window owner, ProformaRecord source) {
        super(owner, "Proforma Entry", 1220, 760);
        this.record = source == null ? new ProformaRecord() : source;
        if (this.record.getInvoiceDate() == null) {
            this.record.setInvoiceDate(LocalDate.now());
        }
        setContentPane(buildContent());
        loadRecord();
    }

    public boolean isSaved() {
        return saved;
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(FoxProTheme.PANEL);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(panel, gbc, 0, 0, "Customer", txtCustomerCode, lookupButton(this::openCustomerLookup), txtCustomerName);
        addField(panel, gbc, 1, 0, "Salesman", txtSalesmanCode, lookupButton(this::openSalesmanLookup), txtSalesmanName);
        addField(panel, gbc, 2, 0, "Branch", txtBranchCode, lookupButton(this::openBranchLookup), txtBranchName);
        addField(panel, gbc, 0, 3, "Proforma No.", txtProformaNo, null, null);
        addField(panel, gbc, 1, 3, "Date", txtDate, null, null);
        addField(panel, gbc, 2, 3, "Adjustment", txtAdjustment, null, null);
        return panel;
    }

    private JPanel buildCenter() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(FoxProTheme.PANEL);
        FoxProTheme.styleTable(lineTable);
        lineTable.setRowHeight(24);
        panel.add(new JScrollPane(lineTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actions.setBackground(FoxProTheme.PANEL);
        JButton add = FoxProTheme.createButton("Add");
        JButton edit = FoxProTheme.createButton("Edit");
        JButton delete = FoxProTheme.createButton("Delete");
        add.addActionListener(e -> addLine());
        edit.addActionListener(e -> editLine());
        delete.addActionListener(e -> deleteLine());
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(FoxProTheme.PANEL);

        JPanel signatures = new JPanel(new GridBagLayout());
        signatures.setBackground(FoxProTheme.PANEL);
        signatures.setBorder(FoxProTheme.sectionBorder("Approval"));
        addSimpleField(signatures, 0, "Issued by", txtPreparedBy);
        addSimpleField(signatures, 1, "Approved by", txtApprovedBy);

        JPanel totals = new JPanel(new GridBagLayout());
        totals.setBackground(FoxProTheme.PANEL);
        totals.setBorder(FoxProTheme.sectionBorder("Totals"));
        addSimpleField(totals, 0, "Packing Charges", txtPackingCharges);
        addSimpleField(totals, 1, "Grand Total", txtGrandTotal);
        addSimpleField(totals, 2, "Total Payables", txtTotalPayables);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.setBackground(FoxProTheme.PANEL);
        JButton save = FoxProTheme.createButton("Save");
        JButton print = FoxProTheme.createButton("Print");
        JButton cancelRecall = FoxProTheme.createButton("Cancel/Recall");
        JButton exit = FoxProTheme.createButton("Exit");
        save.addActionListener(e -> saveRecord());
        print.addActionListener(e -> new ProformaPrintDialog(this, buildPreviewRecord()).setVisible(true));
        cancelRecall.addActionListener(e -> dispose());
        exit.addActionListener(e -> dispose());
        buttons.add(save);
        buttons.add(print);
        buttons.add(cancelRecall);
        buttons.add(exit);

        panel.add(signatures, BorderLayout.WEST);
        panel.add(totals, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, int col, String label, JComponent field, JComponent extra, JComponent third) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = col + 1;
        gbc.weightx = 0.2;
        panel.add(field, gbc);

        if (extra != null) {
            gbc.gridx = col + 2;
            gbc.weightx = 0;
            panel.add(extra, gbc);
        }
        if (third != null) {
            gbc.gridx = col + 3;
            gbc.weightx = 0.4;
            panel.add(third, gbc);
        }
    }

    private void addSimpleField(JPanel panel, int row, String label, JTextField field) {
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private JButton lookupButton(Runnable action) {
        JButton button = FoxProTheme.createLookupButton();
        button.addActionListener(e -> action.run());
        return button;
    }

    private void openCustomerLookup() {
        LookupDialog dialog = new LookupDialog(this, "Customer Lookup", lookupDAO::findCustomers);
        dialog.setVisible(true);
        LookupItem item = dialog.getSelectedItem();
        if (item != null) {
            txtCustomerCode.setText(item.getCode());
            txtCustomerName.setText(item.getName());
        }
    }

    private void openSalesmanLookup() {
        LookupDialog dialog = new LookupDialog(this, "Salesman Lookup", lookupDAO::findSalesmen);
        dialog.setVisible(true);
        LookupItem item = dialog.getSelectedItem();
        if (item != null) {
            txtSalesmanCode.setText(item.getCode());
            txtSalesmanName.setText(item.getName());
        }
    }

    private void openBranchLookup() {
        LookupDialog dialog = new LookupDialog(this, "Branch Lookup", lookupDAO::findBranches);
        dialog.setVisible(true);
        LookupItem item = dialog.getSelectedItem();
        if (item != null) {
            txtBranchCode.setText(item.getCode());
            txtBranchName.setText(item.getName());
        }
    }

    private void addLine() {
        ProformaLineDialog dialog = new ProformaLineDialog(this, null);
        dialog.setVisible(true);
        if (!dialog.isSaved()) {
            return;
        }
        record.getLines().add(dialog.getLine());
        refreshGrid();
    }

    private void editLine() {
        int row = lineTable.getSelectedRow();
        if (row < 0) {
            CisDialogs.showInfo(this, "Select a line first.");
            return;
        }
        ProformaLine current = record.getLines().get(row);
        ProformaLineDialog dialog = new ProformaLineDialog(this, current);
        dialog.setVisible(true);
        if (!dialog.isSaved()) {
            return;
        }
        record.getLines().set(row, dialog.getLine());
        refreshGrid();
    }

    private void deleteLine() {
        int row = lineTable.getSelectedRow();
        if (row < 0) {
            CisDialogs.showInfo(this, "Select a line first.");
            return;
        }
        record.getLines().remove(row);
        refreshGrid();
    }

    private void loadRecord() {
        txtProformaNo.setText(defaultString(record.getProformaNo()));
        txtDate.setText(record.getInvoiceDate() == null ? "" : record.getInvoiceDate().toString());
        txtCustomerCode.setText(defaultString(record.getCustomerCode()));
        txtCustomerName.setText(defaultString(record.getCustomerName()));
        txtBranchCode.setText(defaultString(record.getBranchCode()));
        txtBranchName.setText(defaultString(record.getBranchName()));
        txtSalesmanCode.setText(defaultString(record.getSalesmanCode()));
        txtSalesmanName.setText(defaultString(record.getSalesmanName()));
        txtAdjustment.setText(money(record.getAdjustmentPercent()).toPlainString());
        txtPackingCharges.setText(money(record.getPackingCharges()).toPlainString());
        txtPreparedBy.setText(defaultString(record.getPreparedBy()));
        txtApprovedBy.setText(defaultString(record.getApprovedBy()));
        refreshGrid();
    }

    private void refreshGrid() {
        lineModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (ProformaLine line : record.getLines()) {
            line.recompute();
            total = total.add(money(line.getTotalPrice()));
            lineModel.addRow(new Object[]{
                    line.getTransShipperCode(),
                    line.getBoxNo(),
                    line.getProductCode(),
                    line.getDescription(),
                    line.getQuantity(),
                    money(line.getPrice()).toPlainString(),
                    money(line.getTotalPrice()).toPlainString(),
                    line.getSupplierCode()
            });
        }
        BigDecimal discount = total.multiply(money(parseDecimal(txtAdjustment.getText())))
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal packing = money(parseDecimal(txtPackingCharges.getText()));
        txtGrandTotal.setText(money(total).toPlainString());
        txtTotalPayables.setText(money(total.subtract(discount).add(packing)).toPlainString());
    }

    private void saveRecord() {
        if (txtCustomerCode.getText().isBlank()) {
            CisDialogs.showError(this, "Customer is required.");
            return;
        }
        if (record.getLines().isEmpty()) {
            CisDialogs.showError(this, "Add at least one line.");
            return;
        }
        try {
            record.setProformaNo(txtProformaNo.getText().trim());
            record.setInvoiceDate(txtDate.getText().isBlank() ? LocalDate.now() : LocalDate.parse(txtDate.getText().trim()));
            record.setCustomerCode(txtCustomerCode.getText().trim());
            record.setCustomerName(txtCustomerName.getText().trim());
            record.setBranchCode(txtBranchCode.getText().trim());
            record.setBranchName(txtBranchName.getText().trim());
            record.setSalesmanCode(txtSalesmanCode.getText().trim());
            record.setSalesmanName(txtSalesmanName.getText().trim());
            record.setAdjustmentPercent(money(parseDecimal(txtAdjustment.getText())));
            record.setPackingCharges(money(parseDecimal(txtPackingCharges.getText())));
            record.setPreparedBy(txtPreparedBy.getText().trim());
            record.setApprovedBy(txtApprovedBy.getText().trim());
            documentDAO.saveProforma(record);
            saved = true;
            dispose();
        } catch (Exception e) {
            CisDialogs.showError(this, "Unable to save Proforma: " + e.getMessage());
        }
    }

    private ProformaRecord buildPreviewRecord() {
        ProformaRecord preview = record;
        preview.setAdjustmentPercent(money(parseDecimal(txtAdjustment.getText())));
        preview.setPackingCharges(money(parseDecimal(txtPackingCharges.getText())));
        return preview;
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
