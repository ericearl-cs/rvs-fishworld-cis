package com.rvsfishworld.ui;

import com.rvsfishworld.dao.ReceivingDAO;
import com.rvsfishworld.model.ReceivingHeader;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ReceivingPurchaseInternalFrame extends JInternalFrame {
    private final String[] orderLabels = {"Order by R.R. No.", "by Supplier Code", "by Date"};
    private final String[] orderKeys = {"RR", "SUPPLIER", "DATE"};
    private final ReceivingDAO receivingDAO = new ReceivingDAO();
    private final DecimalFormat amountFormat = new DecimalFormat("#,##0.00");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"R.R. No.", "Supplier Code", "Supplier Name", "Supp. Ref.", "Date", "Fish Purchase", "C.V. Amount", "Amount Paid", "Balance"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);
    private final JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private int selectedOrderIndex = 2;
    private final List<ReceivingDAO.BrowseRow> browseRows = new ArrayList<>();

    public ReceivingPurchaseInternalFrame() {
        super("RECEIVING OF FISH PURCHASES", true, true, true, true);
        FoxProTheme.applyGlobalFont();
        setSize(1180, 620);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildCommandBar(), BorderLayout.WEST);
        root.add(buildGrid(), BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);

        loadRows();
    }

    private JPanel buildTopBar() {
        topBar.setBackground(FoxProTheme.PANEL);
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

        addCommand(panel, "Find", e -> loadRows());
        addCommand(panel, "Add", e -> openAddDialog());
        addCommand(panel, "Edit", e -> openEditDialog());
        addCommand(panel, "Delete", e -> deleteSelected());
        addCommand(panel, "Cancel / Recall", e -> toggleCancelRecall());
        addCommand(panel, "Print", e -> JOptionPane.showMessageDialog(this, "Print flow follows the old report layout and will be wired after the receiving entry flow is stable."));
        addCommand(panel, "C.V.", e -> JOptionPane.showMessageDialog(this, "C.V. flow is not yet wired."));
        addCommand(panel, "Exit", e -> doDefaultCloseAction());
        return panel;
    }

    private void addCommand(JPanel panel, String label, java.awt.event.ActionListener listener) {
        JButton button = FoxProTheme.createButton(label);
        button.setMaximumSize(new Dimension(120, 34));
        button.addActionListener(listener);
        panel.add(button);
        panel.add(Box.createVerticalStrut(8));
    }

    private JScrollPane buildGrid() {
        FoxProTheme.styleTable(table);
        table.setRowHeight(22);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                boolean cancelled = row >= 0 && row < browseRows.size() && browseRows.get(row).isCancelled();
                if (cancelled) {
                    if (isSelected) {
                        c.setBackground(new Color(180, 160, 215));
                    } else {
                        c.setBackground(new Color(225, 214, 245));
                    }
                } else {
                    if (isSelected) {
                        c.setBackground(table.getSelectionBackground());
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
        return new JScrollPane(table);
    }

    private void openAddDialog() {
        ReceivingPurchaseEntryDialog dialog = new ReceivingPurchaseEntryDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadRows();
        }
    }

    private void openEditDialog() {
        ReceivingDAO.BrowseRow row = getSelectedBrowseRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select a receiving row first.");
            return;
        }
        ReceivingHeader header = receivingDAO.findHeaderById(row.getReceivingId());
        if (header == null) {
            JOptionPane.showMessageDialog(this, "Receiving transaction not found.");
            return;
        }
        ReceivingPurchaseEntryDialog dialog = new ReceivingPurchaseEntryDialog(SwingUtilities.getWindowAncestor(this), header);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadRows();
        }
    }

    private void deleteSelected() {
        ReceivingDAO.BrowseRow row = getSelectedBrowseRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select a receiving row first.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Delete selected receiving record?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            receivingDAO.delete(row.getReceivingId());
            loadRows();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleCancelRecall() {
        ReceivingDAO.BrowseRow row = getSelectedBrowseRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select a receiving row first.");
            return;
        }
        try {
            receivingDAO.toggleCancelled(row.getReceivingId());
            loadRows();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cancel / Recall Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ReceivingDAO.BrowseRow getSelectedBrowseRow() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= browseRows.size()) {
            return null;
        }
        return browseRows.get(row);
    }

    private void loadRows() {
        model.setRowCount(0);
        browseRows.clear();
        try {
            browseRows.addAll(receivingDAO.findBrowseRows(orderKeys[selectedOrderIndex]));
            for (ReceivingDAO.BrowseRow row : browseRows) {
                Object[] values = row.toRow();
                values[5] = amountFormat.format(values[5]);
                values[6] = amountFormat.format(values[6]);
                values[7] = amountFormat.format(values[7]);
                values[8] = amountFormat.format(values[8]);
                model.addRow(values);
            }
        } catch (Exception e) {
            model.setRowCount(0);
            model.addRow(new Object[]{"ERROR", e.getMessage(), "Run the updated SQL patch first.", "", "", "", "", "", ""});
        }
    }
}
