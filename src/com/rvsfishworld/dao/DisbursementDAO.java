package com.rvsfishworld.dao;

import com.rvsfishworld.db.Database;
import com.rvsfishworld.model.DisbursementHeader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DisbursementDAO {
    public List<DisbursementHeader> browse() {
        String sql = """
                SELECT d.disbursement_id, d.ref_no, d.payment_date,
                       s.supplier_code, s.supplier_name, d.total_amount
                FROM disbursements d
                JOIN suppliers s ON s.supplier_id = d.supplier_id
                ORDER BY d.payment_date DESC, d.ref_no DESC
                """;
        List<DisbursementHeader> rows = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DisbursementHeader header = new DisbursementHeader();
                header.setId(rs.getLong("disbursement_id"));
                header.setCvNo(rs.getString("ref_no"));
                header.setCvDate(rs.getDate("payment_date").toLocalDate());
                header.setSupplierCode(rs.getString("supplier_code"));
                header.setSupplierName(rs.getString("supplier_name"));
                header.setAmount(rs.getBigDecimal("total_amount"));
                rows.add(header);
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load disbursements: " + e.getMessage(), e);
        }
    }

    public void save(DisbursementHeader header, long receivingId, String rrNo, BigDecimal rrAmount, String remarks) {
        save(header, receivingId, rrNo, rrAmount, rrAmount, remarks);
    }

    public void save(DisbursementHeader header, long receivingId, String rrNo, BigDecimal fishPurchaseAmount, BigDecimal totalAmount, String remarks) {
        String supplierSql = "SELECT supplier_id FROM suppliers WHERE supplier_code = ? LIMIT 1";
        String insertHeaderSql = """
                INSERT INTO disbursements (
                    ref_no, supplier_id, payment_date, description,
                    fish_purchase_amount, total_amount, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())
                """;
        String insertDetailSql = """
                INSERT INTO disbursement_details (
                    disbursement_id, receiving_id, rr_no, rr_amount, rr_adj, rr_total, remarks, created_at, updated_at
                ) VALUES (?, ?, ?, ?, 0, ?, ?, NOW(), NOW())
                """;
        String updateReceivingSql = """
                UPDATE receiving_headers
                SET amount_paid = COALESCE(amount_paid, 0) + ?,
                    cash_on_hand = GREATEST(COALESCE(total_amount, 0) - (COALESCE(amount_paid, 0) + ?), 0),
                    updated_at = NOW()
                WHERE receiving_id = ?
                """;

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long supplierId;
                try (PreparedStatement ps = conn.prepareStatement(supplierSql)) {
                    ps.setString(1, header.getSupplierCode());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("Supplier not found for code " + header.getSupplierCode());
                        }
                        supplierId = rs.getLong(1);
                    }
                }

                long disbursementId;
                try (PreparedStatement ps = conn.prepareStatement(insertHeaderSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, header.getCvNo());
                    ps.setLong(2, supplierId);
                    ps.setDate(3, java.sql.Date.valueOf(header.getCvDate()));
                    ps.setString(4, remarks);
                    ps.setBigDecimal(5, fishPurchaseAmount);
                    ps.setBigDecimal(6, totalAmount);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("Failed to create C.V. header.");
                        }
                        disbursementId = rs.getLong(1);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(insertDetailSql)) {
                    ps.setLong(1, disbursementId);
                    ps.setLong(2, receivingId);
                    ps.setString(3, rrNo);
                    ps.setBigDecimal(4, totalAmount);
                    ps.setBigDecimal(5, totalAmount);
                    ps.setString(6, remarks);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(updateReceivingSql)) {
                    ps.setBigDecimal(1, totalAmount);
                    ps.setBigDecimal(2, totalAmount);
                    ps.setLong(3, receivingId);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save disbursement: " + e.getMessage(), e);
        }
    }
}
