package com.rvsfishworld.dao;

import com.rvsfishworld.db.Database;
import com.rvsfishworld.model.GenericDocumentRecord;
import com.rvsfishworld.model.ProformaLine;
import com.rvsfishworld.model.ProformaRecord;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GenericDocumentDAO {
    public record BrowseRow(
            long documentId,
            String documentNo,
            String partyCode,
            String partyName,
            LocalDate documentDate,
            BigDecimal totalPayables,
            String status) {
    }

    public List<GenericDocumentRecord> browse(String documentType) {
        return Collections.emptyList();
    }

    public List<BrowseRow> browseProformas(String orderKey) {
        String orderBy = switch (defaultString(orderKey, "PROFORMA").toUpperCase()) {
            case "CUSTOMER" -> "party_code, document_date, document_no";
            case "DATE" -> "document_date, document_no";
            default -> "document_no, document_date";
        };
        String sql = """
                SELECT document_id, document_no, party_code, party_name, document_date, total_payables, status
                FROM generic_documents
                WHERE document_type = 'PROFORMA'
                ORDER BY
                """ + orderBy;
        List<BrowseRow> rows = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new BrowseRow(
                        rs.getLong("document_id"),
                        rs.getString("document_no"),
                        rs.getString("party_code"),
                        rs.getString("party_name"),
                        rs.getDate("document_date") == null ? null : rs.getDate("document_date").toLocalDate(),
                        money(rs.getBigDecimal("total_payables")),
                        defaultString(rs.getString("status"), "OPEN")));
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to browse Proformas: " + e.getMessage(), e);
        }
        return rows;
    }

    public ProformaRecord loadProforma(long documentId) {
        String sql = """
                SELECT document_id, document_no, document_date, party_code, party_name, location_code,
                       salesman_code, salesman_name, discount_percent, packing_charges,
                       total_payables, amount, prepared_by, approved_by_name
                FROM generic_documents
                WHERE document_type = 'PROFORMA' AND document_id = ?
                LIMIT 1
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ProformaRecord record = new ProformaRecord();
                record.setId(rs.getLong("document_id"));
                record.setProformaNo(rs.getString("document_no"));
                record.setInvoiceDate(rs.getDate("document_date") == null ? LocalDate.now() : rs.getDate("document_date").toLocalDate());
                record.setCustomerCode(rs.getString("party_code"));
                record.setCustomerName(rs.getString("party_name"));
                record.setBranchCode(defaultString(rs.getString("location_code")));
                record.setSalesmanCode(defaultString(rs.getString("salesman_code")));
                record.setSalesmanName(defaultString(rs.getString("salesman_name")));
                record.setAdjustmentPercent(money(rs.getBigDecimal("discount_percent")));
                record.setPackingCharges(money(rs.getBigDecimal("packing_charges")));
                record.setTotalPayables(money(rs.getBigDecimal("total_payables")));
                record.setTotalAmount(money(rs.getBigDecimal("amount")));
                record.setPreparedBy(defaultString(rs.getString("prepared_by")));
                record.setApprovedBy(defaultString(rs.getString("approved_by_name")));
                loadProformaLines(conn, record);
                recompute(record);
                return record;
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load Proforma: " + e.getMessage(), e);
        }
    }

    public void save(GenericDocumentRecord record) {
        // Generic document editing is still not on the active runtime path.
    }

    public void saveProforma(ProformaRecord record) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                recompute(record);
                long id = upsertHeader(conn, record);
                saveLines(conn, id, record);
                conn.commit();
                record.setId(id);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to save Proforma: " + e.getMessage(), e);
        }
    }

    public void deleteProforma(long documentId) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement deleteLines = conn.prepareStatement("DELETE FROM generic_document_lines WHERE document_id = ?");
                 PreparedStatement deleteHeader = conn.prepareStatement("DELETE FROM generic_documents WHERE document_id = ? AND document_type = 'PROFORMA'")) {
                deleteLines.setLong(1, documentId);
                deleteLines.executeUpdate();
                deleteHeader.setLong(1, documentId);
                deleteHeader.executeUpdate();
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to delete Proforma: " + e.getMessage(), e);
        }
    }

    public void toggleProformaCancelRecall(long documentId) {
        String sql = """
                UPDATE generic_documents
                SET status = CASE
                        WHEN UPPER(COALESCE(status, 'OPEN')) = 'CANCELLED' THEN 'OPEN'
                        ELSE 'CANCELLED'
                    END,
                    updated_by = 'SYSTEM',
                    updated_at = CURRENT_TIMESTAMP
                WHERE document_id = ? AND document_type = 'PROFORMA'
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, documentId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Unable to cancel/recall Proforma: " + e.getMessage(), e);
        }
    }

    private void loadProformaLines(Connection conn, ProformaRecord record) throws Exception {
        String sql = """
                SELECT document_line_id, line_no, trans_shipper_code, box_no, product_code,
                       COALESCE(remarks, '') AS description, supplier_code, qty_out, unit_price, total_price
                FROM generic_document_lines
                WHERE document_id = ?
                ORDER BY line_no
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, record.getId());
            try (ResultSet rs = ps.executeQuery()) {
                record.getLines().clear();
                while (rs.next()) {
                    ProformaLine line = new ProformaLine();
                    line.setId(rs.getLong("document_line_id"));
                    line.setLineNo(rs.getInt("line_no"));
                    line.setTransShipperCode(defaultString(rs.getString("trans_shipper_code")));
                    line.setBoxNo(defaultString(rs.getString("box_no")));
                    line.setProductCode(defaultString(rs.getString("product_code")));
                    line.setDescription(defaultString(rs.getString("description")));
                    line.setSupplierCode(defaultString(rs.getString("supplier_code")));
                    line.setQuantity(decimal(rs.getBigDecimal("qty_out")).intValue());
                    line.setPrice(money(rs.getBigDecimal("unit_price")));
                    line.setTotalPrice(money(rs.getBigDecimal("total_price")));
                    record.getLines().add(line);
                }
            }
        }
    }

    private long upsertHeader(Connection conn, ProformaRecord record) throws Exception {
        boolean insert = record.getId() <= 0;
        String sql = insert
                ? """
                INSERT INTO generic_documents (
                    document_type, document_no, document_date, party_code, party_name, location_code,
                    salesman_code, salesman_name, discount_percent, packing_charges,
                    total_payables, amount, prepared_by, approved_by_name, status, created_by, updated_by
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """
                : """
                UPDATE generic_documents SET
                    document_date=?, party_code=?, party_name=?, location_code=?, salesman_code=?, salesman_name=?,
                    discount_percent=?, packing_charges=?, total_payables=?, amount=?, prepared_by=?, approved_by_name=?,
                    updated_by=?, updated_at=CURRENT_TIMESTAMP
                WHERE document_id=? AND document_type='PROFORMA'
                """;
        try (PreparedStatement ps = insert ? conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : conn.prepareStatement(sql)) {
            int i = 1;
            if (insert) {
                ps.setString(i++, "PROFORMA");
                ps.setString(i++, defaultString(record.getProformaNo(), nextProformaNo(conn)));
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

    private int bindHeader(PreparedStatement ps, int start, ProformaRecord record) throws Exception {
        int i = start;
        ps.setDate(i++, record.getInvoiceDate() == null ? Date.valueOf(LocalDate.now()) : Date.valueOf(record.getInvoiceDate()));
        ps.setString(i++, defaultString(record.getCustomerCode()));
        ps.setString(i++, defaultString(record.getCustomerName()));
        ps.setString(i++, defaultString(record.getBranchCode()));
        ps.setString(i++, defaultString(record.getSalesmanCode()));
        ps.setString(i++, defaultString(record.getSalesmanName()));
        ps.setBigDecimal(i++, money(record.getAdjustmentPercent()));
        ps.setBigDecimal(i++, money(record.getPackingCharges()));
        ps.setBigDecimal(i++, money(record.getTotalPayables()));
        ps.setBigDecimal(i++, money(record.getTotalAmount()));
        ps.setString(i++, defaultString(record.getPreparedBy()));
        ps.setString(i++, defaultString(record.getApprovedBy()));
        return i;
    }

    private void saveLines(Connection conn, long documentId, ProformaRecord record) throws Exception {
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
            for (ProformaLine line : record.getLines()) {
                line.setLineNo(lineNo);
                line.recompute();
                ps.setLong(1, documentId);
                ps.setInt(2, lineNo++);
                ps.setString(3, line.getProductCode());
                ps.setString(4, defaultString(record.getCustomerCode()));
                ps.setString(5, defaultString(line.getSupplierCode()));
                ps.setString(6, defaultString(line.getBoxNo()));
                ps.setString(7, "");
                ps.setBigDecimal(8, BigDecimal.valueOf(Math.max(0, line.getQuantity())));
                ps.setBigDecimal(9, money(line.getPrice()));
                ps.setBigDecimal(10, money(line.getTotalPrice()));
                ps.setString(11, defaultString(line.getDescription()));
                ps.setString(12, defaultString(line.getTransShipperCode()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void recompute(ProformaRecord record) {
        BigDecimal total = BigDecimal.ZERO;
        for (ProformaLine line : record.getLines()) {
            line.recompute();
            total = total.add(money(line.getTotalPrice()));
        }
        BigDecimal discount = money(total.multiply(decimal(record.getAdjustmentPercent()))
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        BigDecimal payables = money(total.subtract(discount).add(decimal(record.getPackingCharges())));
        record.setTotalAmount(money(total));
        record.setTotalPayables(payables);
    }

    private String nextProformaNo(Connection conn) throws Exception {
        long max = 19100;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT document_no FROM generic_documents WHERE document_type = 'PROFORMA'");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String value = rs.getString(1);
                if (value != null) {
                    String digits = value.replaceAll("[^0-9]", "");
                    if (!digits.isEmpty()) {
                        max = Math.max(max, Long.parseLong(digits));
                    }
                }
            }
        }
        return "P-%07d".formatted(max + 1);
    }

    private BigDecimal decimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal money(BigDecimal value) {
        return decimal(value).setScale(2, RoundingMode.HALF_UP);
    }

    private String defaultString(String value) {
        return defaultString(value, "");
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
