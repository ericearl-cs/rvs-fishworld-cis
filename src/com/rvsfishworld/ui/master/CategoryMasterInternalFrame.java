package com.rvsfishworld.ui.master;

import com.rvsfishworld.dao.MasterFileDAO;
import com.rvsfishworld.ui.generic.DataBrowseInternalFrame;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

public class CategoryMasterInternalFrame extends DataBrowseInternalFrame {
    private final MasterFileDAO dao = new MasterFileDAO();

    public CategoryMasterInternalFrame() {
        super(
                "Category Master File",
                "Category Master File",
                new String[]{"Order by Category Code", "Order by Category Name", "Order by Sort"},
                new String[]{
                        "SELECT category_code, category_name, COALESCE(sort_code, '') FROM categories ORDER BY category_code",
                        "SELECT category_code, category_name, COALESCE(sort_code, '') FROM categories ORDER BY category_name",
                        "SELECT category_code, category_name, COALESCE(sort_code, '') FROM categories ORDER BY sort_code, category_code"
                },
                new String[]{"Find", "View", "Add", "Edit", "Delete", "Print", "Refresh", "Exit"},
                new String[]{"CODE", "CATEGORY NAME", "SORT"},
                "This browse loads live category rows from MySQL."
        );
    }

    @Override
    protected void handleCommand(String label) {
        switch (label.toUpperCase()) {
            case "ADD" -> openDialog("Add Category", Map.of(), false);
            case "VIEW" -> openDialog("View Category", selectedValues(), true);
            case "EDIT" -> openDialog("Edit Category", selectedValues(), false);
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
                List.of("category_code", "category_name", "sort_code"),
                Map.of(
                        "category_code", "Category Code",
                        "category_name", "Category Name",
                        "sort_code", "Sort Code"),
                List.of("category_code", "category_name"));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            dao.saveCategory(dialog.values());
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
        values.put("category_code", getModel().getValueAt(modelRow, 0));
        values.put("category_name", getModel().getValueAt(modelRow, 1));
        values.put("sort_code", getModel().getValueAt(modelRow, 2));
        return values;
    }

    private void deleteSelected() {
        Map<String, Object> values = selectedValues();
        Object code = values.get("category_code");
        if (code == null || code.toString().isBlank()) {
            return;
        }
        dao.deleteCategory(code.toString());
        loadRows();
    }
}

