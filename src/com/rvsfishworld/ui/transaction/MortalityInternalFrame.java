package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.dao.MortalityDAO;
import com.rvsfishworld.model.MortalityRecord;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.core.CisScale;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class MortalityInternalFrame extends JInternalFrame {
    private final MortalityDAO mortalityDAO = new MortalityDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Reference No.", "Date", "Area", "Total", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private final String[] orderLabels = {"Order by Ref No.", "by Date", "by Area"};
    private final String[] orderKeys = {"REF", "DATE", "AREA"};
    private final List<MortalityDAO.BrowseRow> rows = new ArrayList<>();
    private final DecimalFormat amountFormat = new DecimalFormat("#,##0.00");
    private int selectedOrderIndex = 1;

    public MortalityInternalFrame() {
        super("MORTALITY (NEW)", true, true, true, true);
        FoxProTheme.applyGlobalFont();
        setSize(CisScale.scale(780), CisScale.scale(620));
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(CisScale.scale(8), CisScale.scale(8)));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(
                CisScale.scale(8), CisScale.scale(8), CisScale.scale(8), CisScale.scale(8)));
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
        addCommand(panel, "Delete", e -> deleteCurrent());
        addCommand(panel, "Print", e -> openPrintDialog());
        addCommand(panel, "Exit", e -> doDefaultCloseAction());
        return panel;
    }

    private void addCommand(JPanel panel, String label, java.awt.event.ActionListener listener) {
        JButton button = FoxProTheme.createButton(label);
        button.setMaximumSize(new Dimension(CisScale.scale(106), CisScale.scale(32)));
        button.addActionListener(listener);
        panel.add(button);
        panel.add(Box.createVerticalStrut(CisScale.scale(8)));
    }

    private JScrollPane buildGrid() {
        FoxProTheme.styleTable(table);
        table.setRowHeight(CisScale.scale(22));
        return new JScrollPane(table);
    }

    private void loadRows() {
        model.setRowCount(0);
        rows.clear();
        try {
            rows.addAll(mortalityDAO.browse(orderKeys[selectedOrderIndex]));
            for (MortalityDAO.BrowseRow row : rows) {
                model.addRow(new Object[]{
                        row.referenceNo(),
                        row.recordDate() == null ? "" : row.recordDate().toString(),
                        row.area(),
                        amountFormat.format(row.totalAmount()),
                        row.status()
                });
            }
        } catch (Exception e) {
            model.addRow(new Object[]{"ERROR", "", e.getMessage(), "", ""});
        }
    }

    private MortalityDAO.BrowseRow getSelectedRow() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= rows.size()) {
            return null;
        }
        return rows.get(row);
    }

    private void openAddDialog() {
        MortalityEntryDialog dialog = new MortalityEntryDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadRows();
        }
    }

    private void openEditDialog() {
        MortalityDAO.BrowseRow row = getSelectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select a mortality row first.");
            return;
        }
        MortalityRecord record = mortalityDAO.load(row.id());
        MortalityEntryDialog dialog = new MortalityEntryDialog(SwingUtilities.getWindowAncestor(this), record);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadRows();
        }
    }

    private void deleteCurrent() {
        MortalityDAO.BrowseRow row = getSelectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select a mortality row first.");
            return;
        }
        if (CisDialogs.askYesNo(this, "Delete Mortality", "Delete selected mortality record?") != CisDialogs.YES) {
            return;
        }
        try {
            mortalityDAO.delete(row.id());
            loadRows();
        } catch (RuntimeException e) {
            CisDialogs.showError(this, e.getMessage());
        }
    }

    private void openPrintDialog() {
        MortalityDAO.BrowseRow row = getSelectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select a mortality row first.");
            return;
        }
        MortalityRecord record = mortalityDAO.load(row.id());
        new MortalityPrintDialog(SwingUtilities.getWindowAncestor(this), record).setVisible(true);
    }
}
