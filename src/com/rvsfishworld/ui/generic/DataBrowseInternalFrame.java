package com.rvsfishworld.ui.generic;

import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.db.Database;
import com.rvsfishworld.ui.core.CisScale;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class DataBrowseInternalFrame extends JInternalFrame {
    private final String moduleName;
    private final String[] commandButtons;
    private final String[] columns;
    private final String[] orderLabels;
    private final String[] orderSqls;
    private final String footerText;
    private final DefaultTableModel model;
    private final JTable table;
    private final JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private int selectedOrderIndex = 0;

    public DataBrowseInternalFrame(
            String title,
            String moduleName,
            String[] orderLabels,
            String[] orderSqls,
            String[] commandButtons,
            String[] columns,
            String footerText) {
        super(title, true, true, true, true);
        this.moduleName = moduleName;
        this.commandButtons = commandButtons;
        this.columns = columns;
        this.orderLabels = orderLabels;
        this.orderSqls = orderSqls;
        this.footerText = footerText;
        this.model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.table = new JTable(model);

        FoxProTheme.applyGlobalFont();
        setSize(CisScale.scale(760), CisScale.scale(420));
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(CisScale.scale(8), CisScale.scale(8)));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(
                CisScale.scale(6), CisScale.scale(6), CisScale.scale(6), CisScale.scale(6)));

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildCommandBar(), BorderLayout.WEST);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
        loadRows();
    }

    private JPanel buildTopBar() {
        topBar.setBackground(FoxProTheme.PANEL);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, CisScale.scale(1), 0));
        refreshOrderButtons();
        return topBar;
    }

    private void refreshOrderButtons() {
        topBar.removeAll();
        for (int i = 0; i < orderLabels.length; i++) {
            int index = i;
            JButton button = FoxProTheme.createOrderButton(orderLabels[i], i == selectedOrderIndex);
            button.addActionListener(e -> {
                selectedOrderIndex = index;
                refreshOrderButtons();
                loadRows();
            });
            topBar.add(button);
        }
        topBar.revalidate();
        topBar.repaint();
    }

    private JPanel buildCommandBar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(FoxProTheme.PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        for (String label : commandButtons) {
            JButton button = FoxProTheme.createButton(label);
            button.setMaximumSize(new Dimension(CisScale.scale(94), CisScale.scale(28)));
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.addActionListener(e -> handleCommand(label));
            panel.add(button);
            panel.add(Box.createVerticalStrut(CisScale.scale(5)));
        }
        return panel;
    }

    protected void handleCommand(String label) {
        if ("Exit".equalsIgnoreCase(label)) {
            doDefaultCloseAction();
            return;
        }
        if ("Refresh".equalsIgnoreCase(label) || "Find".equalsIgnoreCase(label)) {
            loadRows();
            return;
        }
        JOptionPane.showMessageDialog(
                this,
                moduleName + " workflow is on the next pass.",
                moduleName,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JScrollPane buildCenter() {
        FoxProTheme.styleTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(FoxProTheme.GRID));
        scrollPane.getHorizontalScrollBar().setUnitIncrement(12);
        return scrollPane;
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(FoxProTheme.PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(CisScale.scale(6), 0, 0, 0));
        JLabel label = new JLabel(footerText);
        label.setFont(FoxProTheme.FONT);
        panel.add(label, BorderLayout.WEST);
        panel.setVisible(footerText != null && !footerText.isBlank());
        return panel;
    }

    protected JTable getTable() {
        return table;
    }

    protected DefaultTableModel getModel() {
        return model;
    }

    protected void setColumnWidths(int... widths) {
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            int scaledWidth = CisScale.scale(widths[i]);
            column.setPreferredWidth(scaledWidth);
            column.setMinWidth(Math.min(scaledWidth, CisScale.scale(28)));
        }
    }

    protected void rightAlignColumns(int... indexes) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int index : indexes) {
            if (index >= 0 && index < table.getColumnCount()) {
                table.getColumnModel().getColumn(index).setCellRenderer(renderer);
            }
        }
    }

    protected void loadRows() {
        model.setRowCount(0);
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(orderSqls[selectedOrderIndex]);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columns.length; i++) {
                    row.add(rs.getObject(i));
                }
                model.addRow(row);
            }
        } catch (Exception e) {
            model.setRowCount(0);
            Object[] row = new Object[columns.length];
            if (columns.length > 0) row[0] = "ERROR";
            if (columns.length > 1) row[1] = e.getMessage();
            if (columns.length > 2) row[2] = "Run the updated SQL files first.";
            model.addRow(row);
        }
    }
}

