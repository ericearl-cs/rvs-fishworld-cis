package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.db.Database;
import com.rvsfishworld.ui.FoxProTheme;
import com.rvsfishworld.ui.chrome.FoxProChildDialog;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class CoverInvoiceDialog extends FoxProChildDialog {
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Invoice No.", "Proforma No.", "Customer", "Date", "Total Payables", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public CoverInvoiceDialog(Window owner) {
        super(owner, "Cover Invoice", 840, 420);
        setContentPane(buildContent());
        loadRows();
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(FoxProTheme.PANEL);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTable table = new JTable(model);
        FoxProTheme.styleTable(table);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actions.setBackground(FoxProTheme.PANEL);
        JButton close = FoxProTheme.createButton("Close");
        close.addActionListener(e -> dispose());
        actions.add(close);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    private void loadRows() {
        model.setRowCount(0);
        String sql = """
                SELECT COALESCE(invoice_no, document_no), COALESCE(source_proforma_no, ''), COALESCE(party_name, ''),
                       DATE_FORMAT(document_date, '%m/%d/%Y'), COALESCE(total_payables, amount), status
                FROM generic_documents
                WHERE document_type = 'SALES_INVOICE'
                ORDER BY document_date DESC, document_no DESC
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getBigDecimal(5),
                        rs.getString(6)
                });
            }
        } catch (Exception e) {
            model.addRow(new Object[]{"ERROR", "", e.getMessage(), "", "", ""});
        }
    }
}
