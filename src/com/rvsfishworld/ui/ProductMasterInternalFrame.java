package com.rvsfishworld.ui;

public class ProductMasterInternalFrame extends DataBrowseInternalFrame {
    public ProductMasterInternalFrame() {
        super(
                "Product Master File",
                "Product Master File",
                new String[]{"Order by Product No", "Order by Description", "Order by Scientific Name"},
                new String[]{
                        "SELECT p.product_code, p.description, COALESCE(p.scientific_name, ''), c.category_code, p.total_quantity, p.price_b FROM products p JOIN categories c ON c.category_id = p.category_id ORDER BY p.product_code",
                        "SELECT p.product_code, p.description, COALESCE(p.scientific_name, ''), c.category_code, p.total_quantity, p.price_b FROM products p JOIN categories c ON c.category_id = p.category_id ORDER BY p.description",
                        "SELECT p.product_code, p.description, COALESCE(p.scientific_name, ''), c.category_code, p.total_quantity, p.price_b FROM products p JOIN categories c ON c.category_id = p.category_id ORDER BY p.scientific_name, p.product_code"
                },
                new String[]{"Find", "View", "Add", "Edit", "Delete", "Print", "Component", "Refresh", "Exit"},
                new String[]{"CODE", "DESCRIPTION", "SCIENTIFIC NAME", "CATEGORY", "TOTAL QTY.", "PRICE B"},
                "This browse loads live product rows from MySQL."
        );
    }
}
