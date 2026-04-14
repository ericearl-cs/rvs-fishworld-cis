package com.rvsfishworld.ui;

public class CategoryMasterInternalFrame extends DataBrowseInternalFrame {
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
}
