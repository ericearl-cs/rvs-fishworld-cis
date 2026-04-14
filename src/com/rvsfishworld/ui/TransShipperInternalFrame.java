package com.rvsfishworld.ui;

public class TransShipperInternalFrame extends DataBrowseInternalFrame {
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
}
