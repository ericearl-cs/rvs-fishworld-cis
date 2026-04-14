package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.dao.MortalityDAO;
import com.rvsfishworld.model.MortalityLine;
import com.rvsfishworld.model.MortalityRecord;
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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class MortalityEntryDialog extends FoxProChildDialog {
    private final MortalityDAO mortalityDAO = new MortalityDAO();
    private MortalityRecord record;
    private boolean saved;

    private final JTextField txtReferenceNo = FoxProTheme.createTextField(12);
    private final JTextField txtDate = FoxProTheme.createTextField(10);
    private final JTextField txtArea = FoxProTheme.createTextField(18);
    private final JTextField txtTotal = FoxProTheme.createTextField(10);
    private final DefaultTableModel lineModel = new DefaultTableModel(
            new Object[]{"Product No.", "Description", "Area", "Qty.", "Average Cost", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable lineTable = new JTable(lineModel);

    public MortalityEntryDialog(Window owner) {
        this(owner, new MortalityRecord());
    }

    public MortalityEntryDialog(Window owner, MortalityRecord source) {
        super(owner, "Mortality Entry", 920, 640);
        this.record = source == null ? new MortalityRecord() : source;
        if (this.record.getRecordDate() == null) {
            this.record.setRecordDate(LocalDate.now());
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
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Reference No."), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        panel.add(txtReferenceNo, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Date"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.2;
        panel.add(txtDate, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Area"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        panel.add(txtArea, gbc);
        gbc.gridwidth = 1;
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

        JPanel totals = new JPanel(new GridBagLayout());
        totals.setBackground(FoxProTheme.PANEL);
        totals.setBorder(FoxProTheme.sectionBorder("Totals"));
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.gridx = 0;
        gbc.gridy = 0;
        totals.add(new JLabel("Total Amount"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        totals.add(txtTotal, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.setBackground(FoxProTheme.PANEL);
        JButton save = FoxProTheme.createButton("Save");
        JButton exit = FoxProTheme.createButton("Exit");
        save.addActionListener(e -> saveRecord());
        exit.addActionListener(e -> dispose());
        buttons.add(save);
        buttons.add(exit);

        panel.add(totals, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void loadRecord() {
        txtReferenceNo.setText(defaultString(record.getReferenceNo()));
        txtDate.setText(record.getRecordDate() == null ? "" : record.getRecordDate().toString());
        txtArea.setText(defaultString(record.getArea()));
        refreshGrid();
    }

    private void refreshGrid() {
        lineModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (MortalityLine line : record.getLines()) {
            line.recompute();
            total = total.add(money(line.getTotalCost()));
            lineModel.addRow(new Object[]{
                    line.getProductCode(),
                    line.getDescription(),
                    line.getArea(),
                    line.getQuantity(),
                    money(line.getAverageCost()).toPlainString(),
                    money(line.getTotalCost()).toPlainString()
            });
        }
        txtTotal.setText(money(total).toPlainString());
    }

    private void addLine() {
        MortalityLineDialog dialog = new MortalityLineDialog(this, null, txtArea.getText().trim());
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
        MortalityLineDialog dialog = new MortalityLineDialog(this, record.getLines().get(row), txtArea.getText().trim());
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

    private void saveRecord() {
        if (record.getLines().isEmpty()) {
            CisDialogs.showError(this, "Add at least one mortality line.");
            return;
        }
        try {
            record.setReferenceNo(txtReferenceNo.getText().trim());
            record.setRecordDate(txtDate.getText().isBlank() ? LocalDate.now() : LocalDate.parse(txtDate.getText().trim()));
            record.setArea(txtArea.getText().trim());
            mortalityDAO.save(record);
            saved = true;
            dispose();
        } catch (Exception e) {
            CisDialogs.showError(this, "Unable to save Mortality: " + e.getMessage());
        }
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
