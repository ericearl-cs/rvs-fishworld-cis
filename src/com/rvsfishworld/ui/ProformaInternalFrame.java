package com.rvsfishworld.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ProformaInternalFrame extends JInternalFrame {
    public ProformaInternalFrame() {
        super("Proforma", true, true, true, true);
        FoxProTheme.applyGlobalFont();

        setSize(1080, 650);
        setLayout(new BorderLayout(8, 8));

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        root.add(buildTopTabs(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
    }

    private JPanel buildTopTabs() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBackground(FoxProTheme.PANEL);
        panel.add(FoxProTheme.createOrderButton("Main Info", true));
        panel.add(FoxProTheme.createOrderButton("Other Info", false));
        return panel;
    }

    private JPanel buildCenter() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(FoxProTheme.PANEL);

        panel.add(buildHeader(), BorderLayout.NORTH);
        panel.add(buildGrid(), BorderLayout.CENTER);
        panel.add(buildFooter(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(FoxProTheme.PANEL);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(panel, gbc, 0, 0, "Customer", value("CN0105"), value("APET INC"));
        addField(panel, gbc, 1, 0, "Salesman", value(""), value(""));
        addField(panel, gbc, 2, 0, "Branch", value("MAIN"), value("MAIN WAREHOUSE"));
        addField(panel, gbc, 0, 3, "Proforma No.", value("P-00019111"), null);
        addField(panel, gbc, 1, 3, "Date", value("03/17/2026"), null);
        addField(panel, gbc, 2, 3, "Adjustment", value("0.00 %"), null);

        return panel;
    }

    private JScrollPane buildGrid() {
        String[] columns = {"Tran-Shipper", "BOX", "Product No.", "Description", "Qty. Order", "U/M", "Selling Price", "Total Price", "Supplier"};
        Object[][] rows = {
                {"003", "5", "01001", "BANDED ANGEL (REEF SAFE)", "5", "PCS.", "20.00", "100.00", ""},
                {"", "6", "01005", "BLUE BELLUS ANGEL FEMALE (REEF SAFE)", "10", "PCS.", "55.00", "550.00", ""},
                {"1972", "6", "01008", "BLUE FACE ANGEL ADULT (M/L)", "5", "PCS.", "45.00", "225.00", ""}
        };

        JTable table = new JTable(new DefaultTableModel(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        FoxProTheme.styleTable(table);
        return new JScrollPane(table);
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(FoxProTheme.PANEL);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setBackground(FoxProTheme.PANEL);
        left.add(valueLabel("Issued by : MKS"));
        left.add(valueLabel("Approved by : MKS"));

        JPanel totals = new JPanel(new GridLayout(2, 4, 8, 4));
        totals.setBackground(FoxProTheme.PANEL);
        totals.add(valueLabel("Discount"));
        totals.add(valueLabel("0.00"));
        totals.add(valueLabel("Grand Total"));
        totals.add(valueLabel("875.00"));
        totals.add(valueLabel("Packing Charges"));
        totals.add(valueLabel("0.00"));
        totals.add(valueLabel("Total Payables"));
        totals.add(valueLabel("875.00"));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.setBackground(FoxProTheme.PANEL);
        for (String label : new String[]{"Add", "Edit", "Delete", "Save", "Cancel/Recall", "Exit"}) {
            JButton button = FoxProTheme.createButton(label);
            if ("Exit".equals(label)) {
                button.addActionListener(e -> doDefaultCloseAction());
            }
            buttons.add(button);
        }

        panel.add(left, BorderLayout.NORTH);
        panel.add(totals, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, int col, String label, JComponent field1, JComponent field2) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = col + 1;
        gbc.weightx = 0.25;
        panel.add(field1, gbc);

        if (field2 != null) {
            gbc.gridx = col + 2;
            gbc.weightx = 0.5;
            panel.add(field2, gbc);
        }
    }

    private JTextField value(String text) {
        JTextField field = FoxProTheme.createTextField(12);
        field.setText(text);
        return field;
    }

    private JLabel valueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FoxProTheme.FONT);
        return label;
    }
}
