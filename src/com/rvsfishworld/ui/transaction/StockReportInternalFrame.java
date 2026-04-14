package com.rvsfishworld.ui.transaction;

import com.rvsfishworld.ui.generic.DataBrowseInternalFrame;

@SuppressWarnings("serial")
public class StockReportInternalFrame extends DataBrowseInternalFrame {
    public StockReportInternalFrame() {
        super(
                "Stock Report",
                "Stock Report",
                new String[]{"Order by Product No", "Order by Description", "Order by Category"},
                new String[]{
                        "SELECT product_code, description, COALESCE(scientific_name, ''), COALESCE(total_quantity, 0), COALESCE(average_cost, 0), COALESCE(price_b, 0) FROM products ORDER BY product_code",
                        "SELECT product_code, description, COALESCE(scientific_name, ''), COALESCE(total_quantity, 0), COALESCE(average_cost, 0), COALESCE(price_b, 0) FROM products ORDER BY description, product_code",
                        "SELECT p.product_code, p.description, COALESCE(p.scientific_name, ''), COALESCE(p.total_quantity, 0), COALESCE(p.average_cost, 0), COALESCE(p.price_b, 0) FROM products p LEFT JOIN categories c ON c.category_id = p.category_id ORDER BY c.category_code, p.product_code"
                },
                new String[]{"Find", "View", "Print", "Refresh", "Exit"},
                new String[]{"PRODUCT", "DESCRIPTION", "SCIENTIFIC NAME", "QTY.", "AVERAGE COST", "PRICE B"},
                "This browse loads live stock rows from MySQL.");
    }
}
