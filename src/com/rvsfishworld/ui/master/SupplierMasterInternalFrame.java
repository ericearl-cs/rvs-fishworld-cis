package com.rvsfishworld.ui.master;

import com.rvsfishworld.ui.generic.DataBrowseInternalFrame;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

public class SupplierMasterInternalFrame extends DataBrowseInternalFrame {
    public SupplierMasterInternalFrame() {
        super(
                "Supplier Master File",
                "Supplier Master File",
                new String[]{"Order by Supplier Code", "Order by Supplier Name", "Order by Address"},
                new String[]{
                        "SELECT supplier_code, supplier_name, COALESCE(supplier_address, '') FROM suppliers ORDER BY supplier_code",
                        "SELECT supplier_code, supplier_name, COALESCE(supplier_address, '') FROM suppliers ORDER BY supplier_name",
                        "SELECT supplier_code, supplier_name, COALESCE(supplier_address, '') FROM suppliers ORDER BY supplier_address, supplier_code"
                },
                new String[]{"Find", "View", "Add", "Edit", "Delete", "Print", "Refresh", "Exit"},
                new String[]{"SUPP. CODE", "SUPPLIER NAME", "ADDRESS"},
                "This browse loads live supplier rows from MySQL."
        );
    }

    @Override
    protected void handleCommand(String label) {
        switch (label.toUpperCase()) {
            case "ADD" -> openDialog("Add Supplier", Map.of(), false);
            case "VIEW" -> openDialog("View Supplier", selectedValues(), true);
            case "EDIT" -> openDialog("Edit Supplier", selectedValues(), false);
            default -> super.handleCommand(label);
        }
    }

    private void openDialog(String title, Map<String, Object> values, boolean readOnly) {
        SupplierRecordDialog dialog = new SupplierRecordDialog(SwingUtilities.getWindowAncestor(this), title, values, readOnly);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadRows();
        }
    }

    private Map<String, Object> selectedValues() {
        int row = getTable().getSelectedRow();
        if (row < 0) {
            return Map.of();
        }
        int modelRow = getTable().convertRowIndexToModel(row);
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("supplier_code", getModel().getValueAt(modelRow, 0));
        values.put("supplier_name", getModel().getValueAt(modelRow, 1));
        values.put("supplier_address", getModel().getValueAt(modelRow, 2));
        return values;
    }
}

