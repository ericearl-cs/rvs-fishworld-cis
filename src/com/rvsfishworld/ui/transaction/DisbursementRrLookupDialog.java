package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.dao.ReceivingDAO;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class DisbursementRrLookupDialog extends FoxProChildDialog {
    private final ReceivingDAO receivingDAO = new ReceivingDAO();
    private final JTextField txtFind = FoxProTheme.createTextField(16);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "R.R. No.", "Supplier", "Date", "Balance"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final List<ReceivingDAO.BrowseRow> rows = new ArrayList<>();
    private ReceivingDAO.BrowseRow selectedRow;

    public DisbursementRrLookupDialog(Window owner) {
        super(owner, "Disbursement R.R. Lookup", 900, 520);
        setContentPane(buildContent());
        loadRows();
    }

    public ReceivingDAO.BrowseRow getSelectedRow() {
        return selectedRow;
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setBackground(FoxProTheme.PANEL);
        top.add(txtFind, BorderLayout.CENTER);
        JButton find = FoxProTheme.createButton("Find");
        find.addActionListener(e -> loadRows());
        top.add(find, BorderLayout.EAST);

        JPanel commandBar = new JPanel();
        commandBar.setLayout(new BoxLayout(commandBar, BoxLayout.Y_AXIS));
        commandBar.setBackground(FoxProTheme.PANEL);
        addCommand(commandBar, "Select", e -> selectCurrent());
        addCommand(commandBar, "Find", e -> loadRows());
        addCommand(commandBar, "Exit", e -> dispose());

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        FoxProTheme.styleTable(table);
        table.removeColumn(table.getColumnModel().getColumn(0));
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectCurrent();
                }
            }
        });

        root.add(top, BorderLayout.NORTH);
        root.add(commandBar, BorderLayout.WEST);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        return root;
    }

    private void addCommand(JPanel panel, String label, java.awt.event.ActionListener listener) {
        JButton button = FoxProTheme.createButton(label);
        button.setMaximumSize(new Dimension(106, 32));
        button.addActionListener(listener);
        panel.add(button);
        panel.add(Box.createVerticalStrut(8));
    }

    private void loadRows() {
        String keyword = txtFind.getText() == null ? "" : txtFind.getText().trim().toUpperCase();
        model.setRowCount(0);
        rows.clear();
        for (ReceivingDAO.BrowseRow row : receivingDAO.findBrowseRows("DATE")) {
            String haystack = (row.getRrNo() + " " + row.getSupplierCode() + " " + row.getSupplierName()).toUpperCase();
            if (!keyword.isBlank() && !haystack.contains(keyword)) {
                continue;
            }
            rows.add(row);
            model.addRow(new Object[]{
                    row.getReceivingId(),
                    row.getRrNo(),
                    row.getSupplierCode() + " - " + row.getSupplierName(),
                    row.getDateText(),
                    row.getBalance()
            });
        }
    }

    private void selectCurrent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedRow = rows.get(table.convertRowIndexToModel(row));
        dispose();
    }
}
