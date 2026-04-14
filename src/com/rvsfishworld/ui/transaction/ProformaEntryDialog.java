package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.dao.GenericDocumentDAO;
import com.rvsfishworld.dao.LookupDAO;
import com.rvsfishworld.model.LookupItem;
import com.rvsfishworld.model.ProformaLine;
import com.rvsfishworld.model.ProformaRecord;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.core.CisScale;
import com.rvsfishworld.ui.generic.LookupDialog;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class ProformaEntryDialog extends FoxProChildDialog {
    private final GenericDocumentDAO documentDAO = new GenericDocumentDAO();
    private final LookupDAO lookupDAO = new LookupDAO();
    private ProformaRecord record;
    private boolean saved;

    private final JTextField txtCustomerCode = field(10);
    private final JTextField txtCustomerName = field(28);
    private final JTextField txtSalesmanCode = field(8);
    private final JTextField txtSalesmanName = field(20);
    private final JTextField txtBranchCode = field(8);
    private final JTextField txtBranchName = field(20);
    private final JTextField txtProformaNo = field(12);
    private final JTextField txtDate = field(10);
    private final JTextField txtAdjustment = numericField(6);
    private final JTextField txtDiscountAmount = numericField(8);
    private final JTextField txtPackingCharges = numericField(8);
    private final JTextField txtGrandTotal = numericField(10);
    private final JTextField txtTotalPayables = numericField(10);
    private final JTextField txtIssuedBy = field(10);
    private final JTextField txtApprovedBy = field(10);

    private final DefaultTableModel lineModel = new DefaultTableModel(
            new Object[]{"Tran-Shipper", "BOX", "Product No.", "Description", "Qty. Order", "U/M", "Selling Price", "Total Price", "Supplier"}, 0) {
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
        super(owner, "Proforma", 900, 620);
        this.record = source == null ? new ProformaRecord() : source;
        if (this.record.getInvoiceDate() == null) {
            this.record.setInvoiceDate(LocalDate.now());
        }
        setContentPane(buildContent());
        loadRecord();
        refreshGrid();
    }

    public boolean isSaved() {
        return saved;
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(null);
        root.setBackground(FoxProTheme.PANEL);
        root.setPreferredSize(new Dimension(CisScale.scale(900), CisScale.scale(620)));

        addMainInfoBand(root);
        addHeaderFields(root);
        addLineGrid(root);
        addFooterBands(root);
        addBottomButtons(root);
        return root;
    }

    private void addMainInfoBand(JPanel root) {
        JLabel main = new JLabel("Main Info", SwingConstants.CENTER);
        main.setFont(FoxProTheme.FONT_BOLD);
        main.setBounds(s(210), s(10), s(245), s(18));
        root.add(main);
        JLabel other = new JLabel("Other Info", SwingConstants.CENTER);
        other.setFont(FoxProTheme.FONT_BOLD);
        other.setBounds(s(520), s(10), s(165), s(18));
        root.add(other);

        JPanel line1 = new JPanel();
        line1.setBackground(Color.GRAY);
        line1.setBounds(s(108), s(19), s(220), 1);
        root.add(line1);
        JPanel line2 = new JPanel();
        line2.setBackground(Color.GRAY);
        line2.setBounds(s(365), s(19), s(118), 1);
        root.add(line2);
        JPanel line3 = new JPanel();
        line3.setBackground(Color.GRAY);
        line3.setBounds(s(456), s(19), s(82), 1);
        root.add(line3);
        JPanel line4 = new JPanel();
        line4.setBackground(Color.GRAY);
        line4.setBounds(s(614), s(19), s(92), 1);
        root.add(line4);
    }

    private void addHeaderFields(JPanel root) {
        addLabel(root, "Customer", 22, 34, 52);
        addCodeRow(root, txtCustomerCode, txtCustomerName, 84, 30, this::openCustomerLookup);

        addLabel(root, "Salesman", 22, 64, 52);
        addCodeRow(root, txtSalesmanCode, txtSalesmanName, 84, 60, this::openSalesmanLookup);

        addLabel(root, "Branch", 22, 94, 52);
        addCodeRow(root, txtBranchCode, txtBranchName, 84, 90, this::openBranchLookup);

        addLabel(root, "Proforma No.", 540, 34, 64);
        placeField(root, txtProformaNo, 610, 30, 92, 22);

        addLabel(root, "Date", 540, 64, 64);
        placeField(root, txtDate, 610, 60, 92, 22);

        addLabel(root, "Adjustment", 540, 94, 64);
        placeField(root, txtAdjustment, 610, 90, 70, 22);
        JLabel pct = new JLabel("%");
        pct.setFont(FoxProTheme.FONT);
        pct.setBounds(s(686), s(92), s(14), s(18));
        root.add(pct);
    }

    private void addLineGrid(JPanel root) {
        FoxProTheme.styleTable(lineTable);
        lineTable.setRowHeight(CisScale.scale(18));
        lineTable.setSelectionBackground(new Color(255, 210, 210));
        lineTable.setSelectionForeground(Color.BLACK);
        int[] widths = {70, 42, 72, 230, 58, 44, 72, 82, 80};
        for (int i = 0; i < widths.length; i++) {
            lineTable.getColumnModel().getColumn(i).setPreferredWidth(CisScale.scale(widths[i]));
        }
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        lineTable.getColumnModel().getColumn(4).setCellRenderer(right);
        lineTable.getColumnModel().getColumn(6).setCellRenderer(right);
        lineTable.getColumnModel().getColumn(7).setCellRenderer(right);

        JScrollPane scrollPane = new JScrollPane(lineTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBounds(s(18), s(126), s(860), s(292));
        root.add(scrollPane);
    }

    private void addFooterBands(JPanel root) {
        JLabel packingLookup = new JLabel("Packing List Lookup");
        packingLookup.setFont(FoxProTheme.FONT);
        packingLookup.setBounds(s(28), s(430), s(118), s(18));
        root.add(packingLookup);

        JButton insertAbove = FoxProTheme.createButton("INSERT ABOVE *");
        insertAbove.setBounds(s(18), s(448), s(120), s(24));
        insertAbove.addActionListener(e -> insertLineAtSelection(false));
        root.add(insertAbove);

        JButton insertBlank = FoxProTheme.createButton("INSERT BLANK ABOVE");
        insertBlank.setBounds(s(18), s(478), s(120), s(24));
        insertBlank.addActionListener(e -> insertLineAtSelection(true));
        root.add(insertBlank);

        addLabel(root, "Discount", 168, 454, 48);
        placeField(root, txtAdjustment, 222, 450, 52, 22);
        txtAdjustment.setBounds(s(222), s(450), s(52), s(22));
        placeReadOnly(root, txtDiscountAmount, 278, 450, 86, 22);

        addLabel(root, "Grand Total", 530, 454, 56);
        placeReadOnly(root, txtGrandTotal, 620, 450, 92, 22);
        addLabel(root, "Packing Charges", 530, 479, 76);
        placeField(root, txtPackingCharges, 620, 475, 92, 22);
        addLabel(root, "Total Payables", 530, 504, 76);
        placeReadOnly(root, txtTotalPayables, 620, 500, 92, 22);

        JButton printLabels = FoxProTheme.createButton("Print Labels");
        printLabels.setBounds(s(785), s(445), s(78), s(54));
        printLabels.addActionListener(e -> openPrintPreview());
        root.add(printLabels);

        addLabel(root, "Issued by :", 18, 535, 50);
        placeField(root, txtIssuedBy, 78, 531, 120, 22);
        addLabel(root, "Approved by :", 468, 535, 62);
        placeField(root, txtApprovedBy, 538, 531, 165, 22);
    }

    private void addBottomButtons(JPanel root) {
        addActionButton(root, "Add", 188, 560, e -> addLine());
        addActionButton(root, "Edit", 288, 560, e -> editLine());
        addActionButton(root, "Delete", 388, 560, e -> deleteLine());
        addActionButton(root, "Save", 488, 560, e -> saveRecord());
        addActionButton(root, "Cancel/Recall", 590, 560, e -> dispose());
        addActionButton(root, "Exit", 692, 560, e -> dispose());
    }

    private void addActionButton(JPanel root, String text, int x, int y, java.awt.event.ActionListener listener) {
        JButton button = FoxProTheme.createButton(text);
        button.setBounds(s(x), s(y), s(84), s(24));
        button.addActionListener(listener);
        root.add(button);
    }

    private void addLabel(JPanel root, String text, int x, int y, int width) {
        JLabel label = new JLabel(text);
        label.setFont(FoxProTheme.FONT);
        label.setBounds(s(x), s(y), s(width), s(18));
        root.add(label);
    }

    private void addCodeRow(JPanel root, JTextField codeField, JTextField nameField, int x, int y, Runnable lookupAction) {
        placeField(root, codeField, x, y, 62, 22);
        JButton lookup = FoxProTheme.createLookupButton();
        lookup.setBounds(s(x + 66), s(y), s(24), s(22));
        lookup.addActionListener(e -> lookupAction.run());
        root.add(lookup);
        placeField(root, nameField, x + 94, y, 272, 22);
        nameField.setEditable(false);
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

    private void openCustomerLookup() {
        openLookup("Customer Lookup", lookupDAO::findCustomers, txtCustomerCode, txtCustomerName);
    }

    private void openSalesmanLookup() {
        openLookup("Salesman Lookup", lookupDAO::findSalesmen, txtSalesmanCode, txtSalesmanName);
    }

    private void openBranchLookup() {
        openLookup("Branch Lookup", lookupDAO::findBranches, txtBranchCode, txtBranchName);
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

    private void insertLineAtSelection(boolean keepBlankShell) {
        int index = lineTable.getSelectedRow();
        if (index < 0) {
            index = record.getLines().size();
        }
        ProformaLineDialog dialog = new ProformaLineDialog(this, keepBlankShell ? new ProformaLine() : null);
        dialog.setVisible(true);
        if (!dialog.isSaved()) {
            return;
        }
        record.getLines().add(index, dialog.getLine());
        refreshGrid();
        selectRow(index);
    }

    private void addLine() {
        ProformaLineDialog dialog = new ProformaLineDialog(this, null);
        dialog.setVisible(true);
        if (!dialog.isSaved()) {
            return;
        }
        record.getLines().add(dialog.getLine());
        refreshGrid();
        selectRow(record.getLines().size() - 1);
    }

    private void editLine() {
        int row = lineTable.getSelectedRow();
        if (row < 0 || row >= record.getLines().size()) {
            CisDialogs.showInfo(this, "Select a line first.");
            return;
        }
        ProformaLineDialog dialog = new ProformaLineDialog(this, record.getLines().get(row));
        dialog.setVisible(true);
        if (!dialog.isSaved()) {
            return;
        }
        record.getLines().set(row, dialog.getLine());
        refreshGrid();
        selectRow(row);
    }

    private void deleteLine() {
        int row = lineTable.getSelectedRow();
        if (row < 0 || row >= record.getLines().size()) {
            CisDialogs.showInfo(this, "Select a line first.");
            return;
        }
        record.getLines().remove(row);
        refreshGrid();
        if (!record.getLines().isEmpty()) {
            selectRow(Math.min(row, record.getLines().size() - 1));
        }
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
        txtIssuedBy.setText(defaultString(record.getPreparedBy()));
        txtApprovedBy.setText(defaultString(record.getApprovedBy()));

        if (txtBranchName.getText().isBlank() && !txtBranchCode.getText().isBlank()) {
            LookupItem branch = lookupDAO.findBranchByCode(txtBranchCode.getText().trim());
            if (branch != null) {
                txtBranchName.setText(branch.getName());
            }
        }
    }

    private void refreshGrid() {
        lineModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (ProformaLine line : record.getLines()) {
            line.recompute();
            total = total.add(money(line.getTotalPrice()));
            lineModel.addRow(new Object[]{
                    defaultString(line.getTransShipperCode()),
                    defaultString(line.getBoxNo()),
                    defaultString(line.getProductCode()),
                    defaultString(line.getDescription()),
                    Integer.toString(line.getQuantity()),
                    "PCS.",
                    money(line.getPrice()).toPlainString(),
                    money(line.getTotalPrice()).toPlainString(),
                    defaultString(line.getSupplierCode())
            });
        }
        BigDecimal discountAmount = money(total.multiply(parseDecimal(txtAdjustment.getText()))
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        BigDecimal packing = money(parseDecimal(txtPackingCharges.getText()));
        txtDiscountAmount.setText(discountAmount.toPlainString());
        txtGrandTotal.setText(money(total).toPlainString());
        txtTotalPayables.setText(money(total.subtract(discountAmount).add(packing)).toPlainString());
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
            record.setAdjustmentPercent(parseDecimal(txtAdjustment.getText()));
            record.setPackingCharges(parseDecimal(txtPackingCharges.getText()));
            record.setPreparedBy(txtIssuedBy.getText().trim());
            record.setApprovedBy(txtApprovedBy.getText().trim());
            documentDAO.saveProforma(record);
            saved = true;
            dispose();
        } catch (Exception e) {
            CisDialogs.showError(this, "Unable to save Proforma: " + e.getMessage());
        }
    }

    private void openPrintPreview() {
        ProformaRecord preview = record;
        preview.setAdjustmentPercent(parseDecimal(txtAdjustment.getText()));
        preview.setPackingCharges(parseDecimal(txtPackingCharges.getText()));
        new ProformaPrintDialog(this, preview).setVisible(true);
    }

    private void selectRow(int row) {
        if (row < 0 || row >= lineTable.getRowCount()) {
            return;
        }
        lineTable.setRowSelectionInterval(row, row);
        lineTable.scrollRectToVisible(lineTable.getCellRect(row, 0, true));
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

    private int s(int value) {
        return CisScale.scale(value);
    }
}
