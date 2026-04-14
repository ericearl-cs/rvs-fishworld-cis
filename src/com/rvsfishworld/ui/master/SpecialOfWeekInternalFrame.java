package com.rvsfishworld.ui.master;

import com.rvsfishworld.db.Database;
import com.rvsfishworld.ui.core.CisDialogs;
import com.rvsfishworld.ui.generic.DataBrowseInternalFrame;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class SpecialOfWeekInternalFrame extends DataBrowseInternalFrame {
    public SpecialOfWeekInternalFrame() {
        super(
                "Special Of Week",
                "Special Of Week",
                new String[]{"Order by Product", "Order by Start Date"},
                new String[]{
                        "SELECT special_week_id, product_code, special_price, COALESCE(DATE_FORMAT(start_date, '%m/%d/%Y'), ''), COALESCE(DATE_FORMAT(end_date, '%m/%d/%Y'), ''), COALESCE(remarks, ''), CASE WHEN is_active THEN 'YES' ELSE 'NO' END FROM special_of_week ORDER BY product_code, start_date, end_date",
                        "SELECT special_week_id, product_code, special_price, COALESCE(DATE_FORMAT(start_date, '%m/%d/%Y'), ''), COALESCE(DATE_FORMAT(end_date, '%m/%d/%Y'), ''), COALESCE(remarks, ''), CASE WHEN is_active THEN 'YES' ELSE 'NO' END FROM special_of_week ORDER BY start_date DESC, product_code"
                },
                new String[]{"Find", "Add", "Edit", "Delete", "Refresh", "Exit"},
                new String[]{"ID", "PRODUCT", "SPECIAL PRICE", "START DATE", "END DATE", "REMARKS", "ACTIVE"},
                "This browse loads live special-of-week rows from MySQL.");
        var column = getTable().getColumnModel().getColumn(0);
        column.setMinWidth(0);
        column.setMaxWidth(0);
        column.setPreferredWidth(0);
    }

    @Override
    protected void handleCommand(String label) {
        switch (label.toUpperCase()) {
            case "ADD" -> openDialog("Add Special Price", Map.of());
            case "EDIT" -> {
                Map<String, Object> values = selectedValues();
                if (values.isEmpty()) {
                    CisDialogs.showInfo(this, "Select a special-price row first.");
                    return;
                }
                openDialog("Edit Special Price", values);
            }
            case "DELETE" -> deleteSelected();
            default -> super.handleCommand(label);
        }
    }

    private void openDialog(String title, Map<String, Object> values) {
        SpecialOfWeekEditDialog dialog = new SpecialOfWeekEditDialog(SwingUtilities.getWindowAncestor(this), title, values);
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
        values.put("special_week_id", getModel().getValueAt(modelRow, 0));
        values.put("product_code", getModel().getValueAt(modelRow, 1));
        values.put("special_price", getModel().getValueAt(modelRow, 2));
        values.put("start_date", getModel().getValueAt(modelRow, 3));
        values.put("end_date", getModel().getValueAt(modelRow, 4));
        values.put("remarks", getModel().getValueAt(modelRow, 5));
        values.put("is_active", "YES".equalsIgnoreCase(String.valueOf(getModel().getValueAt(modelRow, 6))));
        return values;
    }

    private void deleteSelected() {
        Map<String, Object> values = selectedValues();
        if (values.isEmpty()) {
            CisDialogs.showInfo(this, "Select a special-price row first.");
            return;
        }
        if (CisDialogs.askYesNo(this, "Delete Special", "Delete selected special-of-week row?") != CisDialogs.YES) {
            return;
        }
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM special_of_week WHERE special_week_id = ?")) {
            ps.setLong(1, Long.parseLong(String.valueOf(values.get("special_week_id"))));
            ps.executeUpdate();
            loadRows();
        } catch (Exception e) {
            CisDialogs.showError(this, e.getMessage());
        }
    }
}
