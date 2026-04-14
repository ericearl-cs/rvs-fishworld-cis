package com.rvsfishworld.ui;

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
}
