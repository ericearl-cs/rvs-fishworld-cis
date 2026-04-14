package com.rvsfishworld.ui;

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
}
