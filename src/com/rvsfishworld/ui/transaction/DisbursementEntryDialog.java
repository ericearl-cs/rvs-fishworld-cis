package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.dao.ReceivingDAO;
import com.rvsfishworld.dao.DisbursementDAO;
import com.rvsfishworld.model.DisbursementHeader;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import com.rvsfishworld.ui.core.CisDialogs;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class DisbursementEntryDialog extends FoxProChildDialog {
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final DisbursementDAO disbursementDAO = new DisbursementDAO();
    private final JTextField txtCvNo = FoxProTheme.createTextField(12);
    private final JTextField txtDate = FoxProTheme.createTextField(10);
    private final JTextField txtSupplierCode = FoxProTheme.createTextField(10);
    private final JTextField txtSupplierName = FoxProTheme.createTextField(24);
    private final JTextField txtRrNo = FoxProTheme.createTextField(12);
    private final JTextField txtAmount = FoxProTheme.createTextField(10);
    private final JTextField txtRemarks = FoxProTheme.createTextField(24);
    private long selectedReceivingId;

    public DisbursementEntryDialog(Window owner) {
        this(owner, null);
    }

    public DisbursementEntryDialog(Window owner, ReceivingDAO.BrowseRow row) {
        super(owner, "C.V. Entry", 760, 340);
        setContentPane(buildContent());
        txtDate.setText(LocalDate.now().toString());
        if (row != null) {
            selectedReceivingId = row.getReceivingId();
            txtSupplierCode.setText(row.getSupplierCode());
            txtSupplierName.setText(row.getSupplierName());
            txtRrNo.setText(row.getRrNo());
            txtAmount.setText(row.getBalance().toPlainString());
        }
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

        addField(form, gbc, 0, "C.V. No.", txtCvNo, null);
        addField(form, gbc, 1, "Payment Date", txtDate, null);
        addField(form, gbc, 2, "Supplier", txtSupplierCode, txtSupplierName);

        JButton rrLookup = FoxProTheme.createLookupButton();
        rrLookup.addActionListener(e -> openRrLookup());
        addField(form, gbc, 3, "R.R. No.", txtRrNo, rrLookup);
        addField(form, gbc, 4, "Amount", txtAmount, null);
        addField(form, gbc, 5, "Remarks", txtRemarks, null);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.setBackground(FoxProTheme.PANEL);
        JButton save = FoxProTheme.createButton("Save");
        JButton exit = FoxProTheme.createButton("Exit");
        save.addActionListener(e -> save());
        exit.addActionListener(e -> dispose());
        buttons.add(save);
        buttons.add(exit);

        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        return root;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field, java.awt.Component extra) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.25;
        panel.add(field, gbc);
        if (extra != null) {
            gbc.gridx = 2;
            gbc.weightx = extra instanceof JTextField ? 0.55 : 0;
            panel.add(extra, gbc);
        }
    }

    private void openRrLookup() {
        DisbursementRrLookupDialog dialog = new DisbursementRrLookupDialog(this);
        dialog.setVisible(true);
        ReceivingDAO.BrowseRow row = dialog.getSelectedRow();
        if (row == null) {
            return;
        }
        selectedReceivingId = row.getReceivingId();
        txtSupplierCode.setText(row.getSupplierCode());
        txtSupplierName.setText(row.getSupplierName());
        txtRrNo.setText(row.getRrNo());
        txtAmount.setText(row.getBalance().setScale(2, RoundingMode.HALF_UP).toPlainString());
    }

    private void save() {
        if (txtCvNo.getText().trim().isBlank()) {
            CisDialogs.showInfo(this, "C.V. No. is required.");
            return;
        }
        if (selectedReceivingId <= 0 || txtRrNo.getText().trim().isBlank()) {
            CisDialogs.showInfo(this, "Select an R.R. row first.");
            return;
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(txtAmount.getText().trim());
        } catch (Exception e) {
            CisDialogs.showError(this, "Invalid amount.");
            return;
        }

        DisbursementHeader header = new DisbursementHeader();
        header.setCvNo(txtCvNo.getText().trim());
        header.setCvDate(parseDate(txtDate.getText().trim()));
        header.setSupplierCode(txtSupplierCode.getText().trim());
        header.setSupplierName(txtSupplierName.getText().trim());
        header.setAmount(amount);

        try {
            disbursementDAO.save(header, selectedReceivingId, txtRrNo.getText().trim(), amount, txtRemarks.getText().trim());
            CisDialogs.showInfo(this, "C.V. saved.");
            dispose();
        } catch (RuntimeException e) {
            CisDialogs.showError(this, e.getMessage());
        }
    }

    private LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) {
            return LocalDate.now();
        }
        try {
            if (text.contains("/")) {
                return LocalDate.parse(text, DISPLAY_DATE);
            }
            return LocalDate.parse(text);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
}
