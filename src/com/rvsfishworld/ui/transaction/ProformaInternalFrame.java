package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.dao.GenericDocumentDAO;
import com.rvsfishworld.model.ProformaRecord;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.core.CisScale;
import com.rvsfishworld.ui.core.CisDialogs;
import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class ProformaInternalFrame extends JInternalFrame {
    private final GenericDocumentDAO documentDAO = new GenericDocumentDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Proforma No.", "Customer Code", "Customer Name", "Date", "Total Payables", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private final String[] orderLabels = {"Order by Proforma No.", "by Customer Code", "by Date"};
    private final String[] orderKeys = {"PROFORMA", "CUSTOMER", "DATE"};
    private final List<GenericDocumentDAO.BrowseRow> browseRows = new ArrayList<>();
    private final DecimalFormat amountFormat = new DecimalFormat("#,##0.00");
    private int selectedOrderIndex = 0;

    public ProformaInternalFrame() {
        super("Proforma", true, true, true, true);
        FoxProTheme.applyGlobalFont();
        setSize(CisScale.scale(1040), CisScale.scale(610));
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
        addCommand(panel, "Add", e -> openAddDialog());
        addCommand(panel, "Edit", e -> openEditDialog());
        addCommand(panel, "Delete", e -> deleteCurrent());
        addCommand(panel, "Print", e -> openPrintDialog());
        addCommand(panel, "Cancel/Recall", e -> toggleCancelRecall());
        addCommand(panel, "Find", e -> findRow());
        addCommand(panel, "Exit", e -> doDefaultCloseAction());
        return panel;
    }

    private void addCommand(JPanel panel, String label, java.awt.event.ActionListener listener) {
        JButton button = FoxProTheme.createButton(label);
        button.setMaximumSize(new Dimension(CisScale.scale(92), CisScale.scale(28)));
        button.addActionListener(listener);
        panel.add(button);
        panel.add(Box.createVerticalStrut(CisScale.scale(5)));
    }

    private JScrollPane buildGrid() {
        FoxProTheme.styleTable(table);
        table.setRowHeight(CisScale.scale(18));
        table.setSelectionBackground(new Color(246, 142, 255));
        table.setSelectionForeground(Color.BLACK);
        int[] widths = {130, 110, 270, 95, 120, 72};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(CisScale.scale(widths[i]));
        }
        table.getColumnModel().getColumn(4).setCellRenderer(rightAlignedRenderer());
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                boolean cancelled = row >= 0
                        && row < browseRows.size()
                        && "CANCELLED".equalsIgnoreCase(browseRows.get(row).status());
                if (!isSelected) {
                    component.setBackground(cancelled ? new Color(225, 214, 245) : Color.WHITE);
                    component.setForeground(Color.BLACK);
                }
                return component;
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    private void loadRows() {
        model.setRowCount(0);
        browseRows.clear();
        try {
            browseRows.addAll(documentDAO.browseProformas(orderKeys[selectedOrderIndex]));
            for (GenericDocumentDAO.BrowseRow row : browseRows) {
                model.addRow(new Object[]{
                        row.documentNo(),
                        row.partyCode(),
                        row.partyName(),
                        row.documentDate() == null ? "" : row.documentDate().toString(),
                        amountFormat.format(row.totalPayables()),
                        row.status()
                });
            }
            if (model.getRowCount() > 0) {
                int row = model.getRowCount() - 1;
                table.setRowSelectionInterval(row, row);
                table.scrollRectToVisible(table.getCellRect(row, 0, true));
            }
        } catch (Exception e) {
            model.addRow(new Object[]{"ERROR", "", e.getMessage(), "", "", ""});
        }
    }

    private GenericDocumentDAO.BrowseRow getSelectedRow() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= browseRows.size()) {
            return null;
        }
        return browseRows.get(row);
    }

    private void openAddDialog() {
        ProformaEntryDialog dialog = new ProformaEntryDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadRows();
        }
    }

    private void openEditDialog() {
        GenericDocumentDAO.BrowseRow row = getSelectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select a proforma row first.");
            return;
        }
        ProformaRecord record = documentDAO.loadProforma(row.documentId());
        ProformaEntryDialog dialog = new ProformaEntryDialog(SwingUtilities.getWindowAncestor(this), record);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadRows();
        }
    }

    private void deleteCurrent() {
        GenericDocumentDAO.BrowseRow row = getSelectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select a proforma row first.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Delete selected proforma?", "Delete Proforma", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        documentDAO.deleteProforma(row.documentId());
        loadRows();
    }

    private void toggleCancelRecall() {
        GenericDocumentDAO.BrowseRow row = getSelectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select a proforma row first.");
            return;
        }
        String action = "CANCELLED".equalsIgnoreCase(row.status()) ? "Recall" : "Cancel";
        if (CisDialogs.askYesNo(this, action + " Proforma", action + " selected proforma?") != CisDialogs.YES) {
            return;
        }
        documentDAO.toggleProformaCancelRecall(row.documentId());
        loadRows();
    }

    private void findRow() {
        String keyword = JOptionPane.showInputDialog(this, "Find Proforma No. / Customer Code / Customer Name");
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        String lookFor = keyword.trim().toUpperCase();
        for (int i = 0; i < browseRows.size(); i++) {
            GenericDocumentDAO.BrowseRow row = browseRows.get(i);
            String haystack = (row.documentNo() + " " + row.partyCode() + " " + row.partyName()).toUpperCase();
            if (haystack.contains(lookFor)) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                return;
            }
        }
        CisDialogs.showInfo(this, "No matching proforma found.");
    }

    private void openPrintDialog() {
        GenericDocumentDAO.BrowseRow row = getSelectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select a proforma row first.");
            return;
        }
        ProformaRecord record = documentDAO.loadProforma(row.documentId());
        new ProformaPrintDialog(SwingUtilities.getWindowAncestor(this), record).setVisible(true);
    }

    private DefaultTableCellRenderer rightAlignedRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
        return renderer;
    }
}
