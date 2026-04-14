package com.rvsfishworld.dao;

import com.rvsfishworld.db.Database;
import com.rvsfishworld.model.SalesInvoiceLine;
import com.rvsfishworld.model.SalesInvoiceRecord;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesWorkflowDAO {
    public record BrowseRow(
            long documentId,
            String documentNo,
            String customerCode,
            String customerName,
            LocalDate documentDate,
            BigDecimal totalPayables,
            String status) {
    }

    public List<BrowseRow> browseSalesInvoices(String orderKey) {
        return browseDocuments("SALES_INVOICE", orderKey, null);
    }

    public List<BrowseRow> browseProformas(String orderKey) {
        return browseDocuments("PROFORMA", orderKey, null);
    }

    public List<BrowseRow> findAvailableProformas(String keyword) {
        return browseDocuments("PROFORMA", "DATE", keyword);
    }

    public SalesInvoiceRecord loadInvoice(long documentId) {
        try (Connection conn = Database.getConnection()) {
            return loadDocument(conn, "SALES_INVOICE", "document_id = ?", ps -> ps.setLong(1, documentId));
        } catch (Exception e) {
            throw new RuntimeException("Unable to load Sales Invoice: " + e.getMessage(), e);
        }
    }

    public SalesInvoiceRecord loadFromProforma(String proformaNo) {
        SalesInvoiceRecord record = new SalesInvoiceRecord();
        record.setInvoiceDate(LocalDate.now());
        record.setCurrencyCode("USD");
        record.setCurrencyName("AMERICAN DOLLAR");
        record.setPricingCode("B");
        record.setApplyFormula(true);
        if (proformaNo == null || proformaNo.isBlank()) {
            return record;
        }
        try (Connection conn = Database.getConnection()) {
            SalesInvoiceRecord loaded = loadDocument(conn, "PROFORMA", "document_no = ?", ps -> ps.setString(1, proformaNo.trim()));
            if (loaded == null) {
                record.setProformaNo(proformaNo.trim());
                record.setSourceProformaNo(proformaNo.trim());
                record.setInvoiceNo(nextInvoiceNo(conn));
                return record;
            }
            long sourceId = loaded.getId();
            loaded.setId(0);
            loaded.setInvoiceNo(nextInvoiceNo(conn));
            loaded.setSourceProformaId(sourceId);
            loaded.setSourceProformaNo(loaded.getProformaNo());
            loaded.setProformaNo(loaded.getProformaNo());
            loaded.setInvoiceDate(LocalDate.now());
            applyFormula(loaded);
            return loaded;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load Proforma: " + e.getMessage(), e);
        }
    }

    public SalesInvoiceRecord applyFormula(SalesInvoiceRecord record) {
        try (Connection conn = Database.getConnection()) {
            for (SalesInvoiceLine line : record.getLines()) {
                applyPricing(conn, record, line);
                line.recompute();
            }
            recomputeTotals(record);
            return record;
        } catch (Exception e) {
            throw new RuntimeException("Unable to apply Sales Invoice formula: " + e.getMessage(), e);
        }
    }

    public void save(SalesInvoiceRecord record) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                applyFormula(record);
                long documentId = upsertHeader(conn, record);
                saveLines(conn, documentId, record);
                setProformaStatus(conn, record.getSourceProformaId(), record.getSourceProformaNo(), "INVOICED");
                conn.commit();
                record.setId(documentId);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to save Sales Invoice: " + e.getMessage(), e);
        }
    }

    public void deleteInvoice(long documentId) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                SourceProforma source = loadSourceProforma(conn, documentId);
                try (PreparedStatement deleteLines = conn.prepareStatement(
                        "DELETE FROM generic_document_lines WHERE document_id = ?");
                     PreparedStatement deleteHeader = conn.prepareStatement(
                             "DELETE FROM generic_documents WHERE document_id = ? AND document_type = 'SALES_INVOICE'")) {
                    deleteLines.setLong(1, documentId);
                    deleteLines.executeUpdate();
                    deleteHeader.setLong(1, documentId);
                    deleteHeader.executeUpdate();
                }
                if (source != null) {
                    setProformaStatus(conn, source.sourceProformaId(), source.sourceProformaNo(), "OPEN");
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to delete Sales Invoice: " + e.getMessage(), e);
        }
    }

    public void toggleCancelRecall(long documentId) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String status = "OPEN";
                SourceProforma source = null;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT status, source_proforma_id, source_proforma_no FROM generic_documents WHERE document_id = ? AND document_type = 'SALES_INVOICE' LIMIT 1")) {
                    ps.setLong(1, documentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("Sales Invoice not found.");
                        }
                        status = defaultString(rs.getString("status"), "OPEN").toUpperCase();
                        source = new SourceProforma(rs.getLong("source_proforma_id"), rs.getString("source_proforma_no"));
                    }
                }
                String nextStatus = "CANCELLED".equals(status) ? "OPEN" : "CANCELLED";
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE generic_documents SET status = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP WHERE document_id = ? AND document_type = 'SALES_INVOICE'")) {
                    ps.setString(1, nextStatus);
                    ps.setString(2, "SYSTEM");
                    ps.setLong(3, documentId);
                    ps.executeUpdate();
                }
                if (source != null) {
                    setProformaStatus(conn, source.sourceProformaId(), source.sourceProformaNo(),
                            "CANCELLED".equals(nextStatus) ? "OPEN" : "INVOICED");
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to cancel/recall Sales Invoice: " + e.getMessage(), e);
        }
    }

    private interface StatementBinder {
        void bind(PreparedStatement ps) throws Exception;
    }

    private SalesInvoiceRecord loadDocument(Connection conn, String documentType, String whereClause, StatementBinder binder) throws Exception {
        String sql = """
                SELECT document_id, document_no, source_proforma_id, source_proforma_no, invoice_no,
                       document_date, party_code, party_name, location_code, salesman_code, salesman_name,
                       currency_code, currency_name, exchange_rate, price_code, apply_formula, consumables,
                       box_qty, total_kgs, fish_cost, discount_percent, discount_amount, misc_amount, ssc_amount,
                       rate_amount, rate2_amount, vat_amount, stamp_amount, doa_amount, freight_amount,
                       packing_charges, product_sales_amount, total_payables, awb_no, broker,
                       prepared_by, checked_by, approved_by_name, received_by
                FROM generic_documents
                WHERE document_type = ? AND """ + whereClause + """
                LIMIT 1
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documentType);
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                SalesInvoiceRecord record = new SalesInvoiceRecord();
                record.setId(rs.getLong("document_id"));
                record.setProformaNo(rs.getString("source_proforma_no"));
                record.setSourceProformaId(rs.getLong("source_proforma_id"));
                record.setSourceProformaNo(rs.getString("source_proforma_no"));
                record.setInvoiceNo(defaultString(rs.getString("invoice_no"), rs.getString("document_no")));
                record.setInvoiceDate(rs.getDate("document_date") == null ? LocalDate.now() : rs.getDate("document_date").toLocalDate());
                record.setCustomerCode(rs.getString("party_code"));
                record.setCustomerName(rs.getString("party_name"));
                record.setBranchCode(rs.getString("location_code"));
                record.setSalesmanCode(rs.getString("salesman_code"));
                record.setSalesmanName(rs.getString("salesman_name"));
                record.setCurrencyCode(defaultString(rs.getString("currency_code"), "USD"));
                record.setCurrencyName(defaultString(rs.getString("currency_name"), "AMERICAN DOLLAR"));
                record.setExchangeRate(decimal(rs.getBigDecimal("exchange_rate"), BigDecimal.ONE));
                record.setPricingCode(defaultString(rs.getString("price_code"), "B"));
                record.setApplyFormula(rs.getBoolean("apply_formula"));
                record.setConsumables(rs.getBoolean("consumables"));
                record.setBoxQty(decimal(rs.getBigDecimal("box_qty")));
                record.setTotalKgs(decimal(rs.getBigDecimal("total_kgs")));
                record.setFishCost(decimal(rs.getBigDecimal("fish_cost")));
                record.setDiscountPercent(decimal(rs.getBigDecimal("discount_percent")));
                record.setDiscountAmount(decimal(rs.getBigDecimal("discount_amount")));
                record.setMiscAmount(decimal(rs.getBigDecimal("misc_amount")));
                record.setSscAmount(decimal(rs.getBigDecimal("ssc_amount")));
                record.setRateAmount(decimal(rs.getBigDecimal("rate_amount")));
                record.setRate2Amount(decimal(rs.getBigDecimal("rate2_amount")));
                record.setVatAmount(decimal(rs.getBigDecimal("vat_amount")));
                record.setStampAmount(decimal(rs.getBigDecimal("stamp_amount")));
                record.setDoaAmount(decimal(rs.getBigDecimal("doa_amount")));
                record.setFreightAmount(decimal(rs.getBigDecimal("freight_amount")));
                record.setPackingCharges(decimal(rs.getBigDecimal("packing_charges")));
                record.setProductSalesAmount(decimal(rs.getBigDecimal("product_sales_amount")));
                record.setTotalPayables(decimal(rs.getBigDecimal("total_payables")));
                record.setAwbNo(rs.getString("awb_no"));
                record.setBroker(rs.getString("broker"));
                record.setPreparedBy(rs.getString("prepared_by"));
                record.setCheckedBy(rs.getString("checked_by"));
                record.setApprovedByName(rs.getString("approved_by_name"));
                record.setReceivedBy(rs.getString("received_by"));
                loadLines(conn, record);
                return record;
            }
        }
    }

    private void loadLines(Connection conn, SalesInvoiceRecord record) throws Exception {
        String sql = """
                SELECT l.document_line_id, l.line_no, l.trans_shipper_code, l.box_no, l.product_code,
                       COALESCE(p.description, l.remarks, '') AS description,
                       l.supplier_code, l.special_value, l.qty_out, l.unit_price, l.total_price
                FROM generic_document_lines l
                LEFT JOIN products p ON p.product_code = l.product_code
                WHERE l.document_id = ?
                ORDER BY l.line_no
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, record.getId());
            try (ResultSet rs = ps.executeQuery()) {
                record.getLines().clear();
                while (rs.next()) {
                    SalesInvoiceLine line = new SalesInvoiceLine();
                    line.setId(rs.getLong("document_line_id"));
                    line.setLineNo(rs.getInt("line_no"));
                    line.setTransShipperCode(rs.getString("trans_shipper_code"));
                    line.setBoxNo(rs.getString("box_no"));
                    line.setProductCode(rs.getString("product_code"));
                    line.setDescription(rs.getString("description"));
                    line.setSupplierCode(rs.getString("supplier_code"));
                    line.setSpecialValue(defaultString(rs.getString("special_value"), ""));
                    line.setSpecial(!line.getSpecialValue().isBlank());
                    line.setQuantity(decimal(rs.getBigDecimal("qty_out")).intValue());
                    line.setSellingPrice(decimal(rs.getBigDecimal("unit_price")));
                    line.setTotalPrice(decimal(rs.getBigDecimal("total_price")));
                    record.getLines().add(line);
                }
            }
        }
    }

    private List<BrowseRow> browseDocuments(String documentType, String orderKey, String keyword) {
        boolean availableOnly = "PROFORMA".equalsIgnoreCase(documentType);
        String orderBy = switch (orderKey == null ? "" : orderKey.toUpperCase()) {
            case "CUSTOMER" -> "party_code, document_date, document_no";
            case "DATE" -> "document_date, document_no";
            default -> "document_no, document_date";
        };
        String sql = """
                SELECT document_id, document_no, party_code, party_name, document_date,
                       total_payables, status
                FROM generic_documents
                WHERE document_type = ?
                  AND (? = FALSE OR UPPER(COALESCE(status, 'OPEN')) = 'OPEN')
                  AND (? = '' OR document_no LIKE ? OR party_code LIKE ? OR party_name LIKE ?)
                ORDER BY
                """ + orderBy;
        List<BrowseRow> rows = new ArrayList<>();
        String search = keyword == null ? "" : keyword.trim();
        String like = "%" + search + "%";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documentType);
            ps.setBoolean(2, availableOnly);
            ps.setString(3, search);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setString(6, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new BrowseRow(
                            rs.getLong("document_id"),
                            rs.getString("document_no"),
                            rs.getString("party_code"),
                            rs.getString("party_name"),
                            rs.getDate("document_date") == null ? null : rs.getDate("document_date").toLocalDate(),
                            decimal(rs.getBigDecimal("total_payables")),
                            defaultString(rs.getString("status"), "OPEN")));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to browse " + documentType + ": " + e.getMessage(), e);
        }
        return rows;
    }

    private void applyPricing(Connection conn, SalesInvoiceRecord record, SalesInvoiceLine line) throws Exception {
        BigDecimal price = resolveBasePrice(conn, line.getProductCode(), record.getPricingCode());
        boolean special = false;
        String specialValue = "";

        BigDecimal override = findNetPrice(conn, line.getProductCode(), record.getCustomerCode(), record.getCurrencyCode());
        if (override != null) {
            price = override;
        }
        override = findContractPrice(conn, "TRANSHIPPER", line.getProductCode(), effectivePartyCode(record, line));
        if (override != null) {
            price = override;
        }
        override = findSimplePrice(conn, "invoice_special_prices", line.getProductCode());
        if (override != null) {
            price = override;
            special = true;
            specialValue = "S";
        }
        override = findDateRangeSpecial(conn, line.getProductCode(), record.getInvoiceDate());
        if (override != null) {
            price = override;
            special = true;
            specialValue = "SW";
        }
        override = findHistorySpecial(conn, line.getProductCode(), record.getInvoiceDate());
        if (override != null) {
            price = override;
            special = true;
            specialValue = "HD";
        }
        override = findContractPrice(conn, "TRANSHIPPER_NEW", line.getProductCode(), effectivePartyCode(record, line));
        if (override != null) {
            price = override;
            special = false;
            specialValue = "";
        }
        if (!record.isApplyFormula()) {
            special = false;
            specialValue = "";
        }
        line.setSellingPrice(money(price));
        line.setSpecial(special);
        line.setSpecialValue(specialValue);
    }

    private BigDecimal resolveBasePrice(Connection conn, String productCode, String pricingCode) throws Exception {
        String sql = """
                SELECT price_a, price_b, price_c, price_d, price_e, price_f, price_g, special_price, local_sales_price
                FROM products WHERE product_code = ? LIMIT 1
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return BigDecimal.ZERO;
                }
                return switch (defaultString(pricingCode, "B").toUpperCase()) {
                    case "A" -> decimal(rs.getBigDecimal("price_a"));
                    case "C" -> decimal(rs.getBigDecimal("price_c"));
                    case "D" -> decimal(rs.getBigDecimal("price_d"));
                    case "E" -> decimal(rs.getBigDecimal("price_e"));
                    case "F" -> decimal(rs.getBigDecimal("price_f"));
                    case "G" -> decimal(rs.getBigDecimal("price_g"));
                    case "S" -> decimal(rs.getBigDecimal("special_price"));
                    case "L" -> decimal(rs.getBigDecimal("local_sales_price"));
                    default -> decimal(rs.getBigDecimal("price_b"));
                };
            }
        }
    }

    private SourceProforma loadSourceProforma(Connection conn, long documentId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT source_proforma_id, source_proforma_no FROM generic_documents WHERE document_id = ? AND document_type = 'SALES_INVOICE' LIMIT 1")) {
            ps.setLong(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new SourceProforma(rs.getLong("source_proforma_id"), rs.getString("source_proforma_no"));
            }
        }
    }

    private void setProformaStatus(Connection conn, long sourceProformaId, String sourceProformaNo, String status) throws Exception {
        if (sourceProformaId > 0) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE generic_documents SET status = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP WHERE document_id = ? AND document_type = 'PROFORMA'")) {
                ps.setString(1, status);
                ps.setString(2, "SYSTEM");
                ps.setLong(3, sourceProformaId);
                if (ps.executeUpdate() > 0) {
                    return;
                }
            }
        }
        if (sourceProformaNo == null || sourceProformaNo.isBlank()) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE generic_documents SET status = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP WHERE document_no = ? AND document_type = 'PROFORMA'")) {
            ps.setString(1, status);
            ps.setString(2, "SYSTEM");
            ps.setString(3, sourceProformaNo.trim());
            ps.executeUpdate();
        }
    }

    private BigDecimal findNetPrice(Connection conn, String productCode, String customerCode, String currencyCode) throws Exception {
        String sql = """
                SELECT net_price FROM net_prices
                WHERE product_code = ? AND customer_code = ? AND currency_code = ?
                LIMIT 1
                """;
        return findScalarPrice(conn, sql, productCode, customerCode, currencyCode);
    }

    private BigDecimal findContractPrice(Connection conn, String contractType, String productCode, String partyCode) throws Exception {
        String sql = """
                SELECT price FROM contract_prices
                WHERE contract_type = ? AND product_code = ? AND party_code = ? AND is_active = TRUE
                LIMIT 1
                """;
        return findScalarPrice(conn, sql, contractType, productCode, partyCode);
    }

    private BigDecimal findSimplePrice(Connection conn, String tableName, String productCode) throws Exception {
        String sql = "SELECT special_price FROM " + tableName + " WHERE product_code = ? AND is_active = TRUE LIMIT 1";
        return findScalarPrice(conn, sql, productCode);
    }

    private BigDecimal findDateRangeSpecial(Connection conn, String productCode, LocalDate invoiceDate) throws Exception {
        String sql = """
                SELECT special_price FROM special_of_week
                WHERE product_code = ? AND is_active = TRUE
                  AND (? IS NULL OR (start_date <= ? AND end_date >= ?))
                ORDER BY start_date DESC
                LIMIT 1
                """;
        Date sqlDate = invoiceDate == null ? null : Date.valueOf(invoiceDate);
        return findScalarPrice(conn, sql, productCode, sqlDate, sqlDate, sqlDate);
    }

    private BigDecimal findHistorySpecial(Connection conn, String productCode, LocalDate invoiceDate) throws Exception {
        String sql = """
                SELECT special_price FROM dated_special_prices
                WHERE product_code = ? AND is_active = TRUE
                  AND (? IS NULL OR pricing_date <= ?)
                ORDER BY pricing_date DESC
                LIMIT 1
                """;
        Date sqlDate = invoiceDate == null ? null : Date.valueOf(invoiceDate);
        return findScalarPrice(conn, sql, productCode, sqlDate, sqlDate);
    }

    private BigDecimal findScalarPrice(Connection conn, String sql, Object... params) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? decimal(rs.getBigDecimal(1)) : null;
            }
        }
    }

    private void recomputeTotals(SalesInvoiceRecord record) {
        BigDecimal totalLines = BigDecimal.ZERO;
        BigDecimal nonSpecial = BigDecimal.ZERO;
        for (SalesInvoiceLine line : record.getLines()) {
            line.recompute();
            totalLines = totalLines.add(decimal(line.getTotalPrice()));
            if (!line.isSpecial()) {
                nonSpecial = nonSpecial.add(decimal(line.getTotalPrice()));
            }
        }
        BigDecimal discountValue = money(nonSpecial.multiply(decimal(record.getDiscountPercent())).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        BigDecimal freight = money(decimal(record.getSscAmount()).add(decimal(record.getRate2Amount()))
                .multiply(decimal(record.getTotalKgs()))
                .add(decimal(record.getVatAmount()))
                .add(decimal(record.getStampAmount())));
        BigDecimal productSales = money(totalLines.subtract(discountValue));
        BigDecimal totalPayables = money(productSales.add(decimal(record.getPackingCharges()))
                .subtract(decimal(record.getDoaAmount()))
                .add(freight)
                .add(decimal(record.getMiscAmount())));

        record.setLineAmount(money(totalLines));
        record.setDiscountAmount(discountValue);
        record.setFreightAmount(freight);
        record.setProductSalesAmount(productSales);
        record.setTotalPayables(totalPayables);
    }

    private long upsertHeader(Connection conn, SalesInvoiceRecord record) throws Exception {
        boolean insert = record.getId() <= 0;
        String sql = insert
                ? """
                INSERT INTO generic_documents (
                    document_type, document_no, document_date, party_code, party_name, location_code,
                    source_proforma_id, source_proforma_no, invoice_no, salesman_code, salesman_name,
                    currency_code, currency_name, exchange_rate, price_code, apply_formula, consumables,
                    box_qty, total_kgs, fish_cost, discount_percent, discount_amount, misc_amount, ssc_amount,
                    rate_amount, rate2_amount, vat_amount, stamp_amount, doa_amount, freight_amount,
                    packing_charges, product_sales_amount, total_payables, awb_no, broker,
                    prepared_by, checked_by, approved_by_name, received_by, amount, status, created_by, updated_by
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """
                : """
                UPDATE generic_documents SET
                    document_date=?, party_code=?, party_name=?, location_code=?, source_proforma_id=?, source_proforma_no=?,
                    invoice_no=?, salesman_code=?, salesman_name=?, currency_code=?, currency_name=?, exchange_rate=?,
                    price_code=?, apply_formula=?, consumables=?, box_qty=?, total_kgs=?, fish_cost=?, discount_percent=?,
                    discount_amount=?, misc_amount=?, ssc_amount=?, rate_amount=?, rate2_amount=?, vat_amount=?, stamp_amount=?,
                    doa_amount=?, freight_amount=?, packing_charges=?, product_sales_amount=?, total_payables=?, awb_no=?, broker=?,
                    prepared_by=?, checked_by=?, approved_by_name=?, received_by=?, amount=?, updated_by=?, updated_at=CURRENT_TIMESTAMP
                WHERE document_id=?
                """;
        try (PreparedStatement ps = insert ? conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : conn.prepareStatement(sql)) {
            int i = 1;
            if (insert) {
                ps.setString(i++, "SALES_INVOICE");
                ps.setString(i++, defaultString(record.getInvoiceNo(), nextInvoiceNo(conn)));
                i = bindHeader(ps, i, record);
                ps.setString(i++, "OPEN");
                ps.setString(i++, "SYSTEM");
                ps.setString(i++, "SYSTEM");
            } else {
                i = bindHeader(ps, i, record);
                ps.setString(i++, "SYSTEM");
                ps.setLong(i, record.getId());
            }
            ps.executeUpdate();
            if (!insert) {
                return record.getId();
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                long id = keys.getLong(1);
                record.setId(id);
                return id;
            }
        }
    }

    private int bindHeader(PreparedStatement ps, int start, SalesInvoiceRecord record) throws Exception {
        int i = start;
        ps.setDate(i++, record.getInvoiceDate() == null ? Date.valueOf(LocalDate.now()) : Date.valueOf(record.getInvoiceDate()));
        ps.setString(i++, record.getCustomerCode());
        ps.setString(i++, record.getCustomerName());
        ps.setString(i++, record.getBranchCode());
        ps.setLong(i++, record.getSourceProformaId());
        ps.setString(i++, record.getSourceProformaNo());
        ps.setString(i++, defaultString(record.getInvoiceNo(), ""));
        ps.setString(i++, record.getSalesmanCode());
        ps.setString(i++, record.getSalesmanName());
        ps.setString(i++, defaultString(record.getCurrencyCode(), "USD"));
        ps.setString(i++, defaultString(record.getCurrencyName(), "AMERICAN DOLLAR"));
        ps.setBigDecimal(i++, money(decimal(record.getExchangeRate(), BigDecimal.ONE)));
        ps.setString(i++, defaultString(record.getPricingCode(), "B"));
        ps.setBoolean(i++, record.isApplyFormula());
        ps.setBoolean(i++, record.isConsumables());
        ps.setBigDecimal(i++, money(record.getBoxQty()));
        ps.setBigDecimal(i++, money(record.getTotalKgs()));
        ps.setBigDecimal(i++, money(record.getFishCost()));
        ps.setBigDecimal(i++, money(record.getDiscountPercent()));
        ps.setBigDecimal(i++, money(record.getDiscountAmount()));
        ps.setBigDecimal(i++, money(record.getMiscAmount()));
        ps.setBigDecimal(i++, money(record.getSscAmount()));
        ps.setBigDecimal(i++, money(record.getRateAmount()));
        ps.setBigDecimal(i++, money(record.getRate2Amount()));
        ps.setBigDecimal(i++, money(record.getVatAmount()));
        ps.setBigDecimal(i++, money(record.getStampAmount()));
        ps.setBigDecimal(i++, money(record.getDoaAmount()));
        ps.setBigDecimal(i++, money(record.getFreightAmount()));
        ps.setBigDecimal(i++, money(record.getPackingCharges()));
        ps.setBigDecimal(i++, money(record.getProductSalesAmount()));
        ps.setBigDecimal(i++, money(record.getTotalPayables()));
        ps.setString(i++, record.getAwbNo());
        ps.setString(i++, record.getBroker());
        ps.setString(i++, record.getPreparedBy());
        ps.setString(i++, record.getCheckedBy());
        ps.setString(i++, record.getApprovedByName());
        ps.setString(i++, record.getReceivedBy());
        ps.setBigDecimal(i++, money(record.getLineAmount()));
        return i;
    }

    private void saveLines(Connection conn, long documentId, SalesInvoiceRecord record) throws Exception {
        try (PreparedStatement delete = conn.prepareStatement("DELETE FROM generic_document_lines WHERE document_id = ?")) {
            delete.setLong(1, documentId);
            delete.executeUpdate();
        }
        String sql = """
                INSERT INTO generic_document_lines (
                    document_id, line_no, product_code, party_code, supplier_code, box_no,
                    special_value, qty_out, unit_price, total_price, remarks, trans_shipper_code
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int lineNo = 1;
            for (SalesInvoiceLine line : record.getLines()) {
                line.setLineNo(lineNo);
                ps.setLong(1, documentId);
                ps.setInt(2, lineNo++);
                ps.setString(3, line.getProductCode());
                ps.setString(4, record.getCustomerCode());
                ps.setString(5, line.getSupplierCode());
                ps.setString(6, line.getBoxNo());
                ps.setString(7, line.isSpecial() ? defaultString(line.getSpecialValue(), "1") : "");
                ps.setBigDecimal(8, BigDecimal.valueOf(Math.max(0, line.getQuantity())));
                ps.setBigDecimal(9, money(line.getSellingPrice()));
                ps.setBigDecimal(10, money(line.getTotalPrice()));
                ps.setString(11, line.getDescription());
                ps.setString(12, line.getTransShipperCode());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private String effectivePartyCode(SalesInvoiceRecord record, SalesInvoiceLine line) {
        return line.getTransShipperCode() != null && !line.getTransShipperCode().isBlank()
                ? line.getTransShipperCode().trim()
                : defaultString(record.getCustomerCode(), "");
    }

    private String nextInvoiceNo(Connection conn) throws Exception {
        String sql = "SELECT invoice_no, document_no FROM generic_documents WHERE document_type = 'SALES_INVOICE'";
        long max = 100000;
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                max = Math.max(max, parseInvoiceNumber(rs.getString("invoice_no")));
                max = Math.max(max, parseInvoiceNumber(rs.getString("document_no")));
            }
        }
        return Long.toString(max + 1);
    }

    private long parseInvoiceNumber(String value) {
        if (value == null) {
            return 0;
        }
        String digits = value.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Long.parseLong(digits);
    }

    private BigDecimal decimal(BigDecimal value) {
        return decimal(value, BigDecimal.ZERO);
    }

    private BigDecimal decimal(BigDecimal value, BigDecimal fallback) {
        return value == null ? fallback : value;
    }

    private BigDecimal money(BigDecimal value) {
        return decimal(value).setScale(2, RoundingMode.HALF_UP);
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private record SourceProforma(long sourceProformaId, String sourceProformaNo) {
    }
}
