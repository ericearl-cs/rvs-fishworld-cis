package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.dao.SalesWorkflowDAO;
import com.rvsfishworld.model.SalesInvoiceRecord;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.FoxProTheme;
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
public class SalesInvoiceInternalFrame extends JInternalFrame {
    private final SalesWorkflowDAO workflowDAO = new SalesWorkflowDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Invoice No.", "Customer Code", "Customer Name", "Date", "Total Payables", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final List<SalesWorkflowDAO.BrowseRow> browseRows = new ArrayList<>();
    private final JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private final String[] orderLabels = {"Order by Invoice No.", "by Customer Code", "by Date"};
    private final String[] orderKeys = {"INVOICE", "CUSTOMER", "DATE"};
    private int selectedOrderIndex = 2;
    private final DecimalFormat amountFormat = new DecimalFormat("#,##0.00");

    public SalesInvoiceInternalFrame() {
        super("Invoicing", true, true, true, true);
        FoxProTheme.applyGlobalFont();
        setSize(CisScale.scale(1180), CisScale.scale(690));
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
        addCommand(panel, "Print", e -> openPrint());
        addCommand(panel, "Find", e -> loadRows());
        addCommand(panel, "Cancel/Recall", e -> toggleCancelRecall());
        addCommand(panel, "Exit", e -> doDefaultCloseAction());

        JButton cover = FoxProTheme.createButton("Cover Invoice");
        cover.setMaximumSize(new Dimension(CisScale.scale(120), CisScale.scale(38)));
        cover.addActionListener(e -> openCoverInvoice());
        panel.add(Box.createVerticalStrut(CisScale.scale(10)));
        panel.add(cover);
        return panel;
    }

    private void addCommand(JPanel panel, String label, java.awt.event.ActionListener listener) {
        JButton button = FoxProTheme.createButton(label);
        button.setMaximumSize(new Dimension(CisScale.scale(120), CisScale.scale(34)));
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
        browseRows.clear();
        try {
            browseRows.addAll(workflowDAO.browseSalesInvoices(orderKeys[selectedOrderIndex]));
            for (SalesWorkflowDAO.BrowseRow row : browseRows) {
                model.addRow(new Object[]{
                        row.documentNo(),
                        row.customerCode(),
                        row.customerName(),
                        row.documentDate() == null ? "" : row.documentDate().toString(),
                        amountFormat.format(row.totalPayables()),
                        row.status()
                });
            }
        } catch (Exception e) {
            model.addRow(new Object[]{"ERROR", "", e.getMessage(), "", "", ""});
        }
    }

    private void openAddDialog() {
        SalesInvoiceEntryDialog dialog = new SalesInvoiceEntryDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadRows();
        }
    }

    private void openEditDialog() {
        SalesWorkflowDAO.BrowseRow row = getSelectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select an invoice first.");
            return;
        }
        SalesInvoiceRecord record = workflowDAO.loadInvoice(row.documentId());
        SalesInvoiceEntryDialog dialog = new SalesInvoiceEntryDialog(SwingUtilities.getWindowAncestor(this), record);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadRows();
        }
    }

    private void openPrint() {
        SalesWorkflowDAO.BrowseRow row = getSelectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select an invoice first.");
            return;
        }
        String preview = """
                SALES INVOICE
                Invoice No: %s
                Customer: %s - %s
                Date: %s
                Total Payables: %s
                Status: %s
                """.formatted(
                row.documentNo(),
                row.customerCode(),
                row.customerName(),
                row.documentDate() == null ? "" : row.documentDate(),
                amountFormat.format(row.totalPayables()),
                row.status());
        new SalesInvoicePrintDialog(SwingUtilities.getWindowAncestor(this), preview).setVisible(true);
    }

    private SalesWorkflowDAO.BrowseRow getSelectedRow() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= browseRows.size()) {
            return null;
        }
        return browseRows.get(row);
    }

    private void deleteCurrent() {
        SalesWorkflowDAO.BrowseRow row = getSelectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select an invoice first.");
            return;
        }
        if (CisDialogs.askYesNo(this, "Delete Sales Invoice", "Delete selected invoice?") != CisDialogs.YES) {
            return;
        }
        try {
            workflowDAO.deleteInvoice(row.documentId());
            loadRows();
        } catch (RuntimeException e) {
            CisDialogs.showError(this, e.getMessage());
        }
    }

    private void toggleCancelRecall() {
        SalesWorkflowDAO.BrowseRow row = getSelectedRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, "Select an invoice first.");
            return;
        }
        String action = "CANCELLED".equalsIgnoreCase(row.status()) ? "Recall" : "Cancel";
        if (CisDialogs.askYesNo(this, action + " Sales Invoice", action + " selected invoice?") != CisDialogs.YES) {
            return;
        }
        try {
            workflowDAO.toggleCancelRecall(row.documentId());
            loadRows();
        } catch (RuntimeException e) {
            CisDialogs.showError(this, e.getMessage());
        }
    }

    private void openCoverInvoice() {
        new CoverInvoiceDialog(SwingUtilities.getWindowAncestor(this)).setVisible(true);
    }
}
