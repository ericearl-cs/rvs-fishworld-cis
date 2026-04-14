package com.rvsfishworld.ui.master;

import com.rvsfishworld.dao.OperationalTableDAO;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.core.CisDialogs;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class OperationalTableInternalFrame extends JInternalFrame {
    private final OperationalTableDAO dao = new OperationalTableDAO();
    private final JTabbedPane tabs = new JTabbedPane();
    private final BrowsePanel salesmenPanel = new BrowsePanel("SALESMAN");
    private final BrowsePanel areasPanel = new BrowsePanel("AREA");
    private final BrowsePanel banksPanel = new BrowsePanel("BANK");

    public OperationalTableInternalFrame() {
        super("Operational Table", true, true, true, true);
        FoxProTheme.applyGlobalFont();
        setSize(980, 620);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(buildCommandBar(), BorderLayout.WEST);
        root.add(buildTabs(), BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);
    }

    private JPanel buildCommandBar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(FoxProTheme.PANEL);
        addCommand(panel, "Find", e -> currentPanel().loadRows());
        addCommand(panel, "View", e -> currentPanel().openSelected(true));
        addCommand(panel, "Add", e -> currentPanel().openEditor(Map.of(), false));
        addCommand(panel, "Edit", e -> currentPanel().openSelected(false));
        addCommand(panel, "Delete", e -> currentPanel().deleteSelected());
        addCommand(panel, "Exit", e -> doDefaultCloseAction());
        return panel;
    }

    private void addCommand(JPanel panel, String label, java.awt.event.ActionListener listener) {
        JButton button = FoxProTheme.createButton(label);
        button.setMaximumSize(new Dimension(110, 34));
        button.addActionListener(listener);
        panel.add(button);
        panel.add(Box.createVerticalStrut(8));
    }

    private JTabbedPane buildTabs() {
        tabs.setFont(FoxProTheme.FONT_BOLD);
        tabs.addTab("Salesmen", salesmenPanel);
        tabs.addTab("Areas", areasPanel);
        tabs.addTab("Banks", banksPanel);
        return tabs;
    }

    private BrowsePanel currentPanel() {
        return switch (tabs.getSelectedIndex()) {
            case 1 -> areasPanel;
            case 2 -> banksPanel;
            default -> salesmenPanel;
        };
    }

    private final class BrowsePanel extends JPanel {
        private final String kind;
        private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "CODE", "NAME", "EXTRA", "ACTIVE"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        private final JTable table = new JTable(model);
        private List<OperationalTableDAO.Row> rows = List.of();

        private BrowsePanel(String kind) {
            super(new BorderLayout());
            this.kind = kind;
            setBackground(FoxProTheme.PANEL);
            FoxProTheme.styleTable(table);
            table.setRowHeight(22);
            JScrollPane scroll = new JScrollPane(table);
            add(scroll, BorderLayout.CENTER);
            hideIdColumn();
            loadRows();
        }

        private void hideIdColumn() {
            var column = table.getColumnModel().getColumn(0);
            column.setMinWidth(0);
            column.setMaxWidth(0);
            column.setPreferredWidth(0);
        }

        private void loadRows() {
            model.setRowCount(0);
            rows = dao.browse(kind);
            for (OperationalTableDAO.Row row : rows) {
                model.addRow(new Object[]{row.id(), row.code(), row.name(), row.extra(), row.active() ? "YES" : "NO"});
            }
        }

        private void openSelected(boolean readOnly) {
            int row = table.getSelectedRow();
            if (row < 0) {
                CisDialogs.showInfo(OperationalTableInternalFrame.this, "Select a row first.");
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            long id = Long.parseLong(String.valueOf(model.getValueAt(modelRow, 0)));
            openEditor(dao.find(kind, id), readOnly);
        }

        private void openEditor(Map<String, Object> values, boolean readOnly) {
            OperationalRecordDialog dialog = new OperationalRecordDialog(
                    SwingUtilities.getWindowAncestor(OperationalTableInternalFrame.this),
                    kind,
                    values,
                    readOnly);
            dialog.setVisible(true);
            if (!readOnly && dialog.isSaved()) {
                dao.save(kind, dialog.values());
                loadRows();
            }
        }

        private void deleteSelected() {
            int row = table.getSelectedRow();
            if (row < 0) {
                CisDialogs.showInfo(OperationalTableInternalFrame.this, "Select a row first.");
                return;
            }
            if (CisDialogs.askYesNo(OperationalTableInternalFrame.this, "Delete Row", "Delete selected row?") != CisDialogs.YES) {
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            long id = Long.parseLong(String.valueOf(model.getValueAt(modelRow, 0)));
            dao.delete(kind, id);
            loadRows();
        }
    }
}
