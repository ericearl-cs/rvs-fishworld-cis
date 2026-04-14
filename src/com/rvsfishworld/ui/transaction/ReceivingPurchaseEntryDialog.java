package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.core.CisScale;
import com.rvsfishworld.ui.generic.LookupDialog;

import com.rvsfishworld.dao.LookupDAO;
import com.rvsfishworld.dao.ReceivingDAO;
import com.rvsfishworld.model.LookupItem;
import com.rvsfishworld.model.ReceivingHeader;
import com.rvsfishworld.model.ReceivingLine;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReceivingPurchaseEntryDialog extends JDialog {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final LookupDAO lookupDAO = new LookupDAO();
    private final ReceivingDAO receivingDAO = new ReceivingDAO();
    private final ReceivingHeader editingHeader;

    private final JTextField txtRrNo = FoxProTheme.createTextField(12);
    private final JTextField txtDateReceived = FoxProTheme.createTextField(10);
    private final JCheckBox chkDirectPurchase = new JCheckBox("Direct Pur.");
    private final JTextField txtSupplierCode = FoxProTheme.createTextField(10);
    private final JTextField txtSupplierName = FoxProTheme.createTextField(42);
    private final JTextField txtLtpNo = FoxProTheme.createTextField(14);
    private final JTextField txtBranchCode = FoxProTheme.createTextField(8);
    private final JTextField txtBranchName = FoxProTheme.createTextField(34);
    private final JTextField txtCurrencyCode = FoxProTheme.createTextField(8);
    private final JTextField txtCurrencyName = FoxProTheme.createTextField(34);
    private final JTextField txtEncodedBy = FoxProTheme.createTextField(10);
    private final JTextField txtCheckedBy = FoxProTheme.createTextField(10);
    private final JTextField txtApprovedBy = FoxProTheme.createTextField(10);
    private final JTextField txtTotalCost = FoxProTheme.createTextField(10);
    private final JTextField txtTotalCostPeso = FoxProTheme.createTextField(10);
    private final JButton btnFindBoxNo = FoxProTheme.createButton("Find Box No.");
    private final JButton btnAdd = FoxProTheme.createButton("Add");
    private final JButton btnEdit = FoxProTheme.createButton("Edit");
    private final JButton btnDelete = FoxProTheme.createButton("Delete");
    private final JButton btnSave = FoxProTheme.createButton("Save");
    private final JButton btnExit = FoxProTheme.createButton("Exit");

    private long supplierId;
    private long branchId;
    private long currencyId;
    private boolean saved;

    private final DefaultTableModel lineModel = new DefaultTableModel(
            new Object[]{"<html><center>GROUP<br>CODE</center></html>",
                    "<html><center>Product<br>No.</center></html>",
                    "Description",
                    "<html><center>Qty.<br>Del.</center></html>",
                    "<html><center>Qty.<br>D.O.A.</center></html>",
                    "<html><center>Qty.<br>Reject</center></html>",
                    "Reason",
                    "Tank",
                    "<html><center>Qty.<br>Bought</center></html>",
                    "<html><center>Unit<br>Cost</center></html>",
                    "<html><center>Total<br>Cost</center></html>"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable lineTable = new JTable(lineModel);
    private final List<ReceivingLine> lines = new ArrayList<>();

    public ReceivingPurchaseEntryDialog(Window owner) {
        this(owner, null);
    }

    public ReceivingPurchaseEntryDialog(Window owner, ReceivingHeader header) {
        super(owner, header == null ? "Receiving of Fish Purchases" : "Edit Receiving of Fish Purchases", ModalityType.APPLICATION_MODAL);
        this.editingHeader = header;
        FoxProTheme.applyGlobalFont();
        setSize(CisScale.scale(1380), CisScale.scale(760));
        setLocationRelativeTo(owner);
        setResizable(false);
        buildUi();
        bindCodeEntry();
        bindEnterAsTab();
        if (header == null) {
            resetDefaults();
        } else {
            loadHeader(header);
        }
    }

    private void buildUi() {
        chkDirectPurchase.setBackground(FoxProTheme.PANEL);
        txtSupplierName.setEditable(false);
        txtBranchName.setEditable(false);
        txtCurrencyName.setEditable(false);
        txtTotalCost.setEditable(false);
        txtTotalCostPeso.setEditable(false);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(FoxProTheme.PANEL);

        JButton btnSupplierLookup = FoxProTheme.createLookupButton();
        JButton btnBranchLookup = FoxProTheme.createLookupButton();
        JButton btnCurrencyLookup = FoxProTheme.createLookupButton();
        btnSupplierLookup.addActionListener(e -> openSupplierLookup());
        btnBranchLookup.addActionListener(e -> openBranchLookup());
        btnCurrencyLookup.addActionListener(e -> openCurrencyLookup());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addHeaderBlock(panel, gbc, 0, 0, "R.R. No.", txtRrNo, null, null);
        addHeaderBlock(panel, gbc, 0, 4, "Supplier", txtSupplierCode, btnSupplierLookup, txtSupplierName);

        addHeaderBlock(panel, gbc, 1, 0, "Date Rec.", txtDateReceived, null, null);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        panel.add(chkDirectPurchase, gbc);
        gbc.gridwidth = 1;
        addHeaderBlock(panel, gbc, 1, 4, "LTP No.", txtLtpNo, null, null);

        addHeaderBlock(panel, gbc, 2, 4, "Branch", txtBranchCode, btnBranchLookup, txtBranchName);
        addHeaderBlock(panel, gbc, 3, 4, "Currency", txtCurrencyCode, btnCurrencyLookup, txtCurrencyName);

        return panel;
    }

    private void addHeaderBlock(JPanel panel, GridBagConstraints gbc, int row, int col, String label,
                                JComponent field, JComponent middle, JComponent end) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = col + 1;
        gbc.weightx = 0.20;
        panel.add(field, gbc);

        if (middle != null) {
            gbc.gridx = col + 2;
            gbc.weightx = 0;
            panel.add(middle, gbc);
        }

        if (end != null) {
            gbc.gridx = col + 3;
            gbc.weightx = 0.40;
            panel.add(end, gbc);
        }
    }

    private JPanel buildCenter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(FoxProTheme.PANEL);

        FoxProTheme.styleTable(lineTable);
        lineTable.setRowHeight(22);
        lineTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {84, 86, 300, 58, 62, 64, 74, 56, 72, 84, 96};
        for (int i = 0; i < widths.length; i++) {
            lineTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scrollPane = new JScrollPane(lineTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(FoxProTheme.PANEL);

        btnAdd.addActionListener(e -> addLine());
        btnEdit.addActionListener(e -> editLine());
        btnDelete.addActionListener(e -> deleteLine());
        btnSave.addActionListener(e -> saveTransaction());
        btnExit.addActionListener(e -> dispose());
        btnFindBoxNo.addActionListener(e -> findBoxNo());
        btnFindBoxNo.setPreferredSize(new Dimension(132, 32));

        JPanel metaLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        metaLine.setBackground(FoxProTheme.PANEL);
        metaLine.add(new JLabel("Encoded by :"));
        metaLine.add(txtEncodedBy);
        metaLine.add(new JLabel("Checked by :"));
        metaLine.add(txtCheckedBy);
        metaLine.add(new JLabel("Approved by :"));
        metaLine.add(txtApprovedBy);

        JPanel lower = new JPanel(new BorderLayout(8, 0));
        lower.setBackground(FoxProTheme.PANEL);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setBackground(FoxProTheme.PANEL);
        buttons.add(btnAdd);
        buttons.add(btnEdit);
        buttons.add(btnDelete);
        buttons.add(btnSave);
        buttons.add(btnExit);

        JPanel totals = new JPanel(new GridBagLayout());
        totals.setBackground(FoxProTheme.PANEL);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 6, 0, 0);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        totals.add(btnFindBoxNo, gbc);

        gbc.gridheight = 1;
        gbc.gridx = 1;
        gbc.gridy = 0;
        totals.add(new JLabel("Total Cost"), gbc);
        gbc.gridx = 2;
        totals.add(txtTotalCost, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        totals.add(new JLabel("Total Cost (Peso)"), gbc);
        gbc.gridx = 2;
        totals.add(txtTotalCostPeso, gbc);

        lower.add(buttons, BorderLayout.WEST);
        lower.add(totals, BorderLayout.EAST);

        panel.add(metaLine, BorderLayout.NORTH);
        panel.add(lower, BorderLayout.CENTER);
        return panel;
    }

    private void bindEnterAsTab() {
        JComponent[] fields = {
                txtRrNo, txtDateReceived, chkDirectPurchase, txtSupplierCode, txtLtpNo,
                txtBranchCode, txtCurrencyCode, txtEncodedBy, txtCheckedBy, txtApprovedBy
        };
        for (JComponent field : fields) {
            registerEnterAsTab(field);
        }
    }

    private void registerEnterAsTab(JComponent component) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = component.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "focusNext");
        actionMap.put("focusNext", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.transferFocus();
            }
        });
    }

    private void bindCodeEntry() {
        registerCodeLookupEnter(txtSupplierCode, this::fillSupplierByCode);
        registerCodeLookupEnter(txtBranchCode, this::fillBranchByCode);
        registerCodeLookupEnter(txtCurrencyCode, this::fillCurrencyByCode);
    }

    private void registerCodeLookupEnter(JTextField field, Runnable action) {
        InputMap inputMap = field.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = field.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "lookupAndNext");
        actionMap.put("lookupAndNext", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
                field.transferFocus();
            }
        });
    }

    private void openSupplierLookup() {
        LookupDialog dialog = new LookupDialog(this, "Supplier Lookup", lookupDAO::findSuppliers);
        dialog.setVisible(true);
        LookupItem item = dialog.getSelectedItem();
        if (item != null) {
            supplierId = item.getId();
            txtSupplierCode.setText(item.getCode());
            txtSupplierName.setText(item.getName());
        }
    }

    private void openBranchLookup() {
        LookupDialog dialog = new LookupDialog(this, "Branch Lookup", lookupDAO::findBranches);
        dialog.setVisible(true);
        LookupItem item = dialog.getSelectedItem();
        if (item != null) {
            branchId = item.getId();
            txtBranchCode.setText(item.getCode());
            txtBranchName.setText(item.getName());
        }
    }

    private void openCurrencyLookup() {
        LookupDialog dialog = new LookupDialog(this, "Currency Lookup", lookupDAO::findCurrencies);
        dialog.setVisible(true);
        LookupItem item = dialog.getSelectedItem();
        if (item != null) {
            currencyId = item.getId();
            txtCurrencyCode.setText(item.getCode());
            txtCurrencyName.setText(item.getName());
        }
    }

    private void fillSupplierByCode() {
        LookupItem item = lookupDAO.findSupplierByCode(txtSupplierCode.getText().trim());
        if (item == null) {
            supplierId = 0;
            txtSupplierName.setText("");
            return;
        }
        supplierId = item.getId();
        txtSupplierName.setText(item.getName());
    }

    private void fillBranchByCode() {
        LookupItem item = lookupDAO.findBranchByCode(txtBranchCode.getText().trim());
        if (item == null) {
            branchId = 0;
            txtBranchName.setText("");
            return;
        }
        branchId = item.getId();
        txtBranchName.setText(item.getName());
    }

    private void fillCurrencyByCode() {
        LookupItem item = lookupDAO.findCurrencyByCode(txtCurrencyCode.getText().trim());
        if (item == null) {
            currencyId = 0;
            txtCurrencyName.setText("");
            return;
        }
        currencyId = item.getId();
        txtCurrencyName.setText(item.getName());
    }

    private void addLine() {
        ReceivingLineDialog dialog = new ReceivingLineDialog(this, null);
        dialog.setVisible(true);
        if (!dialog.isAccepted()) {
            return;
        }
        ReceivingLine line = dialog.getLine();
        line.setLineNo(lines.size() + 1);
        lines.add(line);
        refreshLineTable();
    }

    private void editLine() {
        int row = lineTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a line first.");
            return;
        }
        ReceivingLine current = lines.get(row);
        ReceivingLine copy = new ReceivingLine();
        copy.setLineNo(current.getLineNo());
        copy.setGroupCode(current.getGroupCode());
        copy.setProductId(current.getProductId());
        copy.setProductCode(current.getProductCode());
        copy.setProductDescription(current.getProductDescription());
        copy.setQtyDelivered(current.getQtyDelivered());
        copy.setQtyDoa(current.getQtyDoa());
        copy.setQtyRejected(current.getQtyRejected());
        copy.setRejectReason(current.getRejectReason());
        copy.setTank(current.getTank());
        copy.setQtyBought(current.getQtyBought());
        copy.setUnitCost(current.getUnitCost());
        copy.setTotalCost(current.getTotalCost());
        copy.setStopFlag(current.isStopFlag());

        ReceivingLineDialog dialog = new ReceivingLineDialog(this, copy);
        dialog.setVisible(true);
        if (!dialog.isAccepted()) {
            return;
        }
        ReceivingLine updated = dialog.getLine();
        updated.setLineNo(current.getLineNo());
        lines.set(row, updated);
        refreshLineTable();
    }

    private void deleteLine() {
        int row = lineTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a line first.");
            return;
        }
        lines.remove(row);
        for (int i = 0; i < lines.size(); i++) {
            lines.get(i).setLineNo(i + 1);
        }
        refreshLineTable();
    }

    private void refreshLineTable() {
        lineModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (ReceivingLine line : lines) {
            line.recompute();
            total = total.add(line.getTotalCost());
            lineModel.addRow(new Object[]{
                    line.getGroupCode(),
                    line.getProductCode(),
                    line.getProductDescription(),
                    format4(line.getQtyDelivered()),
                    format4(line.getQtyDoa()),
                    format4(line.getQtyRejected()),
                    nullToBlank(line.getRejectReason()),
                    nullToBlank(line.getTank()),
                    format4(line.getQtyBought()),
                    format4(line.getUnitCost()),
                    format4(line.getTotalCost())
            });
        }
        if (lines.isEmpty()) {
            txtTotalCost.setText("");
            txtTotalCostPeso.setText("");
        } else {
            String formatted = format4(total);
            txtTotalCost.setText(formatted);
            txtTotalCostPeso.setText(formatted);
        }
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private String format4(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.setScale(4, RoundingMode.HALF_UP).toPlainString();
    }

    private void saveTransaction() {
        if (txtRrNo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "R.R. No. is required.");
            return;
        }
        if (supplierId <= 0) {
            JOptionPane.showMessageDialog(this, "Supplier is required.");
            return;
        }
        if (branchId <= 0) {
            JOptionPane.showMessageDialog(this, "Branch is required.");
            return;
        }
        if (currencyId <= 0) {
            JOptionPane.showMessageDialog(this, "Currency is required.");
            return;
        }
        if (lines.isEmpty()) {
            JOptionPane.showMessageDialog(this, "At least one line is required.");
            return;
        }

        ReceivingHeader header = new ReceivingHeader();
        if (editingHeader != null) {
            header.setReceivingId(editingHeader.getReceivingId());
            header.setCancelled(editingHeader.isCancelled());
        }
        header.setRrNo(txtRrNo.getText().trim());
        header.setDateReceived(LocalDate.parse(txtDateReceived.getText().trim(), DATE_FORMAT));
        header.setDirectPurchase(chkDirectPurchase.isSelected());
        header.setSupplierId(supplierId);
        header.setSupplierCode(txtSupplierCode.getText().trim());
        header.setSupplierName(txtSupplierName.getText().trim());
        header.setLtpNo(txtLtpNo.getText().trim());
        header.setBranchId(branchId);
        header.setBranchCode(txtBranchCode.getText().trim());
        header.setBranchName(txtBranchName.getText().trim());
        header.setCurrencyId(currencyId);
        header.setCurrencyCode(txtCurrencyCode.getText().trim());
        header.setCurrencyName(txtCurrencyName.getText().trim());
        header.setEncodedBy(txtEncodedBy.getText().trim());
        header.setCheckedBy(txtCheckedBy.getText().trim());
        header.setApprovedBy(txtApprovedBy.getText().trim());
        header.setTotalAmount(toDecimal(txtTotalCost.getText()));
        header.getLines().addAll(lines);

        int answer = JOptionPane.showConfirmDialog(this, "Save this record?", "Confirm Save", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            receivingDAO.save(header);
            saved = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BigDecimal toDecimal(String value) {
        try {
            String clean = value == null || value.isBlank() ? "0" : value.trim();
            return new BigDecimal(clean);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private void resetDefaults() {
        txtRrNo.setText(receivingDAO.getNextRrNo());
        txtDateReceived.setText(LocalDate.now().format(DATE_FORMAT));
        chkDirectPurchase.setSelected(true);
        txtSupplierCode.setText("");
        txtSupplierName.setText("");
        txtLtpNo.setText("");
        txtBranchCode.setText("MAIN");
        txtBranchName.setText("");
        txtCurrencyCode.setText("PESO");
        txtCurrencyName.setText("");
        txtEncodedBy.setText("");
        txtCheckedBy.setText("");
        txtApprovedBy.setText("");
        txtTotalCost.setText("");
        txtTotalCostPeso.setText("");
        supplierId = 0;
        branchId = 0;
        currencyId = 0;
        fillBranchByCode();
        fillCurrencyByCode();
        lines.clear();
        refreshLineTable();
    }

    private void loadHeader(ReceivingHeader header) {
        txtRrNo.setText(header.getRrNo());
        txtDateReceived.setText(header.getDateReceived().format(DATE_FORMAT));
        chkDirectPurchase.setSelected(header.isDirectPurchase());
        supplierId = header.getSupplierId();
        txtSupplierCode.setText(header.getSupplierCode());
        txtSupplierName.setText(header.getSupplierName());
        txtLtpNo.setText(header.getLtpNo());
        branchId = header.getBranchId();
        txtBranchCode.setText(header.getBranchCode());
        txtBranchName.setText(header.getBranchName());
        currencyId = header.getCurrencyId();
        txtCurrencyCode.setText(header.getCurrencyCode());
        txtCurrencyName.setText(header.getCurrencyName());
        txtEncodedBy.setText(nullToBlank(header.getEncodedBy()));
        txtCheckedBy.setText(nullToBlank(header.getCheckedBy()));
        txtApprovedBy.setText(nullToBlank(header.getApprovedBy()));
        lines.clear();
        lines.addAll(header.getLines());
        refreshLineTable();
    }

    private void findBoxNo() {
        String keyword = JOptionPane.showInputDialog(this, "Find Box No.");
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        String lookFor = keyword.trim().toUpperCase();
        for (int i = 0; i < lines.size(); i++) {
            ReceivingLine line = lines.get(i);
            if (nullToBlank(line.getTank()).toUpperCase().contains(lookFor)
                    || nullToBlank(line.getProductCode()).toUpperCase().contains(lookFor)) {
                lineTable.setRowSelectionInterval(i, i);
                lineTable.scrollRectToVisible(lineTable.getCellRect(i, 0, true));
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "No matching box number found.");
    }

    public boolean isSaved() {
        return saved;
    }
}

