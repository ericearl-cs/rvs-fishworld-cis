package com.rvsfishworld.ui.report;

import com.rvsfishworld.ui.generic.DataBrowseInternalFrame;

@SuppressWarnings("serial")
public class ReportBrowseInternalFrame extends DataBrowseInternalFrame {
    public ReportBrowseInternalFrame(String key) {
        super(
                titleFor(key),
                titleFor(key),
                orderLabelsFor(key),
                orderSqlsFor(key),
                new String[]{"Find", "Print", "Refresh", "Exit"},
                columnsFor(key),
                footerFor(key));
    }

    private static String titleFor(String key) {
        return switch (key) {
            case "report_receiving" -> "Receiving Report";
            case "report_receiving_sub" -> "Receiving Sub-Station Report";
            case "report_sales" -> "Sales Report";
            case "report_sales_summary" -> "Sales Summary";
            default -> "Report Browse";
        };
    }

    private static String[] orderLabelsFor(String key) {
        return switch (key) {
            case "report_sales", "report_sales_summary" -> new String[]{"Order by Date", "Order by Customer", "Order by Document No"};
            default -> new String[]{"Order by Date", "Order by Supplier", "Order by R.R. No."};
        };
    }

    private static String[] columnsFor(String key) {
        return switch (key) {
            case "report_sales", "report_sales_summary" -> new String[]{"DOC. NO.", "DATE", "PARTY CODE", "PARTY NAME", "TOTAL PAYABLES", "STATUS"};
            default -> new String[]{"R.R. NO.", "DATE", "SUPPLIER CODE", "SUPPLIER NAME", "TOTAL", "CANCELLED"};
        };
    }

    private static String[] orderSqlsFor(String key) {
        return switch (key) {
            case "report_sales" -> new String[]{
                    "SELECT document_no, DATE_FORMAT(document_date, '%m/%d/%Y'), COALESCE(party_code, ''), COALESCE(party_name, ''), COALESCE(total_payables, amount), status FROM generic_documents WHERE document_type='SALES_INVOICE' ORDER BY document_date DESC, document_no",
                    "SELECT document_no, DATE_FORMAT(document_date, '%m/%d/%Y'), COALESCE(party_code, ''), COALESCE(party_name, ''), COALESCE(total_payables, amount), status FROM generic_documents WHERE document_type='SALES_INVOICE' ORDER BY party_code, document_date DESC",
                    "SELECT document_no, DATE_FORMAT(document_date, '%m/%d/%Y'), COALESCE(party_code, ''), COALESCE(party_name, ''), COALESCE(total_payables, amount), status FROM generic_documents WHERE document_type='SALES_INVOICE' ORDER BY document_no"
            };
            case "report_sales_summary" -> new String[]{
                    "SELECT DATE_FORMAT(document_date, '%m/%d/%Y') AS group_no, DATE_FORMAT(document_date, '%m/%d/%Y'), COALESCE(party_code, ''), COALESCE(party_name, ''), SUM(COALESCE(total_payables, amount)), CONCAT(COUNT(*), ' INVOICES') FROM generic_documents WHERE document_type='SALES_INVOICE' GROUP BY document_date, party_code, party_name ORDER BY document_date DESC, party_code",
                    "SELECT DATE_FORMAT(document_date, '%m/%d/%Y') AS group_no, DATE_FORMAT(document_date, '%m/%d/%Y'), COALESCE(party_code, ''), COALESCE(party_name, ''), SUM(COALESCE(total_payables, amount)), CONCAT(COUNT(*), ' INVOICES') FROM generic_documents WHERE document_type='SALES_INVOICE' GROUP BY party_code, party_name, document_date ORDER BY party_code, document_date DESC",
                    "SELECT MIN(document_no), DATE_FORMAT(document_date, '%m/%d/%Y'), COALESCE(party_code, ''), COALESCE(party_name, ''), SUM(COALESCE(total_payables, amount)), CONCAT(COUNT(*), ' INVOICES') FROM generic_documents WHERE document_type='SALES_INVOICE' GROUP BY document_date, party_code, party_name ORDER BY MIN(document_no)"
            };
            case "report_receiving_sub" -> new String[]{
                    "SELECT rh.rr_no, DATE_FORMAT(rh.date_received, '%m/%d/%Y'), s.supplier_code, s.supplier_name, COALESCE(rh.total_amount,0), CASE WHEN rh.is_cancelled THEN 'YES' ELSE 'NO' END FROM receiving_headers rh JOIN suppliers s ON s.supplier_id = rh.supplier_id JOIN branches b ON b.branch_id = rh.branch_id WHERE UPPER(COALESCE(b.branch_name,'')) NOT LIKE '%MAIN%' ORDER BY rh.date_received DESC, rh.rr_no",
                    "SELECT rh.rr_no, DATE_FORMAT(rh.date_received, '%m/%d/%Y'), s.supplier_code, s.supplier_name, COALESCE(rh.total_amount,0), CASE WHEN rh.is_cancelled THEN 'YES' ELSE 'NO' END FROM receiving_headers rh JOIN suppliers s ON s.supplier_id = rh.supplier_id JOIN branches b ON b.branch_id = rh.branch_id WHERE UPPER(COALESCE(b.branch_name,'')) NOT LIKE '%MAIN%' ORDER BY s.supplier_code, rh.date_received DESC",
                    "SELECT rh.rr_no, DATE_FORMAT(rh.date_received, '%m/%d/%Y'), s.supplier_code, s.supplier_name, COALESCE(rh.total_amount,0), CASE WHEN rh.is_cancelled THEN 'YES' ELSE 'NO' END FROM receiving_headers rh JOIN suppliers s ON s.supplier_id = rh.supplier_id JOIN branches b ON b.branch_id = rh.branch_id WHERE UPPER(COALESCE(b.branch_name,'')) NOT LIKE '%MAIN%' ORDER BY rh.rr_no"
            };
            default -> new String[]{
                    "SELECT rh.rr_no, DATE_FORMAT(rh.date_received, '%m/%d/%Y'), s.supplier_code, s.supplier_name, COALESCE(rh.total_amount,0), CASE WHEN rh.is_cancelled THEN 'YES' ELSE 'NO' END FROM receiving_headers rh JOIN suppliers s ON s.supplier_id = rh.supplier_id ORDER BY rh.date_received DESC, rh.rr_no",
                    "SELECT rh.rr_no, DATE_FORMAT(rh.date_received, '%m/%d/%Y'), s.supplier_code, s.supplier_name, COALESCE(rh.total_amount,0), CASE WHEN rh.is_cancelled THEN 'YES' ELSE 'NO' END FROM receiving_headers rh JOIN suppliers s ON s.supplier_id = rh.supplier_id ORDER BY s.supplier_code, rh.date_received DESC",
                    "SELECT rh.rr_no, DATE_FORMAT(rh.date_received, '%m/%d/%Y'), s.supplier_code, s.supplier_name, COALESCE(rh.total_amount,0), CASE WHEN rh.is_cancelled THEN 'YES' ELSE 'NO' END FROM receiving_headers rh JOIN suppliers s ON s.supplier_id = rh.supplier_id ORDER BY rh.rr_no"
            };
        };
    }

    private static String footerFor(String key) {
        return switch (key) {
            case "report_sales_summary" -> "Sales summary is loaded from live MySQL invoice headers.";
            case "report_sales" -> "Sales report is loaded from live MySQL invoice headers.";
            case "report_receiving_sub" -> "Receiving sub-station report is loaded from live MySQL receiving headers.";
            default -> "Receiving report is loaded from live MySQL receiving headers.";
        };
    }
}
