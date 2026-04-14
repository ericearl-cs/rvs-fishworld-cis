package com.rvsfishworld.ui.master;

import com.rvsfishworld.ui.generic.DataBrowseInternalFrame;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

public class CustomerMasterInternalFrame extends DataBrowseInternalFrame {
    public CustomerMasterInternalFrame() {
        super(
                "Customer Master File",
                "Customer Master File",
                new String[]{"Order by Customer Code", "Order by Customer Name", "Order by Address"},
                new String[]{
                        "SELECT customer_code, customer_name, COALESCE(customer_address, ''), discount_percent FROM customers ORDER BY customer_code",
                        "SELECT customer_code, customer_name, COALESCE(customer_address, ''), discount_percent FROM customers ORDER BY customer_name",
                        "SELECT customer_code, customer_name, COALESCE(customer_address, ''), discount_percent FROM customers ORDER BY customer_address, customer_code"
                },
                new String[]{"Find", "View", "Add", "Edit", "Delete", "Print", "Refresh", "Exit"},
                new String[]{"CUST. CODE", "CUSTOMER NAME", "ADDRESS", "DISCOUNT"},
                "This browse loads live customer rows from MySQL."
        );
    }

    @Override
    protected void handleCommand(String label) {
        switch (label.toUpperCase()) {
            case "ADD" -> openDialog("Add Customer", Map.of(), false);
            case "VIEW" -> openDialog("View Customer", selectedValues(), true);
            case "EDIT" -> openDialog("Edit Customer", selectedValues(), false);
            default -> super.handleCommand(label);
        }
    }

    private void openDialog(String title, Map<String, Object> values, boolean readOnly) {
        CustomerRecordDialog dialog = new CustomerRecordDialog(SwingUtilities.getWindowAncestor(this), title, values, readOnly);
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
        values.put("customer_code", getModel().getValueAt(modelRow, 0));
        values.put("customer_name", getModel().getValueAt(modelRow, 1));
        values.put("customer_address", getModel().getValueAt(modelRow, 2));
        values.put("discount_percent", getModel().getValueAt(modelRow, 3));
        return values;
    }
}

