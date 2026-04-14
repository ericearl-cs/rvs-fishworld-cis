package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.dao.LookupDAO;
import com.rvsfishworld.dao.SalesWorkflowDAO;
import com.rvsfishworld.model.LookupItem;
import com.rvsfishworld.model.SalesInvoiceLine;
import com.rvsfishworld.model.SalesInvoiceRecord;
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
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class SalesInvoiceEntryDialog extends FoxProChildDialog {
    private final SalesWorkflowDAO workflowDAO = new SalesWorkflowDAO();
    private final LookupDAO lookupDAO = new LookupDAO();
    private SalesInvoiceRecord record;
    private boolean saved;
    private boolean suppressUpdates;

    private final JTextField txtProformaNo = FoxProTheme.createTextField(10);
    private final JTextField txtInvoiceNo = FoxProTheme.createTextField(10);
    private final JTextField txtInvoiceDate = FoxProTheme.createTextField(10);
    private final JTextField txtExchangeRate = FoxProTheme.createTextField(8);
    private final JTextField txtAwbNo = FoxProTheme.createTextField(12);
    private final JTextField txtBroker = FoxProTheme.createTextField(16);
    private final JTextField txtCustomerCode = FoxProTheme.createTextField(10);
    private final JTextField txtCustomerName = FoxProTheme.createTextField(30);
    private final JTextField txtBranchCode = FoxProTheme.createTextField(8);
    private final JTextField txtBranchName = FoxProTheme.createTextField(22);
    private final JTextField txtSalesmanCode = FoxProTheme.createTextField(8);
    private final JTextField txtSalesmanName = FoxProTheme.createTextField(22);
    private final JTextField txtCurrencyCode = FoxProTheme.createTextField(6);
    private final JTextField txtCurrencyName = FoxProTheme.createTextField(20);
    private final JComboBox<String> cboPricing = new JComboBox<>(new String[]{"A", "B", "C", "D", "E", "F", "G", "S", "L"});
    private final JCheckBox chkApplyFormula = new JCheckBox("Apply Formula");
    private final JCheckBox chkConsumables = new JCheckBox("Consumables");
    private final JTextField txtBoxQty = FoxProTheme.createTextField(8);
    private final JTextField txtKgs = FoxProTheme.createTextField(8);
    private final JTextField txtFishCost = FoxProTheme.createTextField(8);
    private final JTextField txtDiscAmount = FoxProTheme.createTextField(8);
    private final JTextField txtMisc = FoxProTheme.createTextField(8);
    private final JTextField txtSsc = FoxProTheme.createTextField(8);
    private final JTextField txtRate = FoxProTheme.createTextField(8);
    private final JTextField txtVat = FoxProTheme.createTextField(8);
    private final JTextField txtStamp = FoxProTheme.createTextField(8);
    private final JTextField txtPreparedBy = FoxProTheme.createTextField(12);
    private final JTextField txtCheckedBy = FoxProTheme.createTextField(12);
    private final JTextField txtApprovedBy = FoxProTheme.createTextField(12);
    private final JTextField txtReceivedBy = FoxProTheme.createTextField(12);
    private final JTextField txtDiscountPercent = FoxProTheme.createTextField(8);
    private final JTextField txtDoa = FoxProTheme.createTextField(8);
    private final JTextField txtRate2 = FoxProTheme.createTextField(8);
    private final JTextField txtFreight = FoxProTheme.createTextField(8);
    private final JTextField txtPackingCharges = FoxProTheme.createTextField(8);
    private final JTextField txtProductSales = FoxProTheme.createTextField(10);
    private final JTextField txtTotalPayables = FoxProTheme.createTextField(10);

    private final DefaultTableModel lineModel = new DefaultTableModel(
            new Object[]{"Tran-Shipper", "BOX", "Product No.", "Description", "SPECIAL", "Qty. Sold", "Selling Price", "Total Price"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable lineTable = new JTable(lineModel);

    public SalesInvoiceEntryDialog(Window owner) {
        this(owner, new SalesInvoiceRecord());
    }

    public SalesInvoiceEntryDialog(Window owner, SalesInvoiceRecord source) {
        super(owner, "Adding Of Product Sales", 1260, 820);
        this.record = source == null ? new SalesInvoiceRecord() : source;
        if (this.record.getInvoiceDate() == null) {
            this.record.setInvoiceDate(LocalDate.now());
        }
        setContentPane(buildContent());
        loadRecordToFields();
        refreshGrid();
    }

    public boolean isSaved() {
        return saved;
    }

    public SalesInvoiceRecord getRecord() {
        return record;
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildGridPanel(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.setBackground(FoxProTheme.PANEL);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(FoxProTheme.PANEL);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(form, gbc, 0, 0, "Proforma No.", txtProformaNo, lookupButton(this::openProformaLookup), null);
        addField(form, gbc, 1, 0, "Invoice No.", txtInvoiceNo, null, null);
        addField(form, gbc, 2, 0, "Invoice Date", txtInvoiceDate, null, null);
        addField(form, gbc, 3, 0, "Exchange Rate", txtExchangeRate, null, null);
        addField(form, gbc, 4, 0, "AWB #", txtAwbNo, null, null);
        addField(form, gbc, 5, 0, "BROKER", txtBroker, null, null);

        addField(form, gbc, 0, 3, "Customer", txtCustomerCode, lookupButton(this::openCustomerLookup), txtCustomerName);
        addField(form, gbc, 1, 3, "Branch", txtBranchCode, lookupButton(this::openBranchLookup), txtBranchName);
        addField(form, gbc, 2, 3, "Salesman", txtSalesmanCode, lookupButton(this::openSalesmanLookup), txtSalesmanName);
        addField(form, gbc, 3, 3, "Currency", txtCurrencyCode, lookupButton(this::openCurrencyLookup), txtCurrencyName);

        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.weightx = 0;
        form.add(new JLabel("Pricing"), gbc);
        gbc.gridx = 4;
        gbc.weightx = 0.2;
        cboPricing.setFont(FoxProTheme.FONT);
        form.add(cboPricing, gbc);
        gbc.gridx = 5;
        gbc.weightx = 0;
        chkApplyFormula.setBackground(FoxProTheme.PANEL);
        form.add(chkApplyFormula, gbc);
        gbc.gridx = 6;
        chkConsumables.setBackground(FoxProTheme.PANEL);
        form.add(chkConsumables, gbc);

        JPanel metrics = new JPanel(new GridBagLayout());
        metrics.setBackground(FoxProTheme.PANEL);
        metrics.setBorder(FoxProTheme.sectionBorder("Totals Strip"));
        addMetric(metrics, 0, "BOX QTY.", txtBoxQty);
        addMetric(metrics, 1, "KGS.", txtKgs);
        addMetric(metrics, 2, "FISH COST", txtFishCost);
        addMetric(metrics, 3, "DISC", txtDiscAmount);
        addMetric(metrics, 4, "MISC.", txtMisc);
        addMetric(metrics, 5, "SSC", txtSsc);
        addMetric(metrics, 6, "RATE", txtRate);
        addMetric(metrics, 7, "VAT", txtVat);
        addMetric(metrics, 8, "STAMP", txtStamp);

        cboPricing.addActionListener(e -> queueApplyFormula());
        chkApplyFormula.addActionListener(e -> queueApplyFormula());

        wrapper.add(form, BorderLayout.NORTH);
        wrapper.add(metrics, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildGridPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(FoxProTheme.PANEL);
        FoxProTheme.styleTable(lineTable);
        lineTable.setRowHeight(24);
        panel.add(new JScrollPane(lineTable), BorderLayout.CENTER);

        JPanel boxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        boxPanel.setBackground(FoxProTheme.PANEL);
        JButton findBox = FoxProTheme.createButton("Find Box No.");
        findBox.addActionListener(e -> CisDialogs.showInfo(this, "Find Box No. will be wired after the core invoice path is stable."));
        boxPanel.add(findBox);
        panel.add(boxPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(FoxProTheme.PANEL);

        JPanel signatures = new JPanel(new GridBagLayout());
        signatures.setBackground(FoxProTheme.PANEL);
        signatures.setBorder(FoxProTheme.sectionBorder("Signatures"));
        addSimpleField(signatures, 0, "Prep.by", txtPreparedBy);
        addSimpleField(signatures, 1, "Check by", txtCheckedBy);
        addSimpleField(signatures, 2, "App.by", txtApprovedBy);
        addSimpleField(signatures, 3, "Rec. by", txtReceivedBy);

        JPanel totals = new JPanel(new GridBagLayout());
        totals.setBackground(FoxProTheme.PANEL);
        totals.setBorder(FoxProTheme.sectionBorder("Computed Totals"));
        addSimpleField(totals, 0, "Discount", txtDiscountPercent);
        addSimpleField(totals, 1, "DOA", txtDoa);
        addSimpleField(totals, 2, "RATE", txtRate2);
        addSimpleField(totals, 3, "FREIGHT", txtFreight);
        addSimpleField(totals, 4, "Packing Charges", txtPackingCharges);
        addSimpleField(totals, 5, "PRODUCT SALES", txtProductSales);
        addSimpleField(totals, 6, "TOTAL PAYABLES", txtTotalPayables);

        JButton printLabels = FoxProTheme.createButton("Print Labels");
        printLabels.setPreferredSize(new Dimension(120, 120));
        printLabels.addActionListener(e -> openPrintPreview());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actions.setBackground(FoxProTheme.PANEL);
        JButton add = FoxProTheme.createButton("Add");
        JButton edit = FoxProTheme.createButton("Edit");
        JButton delete = FoxProTheme.createButton("Delete");
        JButton save = FoxProTheme.createButton("Save");
        JButton exit = FoxProTheme.createButton("Exit");
        JButton cancelRecall = FoxProTheme.createButton("Cancel/Recall");
        add.addActionListener(e -> addLine());
        edit.addActionListener(e -> editLine());
        delete.addActionListener(e -> deleteLine());
        save.addActionListener(e -> saveRecord());
        exit.addActionListener(e -> dispose());
        cancelRecall.addActionListener(e -> dispose());
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        actions.add(save);
        actions.add(exit);
        actions.add(cancelRecall);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(FoxProTheme.PANEL);
        center.add(totals, BorderLayout.CENTER);
        center.add(printLabels, BorderLayout.EAST);

        panel.add(signatures, BorderLayout.WEST);
        panel.add(center, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JButton lookupButton(Runnable action) {
        JButton button = FoxProTheme.createLookupButton();
        button.addActionListener(e -> action.run());
        return button;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, int col, String label, JComponent primary, JComponent extra, JComponent secondary) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = col + 1;
        gbc.weightx = 0.15;
        panel.add(primary, gbc);
        if (extra != null) {
            gbc.gridx = col + 2;
            gbc.weightx = 0;
            panel.add(extra, gbc);
        }
        if (secondary != null) {
            gbc.gridx = col + 3;
            gbc.weightx = 0.45;
            panel.add(secondary, gbc);
        }
    }

    private void addMetric(JPanel panel, int index, String label, JTextField field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.gridx = index;
        gbc.gridy = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private void addSimpleField(JPanel panel, int row, String label, JTextField field) {
        GridBagConstraints gbc = new GridBagConstraints();
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

    private void openProformaLookup() {
        ProformaLookupDialog dialog = new ProformaLookupDialog(this);
        dialog.setVisible(true);
        if (dialog.getSelectedProformaNo() == null) {
            return;
        }
        record = workflowDAO.loadFromProforma(dialog.getSelectedProformaNo());
        loadRecordToFields();
        refreshGrid();
    }

    private void openCustomerLookup() {
        openLookup("Customer Lookup", lookupDAO::findCustomers, txtCustomerCode, txtCustomerName);
    }

    private void openBranchLookup() {
        openLookup("Branch Lookup", lookupDAO::findBranches, txtBranchCode, txtBranchName);
    }

    private void openSalesmanLookup() {
        openLookup("Salesman Lookup", lookupDAO::findSalesmen, txtSalesmanCode, txtSalesmanName);
    }

    private void openCurrencyLookup() {
        openLookup("Currency Lookup", lookupDAO::findCurrencies, txtCurrencyCode, txtCurrencyName);
    }

    private void openLookup(String title,
                            java.util.function.Function<String, java.util.List<LookupItem>> loader,
                            JTextField codeField,
                            JTextField nameField) {
        LookupDialog dialog = new LookupDialog(this, title, loader);
        dialog.setVisible(true);
        LookupItem item = dialog.getSelectedItem();
        if (item == null) {
            return;
        }
        codeField.setText(item.getCode());
        nameField.setText(item.getName());
    }

    private void addLine() {
        SalesInvoiceLineDialog dialog = new SalesInvoiceLineDialog(this, null);
        dialog.setVisible(true);
        if (!dialog.isSaved()) {
            return;
        }
        record.getLines().add(dialog.getLine());
        queueApplyFormula();
    }

    private void editLine() {
        int row = lineTable.getSelectedRow();
        if (row < 0 || row >= record.getLines().size()) {
            CisDialogs.showInfo(this, "Select a line first.");
            return;
        }
        SalesInvoiceLineDialog dialog = new SalesInvoiceLineDialog(this, record.getLines().get(row));
        dialog.setVisible(true);
        if (!dialog.isSaved()) {
            return;
        }
        record.getLines().set(row, dialog.getLine());
        queueApplyFormula();
    }

    private void deleteLine() {
        int row = lineTable.getSelectedRow();
        if (row < 0 || row >= record.getLines().size()) {
            CisDialogs.showInfo(this, "Select a line first.");
            return;
        }
        record.getLines().remove(row);
        queueApplyFormula();
    }

    private void saveRecord() {
        try {
            storeFieldsToRecord();
            workflowDAO.save(record);
            saved = true;
            CisDialogs.showInfo(this, "Sales Invoice saved.");
        } catch (Exception e) {
            CisDialogs.showError(this, e.getMessage());
        }
    }

    private void queueApplyFormula() {
        if (suppressUpdates) {
            return;
        }
        try {
            storeFieldsToRecord();
        } catch (Exception e) {
            return;
        }
        setBusy(true);
        new SwingWorker<SalesInvoiceRecord, Void>() {
            @Override
            protected SalesInvoiceRecord doInBackground() {
                return workflowDAO.applyFormula(record);
            }

            @Override
            protected void done() {
                try {
                    record = get();
                    loadRecordToFields();
                    refreshGrid();
                } catch (Exception e) {
                    CisDialogs.showError(SalesInvoiceEntryDialog.this, e.getMessage());
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void setBusy(boolean busy) {
        setCursor(busy ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR) : java.awt.Cursor.getDefaultCursor());
        chkApplyFormula.setEnabled(!busy);
        cboPricing.setEnabled(!busy);
    }

    private void loadRecordToFields() {
        suppressUpdates = true;
        txtProformaNo.setText(nullToBlank(record.getProformaNo()));
        txtInvoiceNo.setText(nullToBlank(record.getInvoiceNo()));
        txtInvoiceDate.setText(record.getInvoiceDate() == null ? "" : record.getInvoiceDate().toString());
        txtExchangeRate.setText(record.getExchangeRate().toPlainString());
        txtAwbNo.setText(record.getAwbNo());
        txtBroker.setText(record.getBroker());
        txtCustomerCode.setText(nullToBlank(record.getCustomerCode()));
        txtCustomerName.setText(nullToBlank(record.getCustomerName()));
        txtBranchCode.setText(nullToBlank(record.getBranchCode()));
        txtBranchName.setText(nullToBlank(record.getBranchName()));
        txtSalesmanCode.setText(nullToBlank(record.getSalesmanCode()));
        txtSalesmanName.setText(nullToBlank(record.getSalesmanName()));
        txtCurrencyCode.setText(nullToBlank(record.getCurrencyCode()));
        txtCurrencyName.setText(nullToBlank(record.getCurrencyName()));
        cboPricing.setSelectedItem(record.getPricingCode() == null ? "B" : record.getPricingCode().toUpperCase());
        chkApplyFormula.setSelected(record.isApplyFormula());
        chkConsumables.setSelected(record.isConsumables());
        txtBoxQty.setText(record.getBoxQty().toPlainString());
        txtKgs.setText(record.getTotalKgs().toPlainString());
        txtFishCost.setText(record.getFishCost().toPlainString());
        txtDiscAmount.setText(record.getDiscountAmount().toPlainString());
        txtMisc.setText(record.getMiscAmount().toPlainString());
        txtSsc.setText(record.getSscAmount().toPlainString());
        txtRate.setText(record.getRateAmount().toPlainString());
        txtVat.setText(record.getVatAmount().toPlainString());
        txtStamp.setText(record.getStampAmount().toPlainString());
        txtPreparedBy.setText(record.getPreparedBy());
        txtCheckedBy.setText(record.getCheckedBy());
        txtApprovedBy.setText(record.getApprovedByName());
        txtReceivedBy.setText(record.getReceivedBy());
        txtDiscountPercent.setText(record.getDiscountPercent().toPlainString());
        txtDoa.setText(record.getDoaAmount().toPlainString());
        txtRate2.setText(record.getRate2Amount().toPlainString());
        txtFreight.setText(record.getFreightAmount().toPlainString());
        txtPackingCharges.setText(record.getPackingCharges().toPlainString());
        txtProductSales.setText(record.getProductSalesAmount().toPlainString());
        txtTotalPayables.setText(record.getTotalPayables().toPlainString());
        suppressUpdates = false;
    }

    private void storeFieldsToRecord() {
        record.setProformaNo(txtProformaNo.getText().trim());
        record.setSourceProformaNo(txtProformaNo.getText().trim());
        record.setInvoiceNo(txtInvoiceNo.getText().trim());
        record.setInvoiceDate(txtInvoiceDate.getText().isBlank() ? LocalDate.now() : LocalDate.parse(txtInvoiceDate.getText().trim()));
        record.setExchangeRate(decimal(txtExchangeRate));
        record.setAwbNo(txtAwbNo.getText().trim());
        record.setBroker(txtBroker.getText().trim());
        record.setCustomerCode(txtCustomerCode.getText().trim());
        record.setCustomerName(txtCustomerName.getText().trim());
        record.setBranchCode(txtBranchCode.getText().trim());
        record.setBranchName(txtBranchName.getText().trim());
        record.setSalesmanCode(txtSalesmanCode.getText().trim());
        record.setSalesmanName(txtSalesmanName.getText().trim());
        record.setCurrencyCode(txtCurrencyCode.getText().trim().isBlank() ? "USD" : txtCurrencyCode.getText().trim());
        record.setCurrencyName(txtCurrencyName.getText().trim());
        record.setPricingCode(String.valueOf(cboPricing.getSelectedItem()));
        record.setApplyFormula(chkApplyFormula.isSelected());
        record.setConsumables(chkConsumables.isSelected());
        record.setBoxQty(decimal(txtBoxQty));
        record.setTotalKgs(decimal(txtKgs));
        record.setFishCost(decimal(txtFishCost));
        record.setMiscAmount(decimal(txtMisc));
        record.setSscAmount(decimal(txtSsc));
        record.setRateAmount(decimal(txtRate));
        record.setVatAmount(decimal(txtVat));
        record.setStampAmount(decimal(txtStamp));
        record.setPreparedBy(txtPreparedBy.getText().trim());
        record.setCheckedBy(txtCheckedBy.getText().trim());
        record.setApprovedByName(txtApprovedBy.getText().trim());
        record.setReceivedBy(txtReceivedBy.getText().trim());
        record.setDiscountPercent(decimal(txtDiscountPercent));
        record.setDoaAmount(decimal(txtDoa));
        record.setRate2Amount(decimal(txtRate2));
        record.setPackingCharges(decimal(txtPackingCharges));
    }

    private void refreshGrid() {
        lineModel.setRowCount(0);
        for (SalesInvoiceLine line : record.getLines()) {
            lineModel.addRow(new Object[]{
                    nullToBlank(line.getTransShipperCode()),
                    nullToBlank(line.getBoxNo()),
                    nullToBlank(line.getProductCode()),
                    nullToBlank(line.getDescription()),
                    line.isSpecial() ? nullToBlank(line.getSpecialValue()) : "",
                    Integer.toString(line.getQuantity()),
                    line.getSellingPrice().toPlainString(),
                    line.getTotalPrice().toPlainString()
            });
        }
    }

    private void openPrintPreview() {
        String text = """
                SALES INVOICE
                Invoice No: %s
                Customer: %s - %s
                Total Payables: %s
                Lines: %d
                """.formatted(
                nullToBlank(record.getInvoiceNo()),
                nullToBlank(record.getCustomerCode()),
                nullToBlank(record.getCustomerName()),
                record.getTotalPayables().toPlainString(),
                record.getLines().size());
        new SalesInvoicePrintDialog(SwingUtilities.getWindowAncestor(this), text).setVisible(true);
    }

    private BigDecimal decimal(JTextField field) {
        String text = field.getText().trim();
        return text.isBlank() ? BigDecimal.ZERO : new BigDecimal(text);
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}
