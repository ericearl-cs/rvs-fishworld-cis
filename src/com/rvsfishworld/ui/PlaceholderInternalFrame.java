package com.rvsfishworld.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PlaceholderInternalFrame extends JInternalFrame {
    public PlaceholderInternalFrame(
            String title,
            String moduleName,
            String[] orderButtons,
            String[] commandButtons,
            String[] columns,
            Object[][] rows,
            String footerText) {
        super(title, true, true, true, true);
        FoxProTheme.applyGlobalFont();

        setSize(980, 620);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        root.add(buildTopBar(orderButtons), BorderLayout.NORTH);
        root.add(buildCommandBar(moduleName, commandButtons), BorderLayout.WEST);
        root.add(buildCenter(columns, rows), BorderLayout.CENTER);
        root.add(buildFooter(footerText), BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
    }

    public PlaceholderInternalFrame(String title, String moduleName, String footerText) {
        this(
                title,
                moduleName,
                new String[]{"Order by Code", "Order by Name"},
                new String[]{"Find", "Add", "Edit", "Delete", "Print", "Exit"},
                new String[]{"Code", "Name", "Notes"},
                new Object[][]{{"", "", footerText}},
                footerText
        );
    }

    private JPanel buildTopBar(String[] orderButtons) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBackground(FoxProTheme.PANEL);

        for (int i = 0; i < orderButtons.length; i++) {
            panel.add(FoxProTheme.createOrderButton(orderButtons[i], i == 0));
        }
        return panel;
    }

    private JPanel buildCommandBar(String moduleName, String[] commandButtons) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(FoxProTheme.PANEL);

        for (String label : commandButtons) {
            JButton button = FoxProTheme.createButton(label);
            button.setMaximumSize(new Dimension(106, 32));

            if ("Exit".equalsIgnoreCase(label)) {
                button.addActionListener(e -> doDefaultCloseAction());
            } else {
                button.addActionListener(e -> JOptionPane.showMessageDialog(
                        this,
                        moduleName + " screen scaffold is ready.",
                        moduleName,
                        JOptionPane.INFORMATION_MESSAGE));
            }

            panel.add(button);
            panel.add(Box.createVerticalStrut(8));
        }
        return panel;
    }

    private JScrollPane buildCenter(String[] columns, Object[][] rows) {
        DefaultTableModel model = new DefaultTableModel(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        FoxProTheme.styleTable(table);
        return new JScrollPane(table);
    }

    private JPanel buildFooter(String footerText) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(FoxProTheme.PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JLabel label = new JLabel(footerText);
        label.setFont(FoxProTheme.FONT);
        panel.add(label, BorderLayout.WEST);
        return panel;
    }
}
