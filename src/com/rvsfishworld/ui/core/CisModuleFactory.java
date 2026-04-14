package com.rvsfishworld.ui.core;

import com.rvsfishworld.ui.master.CategoryMasterInternalFrame;
import com.rvsfishworld.ui.master.CustomerMasterInternalFrame;
import com.rvsfishworld.ui.master.ProductMasterInternalFrame;
import com.rvsfishworld.ui.master.SpecialOfWeekInternalFrame;
import com.rvsfishworld.ui.master.SupplierMasterInternalFrame;
import com.rvsfishworld.ui.master.TransShipperInternalFrame;
import com.rvsfishworld.ui.generic.ApprovedMasterBrowseInternalFrame;
import com.rvsfishworld.ui.report.ReportBrowseInternalFrame;
import com.rvsfishworld.ui.transaction.LocalSalesInternalFrame;
import com.rvsfishworld.ui.transaction.MortalityInternalFrame;
import com.rvsfishworld.ui.transaction.ProformaInternalFrame;
import com.rvsfishworld.ui.transaction.ReceivingPurchaseInternalFrame;
import com.rvsfishworld.ui.transaction.ReceivingSubStationInternalFrame;
import com.rvsfishworld.ui.transaction.SalesInvoiceInternalFrame;
import com.rvsfishworld.ui.transaction.StockReportInternalFrame;
import javax.swing.JInternalFrame;

public final class CisModuleFactory {
    private CisModuleFactory() {
    }

    public static JInternalFrame create(String key) {
        return switch (key) {
            case "product", "master_product" -> new ProductMasterInternalFrame();
            case "category", "master_category" -> new CategoryMasterInternalFrame();
            case "supplier", "master_supplier" -> new SupplierMasterInternalFrame();
            case "customer", "master_customer" -> new CustomerMasterInternalFrame();
            case "trans", "master_trans_shipper" -> new TransShipperInternalFrame();
            case "receiving_purchase", "trans_receiving_purchase" -> new ReceivingPurchaseInternalFrame();
            case "receiving_substation", "trans_receiving_sub" -> new ReceivingSubStationInternalFrame();
            case "proforma", "trans_proforma" -> new ProformaInternalFrame();
            case "sales_invoice", "trans_sales_invoice" -> new SalesInvoiceInternalFrame();
            case "mortality", "trans_mortality" -> new MortalityInternalFrame();
            case "stock_report", "trans_stock_report" -> new StockReportInternalFrame();
            case "trans_local_sales" -> new LocalSalesInternalFrame();
            case "master_special_week" -> new SpecialOfWeekInternalFrame();
            case "utility_users" -> new com.rvsfishworld.ui.admin.UserRightsInternalFrame();
            case "report_receiving", "report_receiving_sub", "report_sales", "report_sales_summary" ->
                    new ReportBrowseInternalFrame(key);
            case "master_net_prices", "master_stop_week", "master_contract_trans", "master_contract_supplier",
                    "master_flat_price", "master_stop_forever", "master_group" ->
                    ApprovedMasterBrowseInternalFrame.forKey(key);
            default -> null;
        };
    }

    public static JInternalFrame createFrame(String key) {
        return create(key);
    }
}
