package com.rvsfishworld.ui.master;

import com.rvsfishworld.ui.generic.DataBrowseInternalFrame;
import com.rvsfishworld.ui.core.CisScale;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

public class ProductMasterInternalFrame extends DataBrowseInternalFrame {
    private static final String PRODUCT_BROWSE_SQL = """
            SELECT
                p.product_code,
                p.description,
                COALESCE(p.scientific_name, ''),
                COALESCE(c.category_code, ''),
                COALESCE(p.total_quantity, 0),
                COALESCE(p.previous_cost, 0),
                COALESCE(p.average_cost, 0),
                COALESCE(p.price_a, 0),
                COALESCE(p.price_b, 0),
                COALESCE(p.price_c, 0),
                COALESCE(p.price_d, 0),
                COALESCE(p.price_e, 0),
                COALESCE(p.price_f, 0),
                COALESCE(p.price_g, 0),
                COALESCE(p.special_price, 0),
                COALESCE(p.local_sales_price, 0),
                COALESCE(p.deliveries_price, 0)
            FROM products p
            LEFT JOIN categories c ON c.category_id = p.category_id
            """;

    public ProductMasterInternalFrame() {
        super(
                "Product Master File",
                "Product Master File",
                new String[]{"Order by Product No", "Order by Description", "Order by Scientific Name"},
                new String[]{
                        PRODUCT_BROWSE_SQL + " ORDER BY p.product_code",
                        PRODUCT_BROWSE_SQL + " ORDER BY p.description, p.product_code",
                        PRODUCT_BROWSE_SQL + " ORDER BY p.scientific_name, p.product_code"
                },
                new String[]{"Find", "View", "Add", "Edit", "Delete", "Print", "Component", "Exit"},
                new String[]{
                        "CODE", "DESCRIPTION", "SCIENTIFIC NAME", "CATEGORY", "TOTAL QTY.",
                        "PREVIOUS COST", "AVERAGE COST",
                        "PRICE A", "PRICE B", "PRICE C", "PRICE D", "PRICE E", "PRICE F", "PRICE G",
                        "SPECIAL", "LOCAL SALES", "DST"
                },
                ""
        );
        setSize(CisScale.scale(1260), CisScale.scale(700));
        setColumnWidths(
                78, 170, 170, 72, 92,
                102, 102,
                78, 78, 78, 78, 78, 78, 78,
                78, 104, 84
        );
        rightAlignColumns(4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
    }

    @Override
    protected void handleCommand(String label) {
        switch (label.toUpperCase()) {
            case "ADD" -> openDialog("Add Product", Map.of(), false);
            case "VIEW" -> openDialog("View Product", selectedValues(), true);
            case "EDIT" -> openDialog("Edit Product", selectedValues(), false);
            default -> super.handleCommand(label);
        }
    }

    private void openDialog(String title, Map<String, Object> values, boolean readOnly) {
        ProductRecordDialog dialog = new ProductRecordDialog(SwingUtilities.getWindowAncestor(this), title, values, readOnly);
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
        values.put("product_code", getModel().getValueAt(modelRow, 0));
        values.put("description", getModel().getValueAt(modelRow, 1));
        values.put("scientific_name", getModel().getValueAt(modelRow, 2));
        values.put("category_code", getModel().getValueAt(modelRow, 3));
        values.put("total_quantity", getModel().getValueAt(modelRow, 4));
        values.put("previous_cost", getModel().getValueAt(modelRow, 5));
        values.put("average_cost", getModel().getValueAt(modelRow, 6));
        values.put("price_a", getModel().getValueAt(modelRow, 7));
        values.put("price_b", getModel().getValueAt(modelRow, 8));
        values.put("price_c", getModel().getValueAt(modelRow, 9));
        values.put("price_d", getModel().getValueAt(modelRow, 10));
        values.put("price_e", getModel().getValueAt(modelRow, 11));
        values.put("price_f", getModel().getValueAt(modelRow, 12));
        values.put("price_g", getModel().getValueAt(modelRow, 13));
        values.put("special_price", getModel().getValueAt(modelRow, 14));
        values.put("local_sales_price", getModel().getValueAt(modelRow, 15));
        values.put("deliveries_price", getModel().getValueAt(modelRow, 16));
        return values;
    }
}

