package com.rvsfishworld.dao;

import com.rvsfishworld.db.Database;
import com.rvsfishworld.model.MortalityLine;
import com.rvsfishworld.model.MortalityRecord;
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

public class MortalityDAO {
    public record BrowseRow(long id, String referenceNo, LocalDate recordDate, String area, BigDecimal totalAmount, String status) {
    }

    public List<BrowseRow> browse(String orderKey) {
        String orderBy = switch ((orderKey == null ? "" : orderKey).toUpperCase()) {
            case "DATE" -> "document_date, document_no";
            case "AREA" -> "location_code, document_date, document_no";
            default -> "document_no, document_date";
        };
        String sql = """
                SELECT document_id, document_no, document_date, location_code, amount, status
                FROM generic_documents
                WHERE document_type = 'MORTALITY'
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
                        rs.getDate("document_date") == null ? null : rs.getDate("document_date").toLocalDate(),
                        defaultString(rs.getString("location_code")),
                        money(rs.getBigDecimal("amount")),
                        defaultString(rs.getString("status"), "OPEN")));
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to browse Mortality: " + e.getMessage(), e);
        }
        return rows;
    }

    public MortalityRecord load(long documentId) {
        String sql = """
                SELECT document_id, document_no, document_date, location_code, amount
                FROM generic_documents
                WHERE document_type = 'MORTALITY' AND document_id = ?
                LIMIT 1
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                MortalityRecord record = new MortalityRecord();
                record.setId(rs.getLong("document_id"));
                record.setReferenceNo(rs.getString("document_no"));
                record.setRecordDate(rs.getDate("document_date") == null ? LocalDate.now() : rs.getDate("document_date").toLocalDate());
                record.setArea(defaultString(rs.getString("location_code")));
                record.setTotalAmount(money(rs.getBigDecimal("amount")));
                loadLines(conn, record);
                recompute(record);
                return record;
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load Mortality: " + e.getMessage(), e);
        }
    }

    public void save(MortalityRecord record) {
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
            throw new RuntimeException("Unable to save Mortality: " + e.getMessage(), e);
        }
    }

    public void delete(long documentId) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement deleteLines = conn.prepareStatement("DELETE FROM generic_document_lines WHERE document_id = ?");
                 PreparedStatement deleteHeader = conn.prepareStatement("DELETE FROM generic_documents WHERE document_id = ? AND document_type = 'MORTALITY'")) {
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
            throw new RuntimeException("Unable to delete Mortality: " + e.getMessage(), e);
        }
    }

    private void loadLines(Connection conn, MortalityRecord record) throws Exception {
        String sql = """
                SELECT document_line_id, product_code, COALESCE(remarks, '') AS description, qty_out, unit_price, total_price
                FROM generic_document_lines
                WHERE document_id = ?
                ORDER BY line_no
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, record.getId());
            try (ResultSet rs = ps.executeQuery()) {
                record.getLines().clear();
                while (rs.next()) {
                    MortalityLine line = new MortalityLine();
                    line.setId(rs.getLong("document_line_id"));
                    line.setProductCode(defaultString(rs.getString("product_code")));
                    line.setDescription(defaultString(rs.getString("description")));
                    line.setArea(record.getArea());
                    line.setQuantity(decimal(rs.getBigDecimal("qty_out")).intValue());
                    line.setAverageCost(money(rs.getBigDecimal("unit_price")));
                    line.setTotalCost(money(rs.getBigDecimal("total_price")));
                    record.getLines().add(line);
                }
            }
        }
    }

    private long upsertHeader(Connection conn, MortalityRecord record) throws Exception {
        boolean insert = record.getId() <= 0;
        String sql = insert
                ? """
                INSERT INTO generic_documents (
                    document_type, document_no, document_date, location_code, amount, total_payables, status, created_by, updated_by
                ) VALUES (?,?,?,?,?,?,?,?,?)
                """
                : """
                UPDATE generic_documents SET document_date=?, location_code=?, amount=?, total_payables=?, updated_by=?, updated_at=CURRENT_TIMESTAMP
                WHERE document_id=? AND document_type='MORTALITY'
                """;
        try (PreparedStatement ps = insert ? conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : conn.prepareStatement(sql)) {
            int i = 1;
            if (insert) {
                ps.setString(i++, "MORTALITY");
                ps.setString(i++, defaultString(record.getReferenceNo(), nextReferenceNo(conn)));
                ps.setDate(i++, record.getRecordDate() == null ? Date.valueOf(LocalDate.now()) : Date.valueOf(record.getRecordDate()));
                ps.setString(i++, defaultString(record.getArea()));
                ps.setBigDecimal(i++, money(record.getTotalAmount()));
                ps.setBigDecimal(i++, money(record.getTotalAmount()));
                ps.setString(i++, "OPEN");
                ps.setString(i++, "SYSTEM");
                ps.setString(i++, "SYSTEM");
            } else {
                ps.setDate(i++, record.getRecordDate() == null ? Date.valueOf(LocalDate.now()) : Date.valueOf(record.getRecordDate()));
                ps.setString(i++, defaultString(record.getArea()));
                ps.setBigDecimal(i++, money(record.getTotalAmount()));
                ps.setBigDecimal(i++, money(record.getTotalAmount()));
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

    private void saveLines(Connection conn, long documentId, MortalityRecord record) throws Exception {
        try (PreparedStatement delete = conn.prepareStatement("DELETE FROM generic_document_lines WHERE document_id = ?")) {
            delete.setLong(1, documentId);
            delete.executeUpdate();
        }
        String sql = """
                INSERT INTO generic_document_lines (
                    document_id, line_no, product_code, qty_out, unit_price, total_price, remarks, party_code
                ) VALUES (?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int lineNo = 1;
            for (MortalityLine line : record.getLines()) {
                line.recompute();
                ps.setLong(1, documentId);
                ps.setInt(2, lineNo++);
                ps.setString(3, defaultString(line.getProductCode()));
                ps.setBigDecimal(4, BigDecimal.valueOf(Math.max(0, line.getQuantity())));
                ps.setBigDecimal(5, money(line.getAverageCost()));
                ps.setBigDecimal(6, money(line.getTotalCost()));
                ps.setString(7, defaultString(line.getDescription()));
                ps.setString(8, defaultString(record.getArea()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void recompute(MortalityRecord record) {
        BigDecimal total = BigDecimal.ZERO;
        for (MortalityLine line : record.getLines()) {
            line.recompute();
            total = total.add(money(line.getTotalCost()));
        }
        record.setTotalAmount(money(total));
    }

    private String nextReferenceNo(Connection conn) throws Exception {
        long max = 8700;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT document_no FROM generic_documents WHERE document_type = 'MORTALITY'");
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
        return "%08d".formatted(max + 1);
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
