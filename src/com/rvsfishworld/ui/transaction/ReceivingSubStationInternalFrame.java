package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.generic.DataBrowseInternalFrame;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class ReceivingSubStationInternalFrame extends DataBrowseInternalFrame {
    public ReceivingSubStationInternalFrame() {
        super(
                "Receiving Sub Station",
                "Receiving Sub Station",
                new String[]{"Order by R.R. No.", "Order by Branch", "Order by Date"},
                new String[]{
                        "SELECT rh.rr_no, b.branch_code, b.branch_name, s.supplier_code, s.supplier_name, DATE_FORMAT(rh.date_received, '%m/%d/%Y'), COALESCE(rh.total_amount, 0), COALESCE(rh.amount_paid, 0), COALESCE(rh.cash_on_hand, rh.total_amount) FROM receiving_headers rh JOIN branches b ON b.branch_id = rh.branch_id JOIN suppliers s ON s.supplier_id = rh.supplier_id WHERE UPPER(COALESCE(b.branch_name, '')) NOT LIKE '%MAIN%' ORDER BY rh.rr_no",
                        "SELECT rh.rr_no, b.branch_code, b.branch_name, s.supplier_code, s.supplier_name, DATE_FORMAT(rh.date_received, '%m/%d/%Y'), COALESCE(rh.total_amount, 0), COALESCE(rh.amount_paid, 0), COALESCE(rh.cash_on_hand, rh.total_amount) FROM receiving_headers rh JOIN branches b ON b.branch_id = rh.branch_id JOIN suppliers s ON s.supplier_id = rh.supplier_id WHERE UPPER(COALESCE(b.branch_name, '')) NOT LIKE '%MAIN%' ORDER BY b.branch_code, rh.date_received DESC, rh.rr_no",
                        "SELECT rh.rr_no, b.branch_code, b.branch_name, s.supplier_code, s.supplier_name, DATE_FORMAT(rh.date_received, '%m/%d/%Y'), COALESCE(rh.total_amount, 0), COALESCE(rh.amount_paid, 0), COALESCE(rh.cash_on_hand, rh.total_amount) FROM receiving_headers rh JOIN branches b ON b.branch_id = rh.branch_id JOIN suppliers s ON s.supplier_id = rh.supplier_id WHERE UPPER(COALESCE(b.branch_name, '')) NOT LIKE '%MAIN%' ORDER BY rh.date_received DESC, rh.rr_no"
                },
                new String[]{"Find", "Add", "Edit", "Print", "Refresh", "Exit"},
                new String[]{"R.R. NO.", "BRANCH", "BRANCH NAME", "SUPPLIER", "SUPPLIER NAME", "DATE", "TOTAL", "AMOUNT PAID", "CASH ON HAND"},
                "This browse loads sub-station receiving rows from MySQL.");
    }

    @Override
    protected void handleCommand(String label) {
        switch (label.toUpperCase()) {
            case "ADD", "EDIT" -> CisDialogs.showInfo(this, "Receiving Sub-Station entry wiring is next after the current receiving core flow.");
            case "PRINT" -> openPrint();
            default -> super.handleCommand(label);
        }
    }

    private void openPrint() {
        int row = getTable().getSelectedRow();
        if (row < 0) {
            CisDialogs.showInfo(this, "Select a sub-station receiving row first.");
            return;
        }
        int modelRow = getTable().convertRowIndexToModel(row);
        String preview = """
                RECEIVING SUB STATION
                R.R. No: %s
                Branch: %s - %s
                Supplier: %s - %s
                Date: %s
                Total: %s
                Amount Paid: %s
                Cash On Hand: %s
                """.formatted(
                getModel().getValueAt(modelRow, 0),
                getModel().getValueAt(modelRow, 1),
                getModel().getValueAt(modelRow, 2),
                getModel().getValueAt(modelRow, 3),
                getModel().getValueAt(modelRow, 4),
                getModel().getValueAt(modelRow, 5),
                getModel().getValueAt(modelRow, 6),
                getModel().getValueAt(modelRow, 7),
                getModel().getValueAt(modelRow, 8));
        new ReceivingSubStationPrintDialog(SwingUtilities.getWindowAncestor(this), preview).setVisible(true);
    }
}
