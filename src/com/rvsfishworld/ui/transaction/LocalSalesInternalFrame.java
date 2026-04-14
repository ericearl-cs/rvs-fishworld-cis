package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.generic.DataBrowseInternalFrame;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class LocalSalesInternalFrame extends DataBrowseInternalFrame {
    public LocalSalesInternalFrame() {
        super(
                "Local Sales",
                "Local Sales",
                new String[]{"Order by Document No", "Order by Customer", "Order by Date"},
                new String[]{
                        "SELECT document_no, COALESCE(party_code, ''), COALESCE(party_name, ''), DATE_FORMAT(document_date, '%m/%d/%Y'), COALESCE(amount, 0), status FROM generic_documents WHERE document_type = 'LOCAL_SALES' ORDER BY document_no",
                        "SELECT document_no, COALESCE(party_code, ''), COALESCE(party_name, ''), DATE_FORMAT(document_date, '%m/%d/%Y'), COALESCE(amount, 0), status FROM generic_documents WHERE document_type = 'LOCAL_SALES' ORDER BY party_code, document_date DESC",
                        "SELECT document_no, COALESCE(party_code, ''), COALESCE(party_name, ''), DATE_FORMAT(document_date, '%m/%d/%Y'), COALESCE(amount, 0), status FROM generic_documents WHERE document_type = 'LOCAL_SALES' ORDER BY document_date DESC, document_no"
                },
                new String[]{"Find", "Add", "Edit", "Print", "Refresh", "Exit"},
                new String[]{"DOC. NO.", "CUSTOMER", "CUSTOMER NAME", "DATE", "AMOUNT", "STATUS"},
                "This browse loads live local-sales headers from MySQL.");
    }

    @Override
    protected void handleCommand(String label) {
        switch (label.toUpperCase()) {
            case "ADD", "EDIT" -> CisDialogs.showInfo(this, "Local-sales entry is next after the current core transaction flows.");
            case "PRINT" -> openPrint();
            default -> super.handleCommand(label);
        }
    }

    private void openPrint() {
        int row = getTable().getSelectedRow();
        if (row < 0) {
            CisDialogs.showInfo(this, "Select a local-sales row first.");
            return;
        }
        int modelRow = getTable().convertRowIndexToModel(row);
        String preview = """
                LOCAL SALES
                Document No: %s
                Customer: %s - %s
                Date: %s
                Amount: %s
                Status: %s
                """.formatted(
                getModel().getValueAt(modelRow, 0),
                getModel().getValueAt(modelRow, 1),
                getModel().getValueAt(modelRow, 2),
                getModel().getValueAt(modelRow, 3),
                getModel().getValueAt(modelRow, 4),
                getModel().getValueAt(modelRow, 5));
        new LocalSalesPrintDialog(SwingUtilities.getWindowAncestor(this), preview).setVisible(true);
    }
}
