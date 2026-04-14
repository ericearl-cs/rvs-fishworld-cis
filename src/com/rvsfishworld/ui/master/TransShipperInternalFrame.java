package com.rvsfishworld.ui.master;

import com.rvsfishworld.dao.MasterFileDAO;
import com.rvsfishworld.ui.generic.DataBrowseInternalFrame;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

public class TransShipperInternalFrame extends DataBrowseInternalFrame {
    private final MasterFileDAO dao = new MasterFileDAO();

    public TransShipperInternalFrame() {
        super(
                "Tran-Shipper Master File",
                "Tran-Shipper Master File",
                new String[]{"Order by Code", "Order by Name"},
                new String[]{
                        "SELECT trans_shipper_code, trans_shipper_name, COALESCE(legacy_parent_customer_code, '') FROM trans_shippers ORDER BY trans_shipper_code",
                        "SELECT trans_shipper_code, trans_shipper_name, COALESCE(legacy_parent_customer_code, '') FROM trans_shippers ORDER BY trans_shipper_name"
                },
                new String[]{"Find", "View", "Add", "Edit", "Delete", "Print", "Refresh", "Exit"},
                new String[]{"CODE", "NAME", "PARENT"},
                "This browse loads live tran-shipper rows from MySQL."
        );
    }

    @Override
    protected void handleCommand(String label) {
        switch (label.toUpperCase()) {
            case "ADD" -> openDialog("Add Tran-Shipper", Map.of(), false);
            case "VIEW" -> openDialog("View Tran-Shipper", selectedValues(), true);
            case "EDIT" -> openDialog("Edit Tran-Shipper", selectedValues(), false);
            case "DELETE" -> deleteSelected();
            default -> super.handleCommand(label);
        }
    }

    private void openDialog(String title, Map<String, Object> values, boolean readOnly) {
        SimpleMasterRecordDialog dialog = new SimpleMasterRecordDialog(
                SwingUtilities.getWindowAncestor(this),
                title,
                values,
                readOnly,
                List.of("trans_shipper_code", "trans_shipper_name", "legacy_parent_customer_code"),
                Map.of(
                        "trans_shipper_code", "Tran-Shipper Code",
                        "trans_shipper_name", "Tran-Shipper Name",
                        "legacy_parent_customer_code", "Parent Customer"),
                List.of("trans_shipper_code", "trans_shipper_name"));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            dao.saveTransShipper(dialog.values());
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
        values.put("trans_shipper_code", getModel().getValueAt(modelRow, 0));
        values.put("trans_shipper_name", getModel().getValueAt(modelRow, 1));
        values.put("legacy_parent_customer_code", getModel().getValueAt(modelRow, 2));
        return values;
    }

    private void deleteSelected() {
        Map<String, Object> values = selectedValues();
        Object code = values.get("trans_shipper_code");
        if (code == null || code.toString().isBlank()) {
            return;
        }
        dao.deleteTransShipper(code.toString());
        loadRows();
    }
}

