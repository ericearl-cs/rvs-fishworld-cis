package com.rvsfishworld.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SalesInvoiceInternalFrame extends JInternalFrame {
    public SalesInvoiceInternalFrame() {
        super("Invoicing", true, true, true, true);
        FoxProTheme.applyGlobalFont();

        setSize(1120, 690);
        setLayout(new BorderLayout(8, 8));

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        root.add(buildCommandBar(), BorderLayout.WEST);
        root.add(buildEditor(), BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
    }

    private JPanel buildCommandBar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(FoxProTheme.PANEL);

        for (String label : new String[]{"Add", "Edit", "Delete", "Print", "Find", "Cancel/Recall", "Exit"}) {
            JButton button = FoxProTheme.createButton(label);
            button.setMaximumSize(new Dimension(106, 32));
            if ("Exit".equals(label)) {
                button.addActionListener(e -> doDefaultCloseAction());
            }
            panel.add(button);
            panel.add(Box.createVerticalStrut(8));
        }

        JButton cover = FoxProTheme.createButton("COVER INVOICE");
        cover.setMaximumSize(new Dimension(110, 42));
        panel.add(Box.createVerticalStrut(16));
        panel.add(cover);
        return panel;
    }

    private JPanel buildEditor() {
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
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(panel, gbc, 0, 0, "Proforma No.", value("P-00019111"), null);
        addField(panel, gbc, 1, 0, "Invoice No.", value("13078"), null);
        addField(panel, gbc, 2, 0, "Invoice Date", value("03/18/2026"), null);
        addField(panel, gbc, 3, 0, "Exchange Rate", value("0.00"), null);
        addField(panel, gbc, 4, 0, "AWB #", value(""), null);
        addField(panel, gbc, 5, 0, "BROKER", value(""), null);

        addField(panel, gbc, 0, 3, "Customer", value("CN0105"), value("APET INC"));
        addField(panel, gbc, 1, 3, "Branch", value("MAIN"), value("MAIN WAREHOUSE"));
        addField(panel, gbc, 2, 3, "Salesman", value(""), value("MEDJONG"));
        addField(panel, gbc, 3, 3, "Currency", value("USD"), value("AMERICAN"));
        addField(panel, gbc, 4, 3, "Pricing", value("B - US/SQ/EU"), null);

        JCheckBox applyFormula = new JCheckBox("Apply Formula", true);
        applyFormula.setBackground(FoxProTheme.PANEL);
        JCheckBox consumables = new JCheckBox("Consumables");
        consumables.setBackground(FoxProTheme.PANEL);

        gbc.gridx = 5;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(applyFormula, gbc);
        gbc.gridy = 5;
        panel.add(consumables, gbc);

        JPanel metrics = new JPanel(new GridLayout(2, 9, 4, 4));
        metrics.setBackground(FoxProTheme.PANEL);
        for (String label : new String[]{"BOX QTY.", "KGS.", "FISH COST", "DISC", "MISC,", "SSC", "RATE", "VAT", "STAMP"}) {
            metrics.add(valueLabel(label));
        }
        for (String value : new String[]{"0.00", "0.00", "0.00", "0.00", "0.00", "0.00", "0.00", "0.00", "0.00"}) {
            metrics.add(valueLabel(value));
        }

        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.setBackground(FoxProTheme.PANEL);
        wrapper.add(panel, BorderLayout.NORTH);
        wrapper.add(metrics, BorderLayout.SOUTH);
        return wrapper;
    }

    private JScrollPane buildGrid() {
        String[] columns = {"Tran-Shipper", "BOX", "Product No.", "Description", "SPECIAL", "Qty. Sold", "Selling Price", "Total Price"};
        Object[][] rows = {
                {"003", "5", "01001", "BANDED ANGEL (REEF SAFE)", "1", "5", "20.00", "100.00"},
                {"", "6", "01005", "BLUE BELLUS ANGEL FEMALE (REEF SAFE)", "1", "10", "55.00", "550.00"},
                {"1972", "6", "01008", "BLUE FACE ANGEL ADULT (M/L)", "1", "5", "45.00", "225.00"}
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

        JPanel names = new JPanel(new GridLayout(2, 4, 6, 4));
        names.setBackground(FoxProTheme.PANEL);
        for (String value : new String[]{"Prep.by  ", "Check by  ", "App.by  ", "Rec. by  ", "MKS", "", "", ""}) {
            names.add(valueLabel(value));
        }

        JPanel totals = new JPanel(new GridLayout(4, 4, 8, 4));
        totals.setBackground(FoxProTheme.PANEL);
        for (String value : new String[]{
                "Discount", "50.00 / 0.00", "Grand Total", "875.00",
                "DOA", "0.00", "Packing Charges", "0.00",
                "RATE", "0.00", "PRODUCT SALES", "875.00",
                "FREIGHT", "0.00", "TOTAL PAYABLES", "875.00"
        }) {
            totals.add(valueLabel(value));
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.setBackground(FoxProTheme.PANEL);
        for (String label : new String[]{"Add", "Edit", "Delete", "Save", "Exit"}) {
            JButton button = FoxProTheme.createButton(label);
            if ("Exit".equals(label)) {
                button.addActionListener(e -> doDefaultCloseAction());
            }
            buttons.add(button);
        }
        buttons.add(FoxProTheme.createButton("Print Labels"));

        panel.add(names, BorderLayout.NORTH);
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
        gbc.weightx = 0.2;
        panel.add(field1, gbc);

        if (field2 != null) {
            gbc.gridx = col + 2;
            gbc.weightx = 0.4;
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
