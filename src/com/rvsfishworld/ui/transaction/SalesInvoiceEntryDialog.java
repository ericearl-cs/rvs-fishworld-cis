package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.dao.LookupDAO;
import com.rvsfishworld.dao.SalesWorkflowDAO;
import com.rvsfishworld.model.LookupItem;
import com.rvsfishworld.model.SalesInvoiceLine;
import com.rvsfishworld.model.SalesInvoiceRecord;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.core.CisScale;
import com.rvsfishworld.ui.generic.LookupDialog;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class SalesInvoiceEntryDialog extends FoxProChildDialog {
    private static final String[] PRICING_OPTIONS = {
            "A", "B - US/SQ/EU", "C", "D", "E", "F", "G", "S", "L"
    };

    private final SalesWorkflowDAO workflowDAO = new SalesWorkflowDAO();
    private final LookupDAO lookupDAO = new LookupDAO();
    private SalesInvoiceRecord record;
    private boolean saved;
    private boolean suppressUpdates;

    private final JTextField txtProformaNo = field(10);
    private final JTextField txtInvoiceNo = field(10);
    private final JTextField txtInvoiceDate = field(10);
    private final JTextField txtExchangeRate = numericField(8);
    private final JTextField txtAwbNo = field(12);
    private final JTextField txtBroker = field(16);
    private final JTextField txtCustomerCode = field(10);
    private final JTextField txtCustomerName = field(28);
    private final JTextField txtBranchCode = field(8);
    private final JTextField txtBranchName = field(22);
    private final JTextField txtSalesmanCode = field(8);
    private final JTextField txtSalesmanName = field(22);
    private final JTextField txtCurrencyCode = field(6);
    private final JTextField txtCurrencyName = field(18);
    private final JComboBox<String> cboPricing = new JComboBox<>(PRICING_OPTIONS);
    private final JCheckBox chkApplyFormula = new JCheckBox("Apply Formula");
    private final JCheckBox chkConsumables = new JCheckBox("Consumables");
    private final JTextField txtBoxQty = numericField(8);
    private final JTextField txtKgs = numericField(8);
    private final JTextField txtFishCost = numericField(8);
    private final JTextField txtDiscAmount = numericField(8);
    private final JTextField txtMisc = numericField(8);
    private final JTextField txtSsc = numericField(8);
    private final JTextField txtRate = numericField(8);
    private final JTextField txtVat = numericField(8);
    private final JTextField txtStamp = numericField(8);
    private final JTextField txtPreparedBy = field(12);
    private final JTextField txtCheckedBy = field(12);
    private final JTextField txtApprovedBy = field(12);
    private final JTextField txtReceivedBy = field(12);
    private final JTextField txtDiscountPercent = numericField(8);
    private final JTextField txtDoa = numericField(8);
    private final JTextField txtRate2 = numericField(8);
    private final JTextField txtFreight = numericField(8);
    private final JTextField txtPackingCharges = numericField(8);
    private final JTextField txtProductSales = numericField(10);
    private final JTextField txtTotalPayables = numericField(10);

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
        super(owner, "Adding Of Product Sales", 1040, 690);
        this.record = source == null ? new SalesInvoiceRecord() : source;
        if (this.record.getInvoiceDate() == null) {
            this.record.setInvoiceDate(LocalDate.now());
        }
        setContentPane(buildContent());
        bindUi();
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
        JPanel root = new JPanel(null);
        root.setBackground(FoxProTheme.PANEL);
        root.setPreferredSize(new Dimension(CisScale.scale(1040), CisScale.scale(690)));

        buildHeader(root);
        buildTotalsStrip(root);
        buildGrid(root);
        buildFooter(root);
        return root;
    }

    private void bindUi() {
        chkApplyFormula.setBackground(FoxProTheme.PANEL);
        chkConsumables.setBackground(FoxProTheme.PANEL);
        cboPricing.setFont(FoxProTheme.FONT);
        cboPricing.addActionListener(e -> queueApplyFormula());
        chkApplyFormula.addActionListener(e -> queueApplyFormula());
    }

    private void buildHeader(JPanel root) {
        addLabel(root, "Proforma No.", 16, 18, 64);
        placeField(root, txtProformaNo, 92, 14, 96, 22);
        addLookupButton(root, 191, 14, this::openProformaLookup);

        addLabel(root, "Invoice No.", 16, 46, 64);
        placeField(root, txtInvoiceNo, 92, 42, 96, 22);

        addLabel(root, "Invoice Date", 16, 74, 64);
        placeField(root, txtInvoiceDate, 92, 70, 96, 22);

        addLabel(root, "Exchange Rate", 16, 102, 74);
        placeField(root, txtExchangeRate, 92, 98, 96, 22);

        addLabel(root, "AWB #", 16, 130, 42);
        placeField(root, txtAwbNo, 92, 126, 128, 22);

        addLabel(root, "BROKER", 16, 158, 48);
        placeField(root, txtBroker, 92, 154, 128, 22);

        addLabel(root, "Customer", 462, 18, 54);
        placeField(root, txtCustomerCode, 530, 14, 58, 22);
        addLookupButton(root, 591, 14, this::openCustomerLookup);
        placeReadOnly(root, txtCustomerName, 619, 14, 305, 22);

        addLabel(root, "Branch", 462, 46, 54);
        placeField(root, txtBranchCode, 530, 42, 58, 22);
        addLookupButton(root, 591, 42, this::openBranchLookup);
        placeReadOnly(root, txtBranchName, 619, 42, 305, 22);

        addLabel(root, "Salesman", 462, 74, 54);
        placeField(root, txtSalesmanCode, 530, 70, 58, 22);
        addLookupButton(root, 591, 70, this::openSalesmanLookup);
        placeReadOnly(root, txtSalesmanName, 619, 70, 305, 22);

        addLabel(root, "Currency", 462, 102, 54);
        placeField(root, txtCurrencyCode, 530, 98, 40, 22);
        addLookupButton(root, 573, 98, this::openCurrencyLookup);
        placeReadOnly(root, txtCurrencyName, 601, 98, 323, 22);

        addLabel(root, "Pricing", 462, 136, 48);
        cboPricing.setBounds(s(530), s(130), s(175), s(24));
        root.add(cboPricing);
        chkApplyFormula.setBounds(s(715), s(132), s(100), s(20));
        root.add(chkApplyFormula);
        chkConsumables.setBounds(s(816), s(132), s(110), s(20));
        root.add(chkConsumables);
    }

    private void buildTotalsStrip(JPanel root) {
        String[] labels = {"BOX QTY.", "KGS.", "FISH COST", "DISC", "MISC.", "SSC", "RATE", "VAT", "STAMP"};
        JTextField[] fields = {txtBoxQty, txtKgs, txtFishCost, txtDiscAmount, txtMisc, txtSsc, txtRate, txtVat, txtStamp};
        int x = 16;
        for (int i = 0; i < labels.length; i++) {
            addCenteredLabel(root, labels[i], x, 198, 94);
            placeField(root, fields[i], x, 218, 92, 22);
            x += 102;
        }
    }

    private void buildGrid(JPanel root) {
        FoxProTheme.styleTable(lineTable);
        lineTable.setRowHeight(CisScale.scale(18));
        lineTable.setSelectionBackground(new Color(246, 142, 255));
        lineTable.setSelectionForeground(Color.BLACK);
        int[] widths = {84, 38, 82, 250, 60, 62, 78, 86};
        for (int i = 0; i < widths.length; i++) {
            lineTable.getColumnModel().getColumn(i).setPreferredWidth(CisScale.scale(widths[i]));
        }
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        lineTable.getColumnModel().getColumn(5).setCellRenderer(right);
        lineTable.getColumnModel().getColumn(6).setCellRenderer(right);
        lineTable.getColumnModel().getColumn(7).setCellRenderer(right);

        JScrollPane scrollPane = new JScrollPane(lineTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBounds(s(16), s(248), s(908), s(270));
        root.add(scrollPane);
    }

    private void buildFooter(JPanel root) {
        addLabel(root, "Prep.by", 20, 545, 46);
        placeField(root, txtPreparedBy, 76, 541, 88, 22);
        addLabel(root, "Check by", 20, 573, 46);
        placeField(root, txtCheckedBy, 76, 569, 88, 22);
        addLabel(root, "App.by", 20, 601, 46);
        placeField(root, txtApprovedBy, 76, 597, 88, 22);
        addLabel(root, "Rec. by", 20, 629, 46);
        placeField(root, txtReceivedBy, 76, 625, 88, 22);

        JButton boxTotal = FoxProTheme.createButton("BOX TOTAL");
        boxTotal.setBounds(s(16), s(654), s(104), s(24));
        boxTotal.addActionListener(e -> CisDialogs.showInfo(this, "Current box quantity: " + txtBoxQty.getText()));
        root.add(boxTotal);

        JButton findBox = FoxProTheme.createButton("Find Box No.");
        findBox.setBounds(s(398), s(520), s(98), s(24));
        findBox.addActionListener(e -> findBoxNumber());
        root.add(findBox);

        addRightTotal(root, "Discount", txtDiscountPercent, 624, 514);
        addRightTotal(root, "DOA", txtDoa, 624, 542);
        addRightTotal(root, "RATE", txtRate2, 624, 570);
        addRightTotal(root, "FREIGHT", txtFreight, 624, 598);
        addRightTotal(root, "Packing Charges", txtPackingCharges, 560, 626);
        addRightTotal(root, "PRODUCT SALES", txtProductSales, 558, 654);
        addRightTotal(root, "TOTAL PAYABLES", txtTotalPayables, 550, 682);
        txtFreight.setEditable(false);
        txtProductSales.setEditable(false);
        txtTotalPayables.setEditable(false);

        JButton printLabels = FoxProTheme.createButton("Print\nLabels");
        printLabels.setBounds(s(866), s(548), s(58), s(72));
        printLabels.addActionListener(e -> openPrintPreview());
        root.add(printLabels);

        addActionButton(root, "Add", 330, 654, e -> addLine());
        addActionButton(root, "Edit", 420, 654, e -> editLine());
        addActionButton(root, "Delete", 510, 654, e -> deleteLine());
        addActionButton(root, "Save", 600, 654, e -> saveRecord());
        addActionButton(root, "Exit", 690, 654, e -> dispose());
        addActionButton(root, "Cancel/Recall", 780, 654, e -> dispose());
    }

    private void addRightTotal(JPanel root, String label, JTextField field, int labelX, int y) {
        addLabel(root, label, labelX, y + 4, 72);
        placeField(root, field, 760, y, 82, 22);
    }

    private void addActionButton(JPanel root, String text, int x, int y, java.awt.event.ActionListener listener) {
        JButton button = FoxProTheme.createButton(text);
        button.setBounds(s(x), s(y), s(82), s(24));
        button.addActionListener(listener);
        root.add(button);
    }

    private void addLabel(JPanel root, String text, int x, int y, int width) {
        JLabel label = new JLabel(text);
        label.setFont(FoxProTheme.FONT);
        label.setBounds(s(x), s(y), s(width), s(18));
        root.add(label);
    }

    private void addCenteredLabel(JPanel root, String text, int x, int y, int width) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(FoxProTheme.FONT);
        label.setBounds(s(x), s(y), s(width), s(16));
        root.add(label);
    }

    private JTextField field(int columns) {
        return FoxProTheme.createTextField(columns);
    }

    private JTextField numericField(int columns) {
        JTextField field = FoxProTheme.createTextField(columns);
        field.setHorizontalAlignment(SwingConstants.RIGHT);
        return field;
    }

    private void placeField(JPanel root, JTextField field, int x, int y, int width, int height) {
        field.setBounds(s(x), s(y), s(width), s(height));
        root.add(field);
    }

    private void placeReadOnly(JPanel root, JTextField field, int x, int y, int width, int height) {
        field.setEditable(false);
        placeField(root, field, x, y, width, height);
    }

    private void addLookupButton(JPanel root, int x, int y, Runnable action) {
        JButton button = FoxProTheme.createLookupButton();
        button.setBounds(s(x), s(y), s(24), s(22));
        button.addActionListener(e -> action.run());
        root.add(button);
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
            dispose();
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
        txtAwbNo.setText(nullToBlank(record.getAwbNo()));
        txtBroker.setText(nullToBlank(record.getBroker()));
        txtCustomerCode.setText(nullToBlank(record.getCustomerCode()));
        txtCustomerName.setText(nullToBlank(record.getCustomerName()));
        txtBranchCode.setText(nullToBlank(record.getBranchCode()));
        txtBranchName.setText(nullToBlank(record.getBranchName()));
        txtSalesmanCode.setText(nullToBlank(record.getSalesmanCode()));
        txtSalesmanName.setText(nullToBlank(record.getSalesmanName()));
        txtCurrencyCode.setText(nullToBlank(record.getCurrencyCode()));
        txtCurrencyName.setText(nullToBlank(record.getCurrencyName()));
        selectPricingDisplay(record.getPricingCode());
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
        txtPreparedBy.setText(nullToBlank(record.getPreparedBy()));
        txtCheckedBy.setText(nullToBlank(record.getCheckedBy()));
        txtApprovedBy.setText(nullToBlank(record.getApprovedByName()));
        txtReceivedBy.setText(nullToBlank(record.getReceivedBy()));
        txtDiscountPercent.setText(record.getDiscountPercent().toPlainString());
        txtDoa.setText(record.getDoaAmount().toPlainString());
        txtRate2.setText(record.getRate2Amount().toPlainString());
        txtFreight.setText(record.getFreightAmount().toPlainString());
        txtPackingCharges.setText(record.getPackingCharges().toPlainString());
        txtProductSales.setText(record.getProductSalesAmount().toPlainString());
        txtTotalPayables.setText(record.getTotalPayables().toPlainString());
        fillDerivedNames();
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
        record.setPricingCode(extractPricingCode());
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
        if (lineModel.getRowCount() > 0) {
            int row = lineModel.getRowCount() - 1;
            lineTable.setRowSelectionInterval(row, row);
            lineTable.scrollRectToVisible(lineTable.getCellRect(row, 0, true));
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

    private void findBoxNumber() {
        String box = javax.swing.JOptionPane.showInputDialog(this, "Find Box No.");
        if (box == null || box.isBlank()) {
            return;
        }
        String lookFor = box.trim().toUpperCase();
        for (int i = 0; i < record.getLines().size(); i++) {
            SalesInvoiceLine line = record.getLines().get(i);
            if (nullToBlank(line.getBoxNo()).toUpperCase().contains(lookFor)) {
                lineTable.setRowSelectionInterval(i, i);
                lineTable.scrollRectToVisible(lineTable.getCellRect(i, 0, true));
                return;
            }
        }
        CisDialogs.showInfo(this, "No matching box number found.");
    }

    private void fillDerivedNames() {
        if (txtBranchName.getText().isBlank() && !txtBranchCode.getText().isBlank()) {
            LookupItem branch = lookupDAO.findBranchByCode(txtBranchCode.getText().trim());
            if (branch != null) {
                txtBranchName.setText(branch.getName());
            }
        }
        if (txtCurrencyName.getText().isBlank() && !txtCurrencyCode.getText().isBlank()) {
            LookupItem currency = lookupDAO.findCurrencyByCode(txtCurrencyCode.getText().trim());
            if (currency != null) {
                txtCurrencyName.setText(currency.getName());
            }
        }
    }

    private void selectPricingDisplay(String pricingCode) {
        String code = pricingCode == null || pricingCode.isBlank() ? "B" : pricingCode.substring(0, 1).toUpperCase();
        for (String option : PRICING_OPTIONS) {
            if (option.startsWith(code)) {
                cboPricing.setSelectedItem(option);
                return;
            }
        }
        cboPricing.setSelectedItem("B - US/SQ/EU");
    }

    private String extractPricingCode() {
        Object selected = cboPricing.getSelectedItem();
        if (selected == null) {
            return "B";
        }
        String value = selected.toString().trim();
        return value.isEmpty() ? "B" : value.substring(0, 1).toUpperCase();
    }

    private BigDecimal decimal(JTextField field) {
        String text = field.getText().trim();
        return text.isBlank() ? BigDecimal.ZERO : new BigDecimal(text);
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private int s(int value) {
        return CisScale.scale(value);
    }
}
