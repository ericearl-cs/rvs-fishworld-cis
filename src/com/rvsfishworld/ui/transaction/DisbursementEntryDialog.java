package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.dao.DisbursementDAO;
import com.rvsfishworld.dao.ReceivingDAO;
import com.rvsfishworld.model.DisbursementHeader;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.core.CisScale;
import com.rvsfishworld.ui.core.CisTheme;
import java.awt.Window;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class DisbursementEntryDialog extends FoxProChildDialog {
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final DisbursementDAO disbursementDAO = new DisbursementDAO();
    private final JTextField txtSupplierCode = CisTheme.createTextField(8);
    private final JTextField txtSupplierName = CisTheme.createTextField(24);
    private final JTextField txtRrNo = CisTheme.createTextField(10);
    private final JTextField txtDate = CisTheme.createTextField(10);
    private final JTextArea txtSummary = new JTextArea();
    private final JTextField txtFishPurchase = decimalField();
    private final JTextField txtCommissionPct = decimalField();
    private final JTextField txtCommissionAmt = decimalField();
    private final JTextField txtTruckingPct = decimalField();
    private final JTextField txtTruckingAmt = decimalField();
    private final JTextField txtCashOnHand = decimalField();
    private final JTextField txtDebitTotal = decimalField();
    private final JTextField txtCreditTotal = decimalField();
    private final JTextField txtPreparedBy = CisTheme.createTextField(12);
    private final JTextField txtCheckBy = CisTheme.createTextField(12);
    private final JTextField txtApprovedBy = CisTheme.createTextField(12);
    private final JTextField txtReceivedBy = CisTheme.createTextField(12);
    private long selectedReceivingId;

    public DisbursementEntryDialog(Window owner) {
        this(owner, null);
    }

    public DisbursementEntryDialog(Window owner, ReceivingDAO.BrowseRow row) {
        super(owner, "C.V. Entry", CisScale.scale(470), CisScale.scale(330));
        setResizable(false);
        txtDate.setText(LocalDate.now().format(DISPLAY_DATE));
        txtCommissionPct.setText("5.00");
        txtTruckingPct.setText("5.00");
        txtSummary.setFont(FoxProTheme.FONT);
        txtSummary.setLineWrap(true);
        txtSummary.setWrapStyleWord(true);
        txtSummary.setText("PAYMENT OF VARIOUS LIVE MARINE TROPICAL FISHES\nAS PER ATTACHED RECEIVING PRODUCTS");
        setContentPane(buildContent());
        if (row != null) {
            loadRow(row);
        }
        registerRecalc(txtCommissionPct);
        registerRecalc(txtTruckingPct);
        recalc();
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(null);
        root.setBackground(CisTheme.PANEL);

        addReadOnlyField(root, "Supplier", txtSupplierCode, 18, 18, 56, 44);
        JButton lookup = CisTheme.createFormButton("...", CisScale.scale(24), CisScale.scale(22));
        lookup.setBounds(CisScale.scale(127), CisScale.scale(16), CisScale.scale(24), CisScale.scale(22));
        lookup.addActionListener(e -> openRrLookup());
        root.add(lookup);
        txtSupplierName.setBounds(CisScale.scale(156), CisScale.scale(16), CisScale.scale(168), CisScale.scale(22));
        CisTheme.styleReadOnlyField(txtSupplierName);
        root.add(txtSupplierName);

        addReadOnlyField(root, "R.R. No.", txtRrNo, 344, 18, 40, 72);
        addReadOnlyField(root, "Date", txtDate, 344, 42, 40, 72);

        JLabel sum = new JLabel("SUM");
        sum.setBounds(CisScale.scale(18), CisScale.scale(45), CisScale.scale(24), CisScale.scale(18));
        root.add(sum);

        JScrollPane summaryScroll = new JScrollPane(txtSummary);
        CisTheme.styleGridScrollPane(summaryScroll);
        summaryScroll.setBounds(CisScale.scale(18), CisScale.scale(66), CisScale.scale(262), CisScale.scale(86));
        root.add(summaryScroll);

        buildDebitCreditBlock(root);
        buildSignatories(root);

        root.add(actionButton("Save", 98, 286, this::save));
        root.add(actionButton("View", 170, 286, this::showPreview));
        root.add(actionButton("Print", 242, 286, this::showPreview));
        root.add(actionButton("Exit", 314, 286, this::dispose));
        return root;
    }

    private void buildDebitCreditBlock(JPanel root) {
        JPanel block = new JPanel(null);
        block.setBackground(CisTheme.PANEL);
        block.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        block.setBounds(CisScale.scale(18), CisScale.scale(166), CisScale.scale(262), CisScale.scale(98));

        JLabel debit = new JLabel("DEBIT", JLabel.CENTER);
        debit.setBounds(CisScale.scale(118), CisScale.scale(2), CisScale.scale(62), CisScale.scale(16));
        block.add(debit);
        JLabel credit = new JLabel("CREDIT", JLabel.CENTER);
        credit.setBounds(CisScale.scale(182), CisScale.scale(2), CisScale.scale(62), CisScale.scale(16));
        block.add(credit);

        addMoneyRow(block, "Fish Purchase", txtFishPurchase, null, null, 18);
        addMoneyRow(block, "Fish Commision", txtCommissionAmt, txtCommissionPct, "%", 36);
        addMoneyRow(block, "Trucking", txtTruckingAmt, txtTruckingPct, "%", 54);
        addCreditRow(block, "Cash on Hand", txtCashOnHand, 72);

        txtDebitTotal.setBounds(CisScale.scale(118), CisScale.scale(90), CisScale.scale(62), CisScale.scale(18));
        txtCreditTotal.setBounds(CisScale.scale(182), CisScale.scale(90), CisScale.scale(62), CisScale.scale(18));
        root.add(block);
        block.add(txtDebitTotal);
        block.add(txtCreditTotal);
        CisTheme.styleReadOnlyField(txtDebitTotal);
        CisTheme.styleReadOnlyField(txtCreditTotal);
    }

    private void buildSignatories(JPanel root) {
        addReadOnlyField(root, "Prepared by", txtPreparedBy, 302, 186, 56, 78, false);
        addReadOnlyField(root, "Check by", txtCheckBy, 302, 212, 56, 78, false);
        addReadOnlyField(root, "Approved by", txtApprovedBy, 302, 238, 56, 78, false);
        addReadOnlyField(root, "Received by", txtReceivedBy, 302, 264, 56, 78, false);
    }

    private void addMoneyRow(JPanel block, String label, JTextField amountField, JTextField pctField, String suffix, int y) {
        JLabel rowLabel = new JLabel(label);
        rowLabel.setBounds(CisScale.scale(8), CisScale.scale(y), CisScale.scale(80), CisScale.scale(16));
        block.add(rowLabel);
        if (pctField != null) {
            pctField.setBounds(CisScale.scale(84), CisScale.scale(y - 2), CisScale.scale(30), CisScale.scale(18));
            block.add(pctField);
            JLabel pct = new JLabel(suffix);
            pct.setBounds(CisScale.scale(114), CisScale.scale(y), CisScale.scale(8), CisScale.scale(16));
            block.add(pct);
        }
        amountField.setBounds(CisScale.scale(118), CisScale.scale(y - 2), CisScale.scale(62), CisScale.scale(18));
        block.add(amountField);
        CisTheme.styleReadOnlyField(amountField);
    }

    private void addCreditRow(JPanel block, String label, JTextField amountField, int y) {
        JLabel rowLabel = new JLabel(label);
        rowLabel.setBounds(CisScale.scale(8), CisScale.scale(y), CisScale.scale(80), CisScale.scale(16));
        block.add(rowLabel);
        amountField.setBounds(CisScale.scale(182), CisScale.scale(y - 2), CisScale.scale(62), CisScale.scale(18));
        block.add(amountField);
        CisTheme.styleReadOnlyField(amountField);
    }

    private void addReadOnlyField(JPanel root, String label, JTextField field, int x, int y, int labelWidth, int fieldWidth) {
        addReadOnlyField(root, label, field, x, y, labelWidth, fieldWidth, true);
    }

    private void addReadOnlyField(JPanel root, String label, JTextField field, int x, int y, int labelWidth, int fieldWidth, boolean readOnly) {
        JLabel jLabel = new JLabel(label);
        jLabel.setBounds(CisScale.scale(x), CisScale.scale(y), CisScale.scale(labelWidth), CisScale.scale(18));
        root.add(jLabel);

        field.setBounds(CisScale.scale(x + labelWidth + 6), CisScale.scale(y - 2), CisScale.scale(fieldWidth), CisScale.scale(22));
        if (readOnly) {
            CisTheme.styleReadOnlyField(field);
        }
        root.add(field);
    }

    private JButton actionButton(String text, int x, int y, Runnable action) {
        JButton button = CisTheme.createFormButton(text, CisScale.scale(58), CisScale.scale(22));
        button.setBounds(CisScale.scale(x), CisScale.scale(y), CisScale.scale(58), CisScale.scale(22));
        button.addActionListener(e -> action.run());
        return button;
    }

    private JTextField decimalField() {
        JTextField field = CisTheme.createTextField(8);
        field.setHorizontalAlignment(JTextField.RIGHT);
        return field;
    }

    private void loadRow(ReceivingDAO.BrowseRow row) {
        selectedReceivingId = row.getReceivingId();
        txtSupplierCode.setText(row.getSupplierCode());
        txtSupplierName.setText(row.getSupplierName());
        txtRrNo.setText(row.getRrNo());
        txtFishPurchase.setText(format(row.getFishPurchase()));
        txtCashOnHand.setText(format(row.getBalance()));
        txtDebitTotal.setText(format(row.getBalance()));
        txtCreditTotal.setText(format(row.getBalance()));
        txtReceivedBy.setText(row.getSupplierName());
        recalc();
    }

    private void openRrLookup() {
        DisbursementRrLookupDialog dialog = new DisbursementRrLookupDialog(this);
        dialog.setVisible(true);
        ReceivingDAO.BrowseRow row = dialog.getSelectedRow();
        if (row != null) {
            loadRow(row);
        }
    }

    private void registerRecalc(JTextField field) {
        field.addActionListener(e -> recalc());
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                recalc();
            }
        });
    }

    private void recalc() {
        BigDecimal purchase = decimal(txtFishPurchase.getText());
        BigDecimal commPct = decimal(txtCommissionPct.getText());
        BigDecimal truckingPct = decimal(txtTruckingPct.getText());
        BigDecimal commission = purchase.multiply(commPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal trucking = purchase.multiply(truckingPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal total = purchase.add(commission).add(trucking);

        txtCommissionAmt.setText(format(commission));
        txtTruckingAmt.setText(format(trucking));
        txtCashOnHand.setText(format(total));
        txtDebitTotal.setText(format(total));
        txtCreditTotal.setText(format(total));
    }

    private void save() {
        if (txtRrNo.getText().trim().isBlank() || selectedReceivingId <= 0) {
            CisDialogs.showInfo(this, "Select an R.R. row first.");
            return;
        }
        try {
            BigDecimal fishPurchase = decimal(txtFishPurchase.getText());
            BigDecimal totalAmount = decimal(txtDebitTotal.getText());
            DisbursementHeader header = new DisbursementHeader();
            header.setCvNo(txtRrNo.getText().trim());
            header.setCvDate(parseDate(txtDate.getText().trim()));
            header.setSupplierCode(txtSupplierCode.getText().trim());
            header.setSupplierName(txtSupplierName.getText().trim());
            header.setAmount(totalAmount);
            disbursementDAO.save(header, selectedReceivingId, txtRrNo.getText().trim(), fishPurchase, totalAmount, txtSummary.getText().trim());
            CisDialogs.showInfo(this, "C.V. saved.");
            dispose();
        } catch (RuntimeException e) {
            CisDialogs.showError(this, e.getMessage());
        }
    }

    private void showPreview() {
        String preview = """
                C.V. ENTRY

                Supplier    : %s - %s
                R.R. No.    : %s
                Date        : %s

                Fish Purchase : %s
                Fish Commision: %s
                Trucking      : %s
                Cash on Hand  : %s
                """.formatted(
                txtSupplierCode.getText().trim(),
                txtSupplierName.getText().trim(),
                txtRrNo.getText().trim(),
                txtDate.getText().trim(),
                txtFishPurchase.getText().trim(),
                txtCommissionAmt.getText().trim(),
                txtTruckingAmt.getText().trim(),
                txtCashOnHand.getText().trim());
        PreviewDialog dialog = new PreviewDialog(this, preview);
        dialog.setVisible(true);
    }

    private BigDecimal decimal(String text) {
        try {
            return new BigDecimal(text == null || text.isBlank() ? "0" : text.trim().replace(",", ""));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String format(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private LocalDate parseDate(String text) {
        try {
            return text != null && text.contains("/") ? LocalDate.parse(text, DISPLAY_DATE) : LocalDate.parse(text);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private static class PreviewDialog extends FoxProChildDialog {
        PreviewDialog(Window owner, String text) {
            super(owner, "Preview", CisScale.scale(420), CisScale.scale(280));
            JTextArea area = new JTextArea(text);
            area.setEditable(false);
            area.setFont(FoxProTheme.FONT);
            JScrollPane scroll = new JScrollPane(area);
            scroll.setBorder(null);
            setContentPane(scroll);
        }
    }
}
