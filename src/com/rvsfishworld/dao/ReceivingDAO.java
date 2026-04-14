package com.rvsfishworld.dao;

import com.rvsfishworld.db.Database;
import com.rvsfishworld.model.ReceivingHeader;
import com.rvsfishworld.model.ReceivingLine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReceivingDAO {

    public String getNextRrNo() {
        String sql = """
                SELECT LPAD(COALESCE(MAX(CAST(rr_no AS UNSIGNED)), 0) + 1, 10, '0') AS next_rr_no
                FROM receiving_headers
                WHERE rr_no REGEXP '^[0-9]+$'
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("next_rr_no");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get next R.R. No.: " + e.getMessage(), e);
        }
        return "0000000001";
    }

    public ReceivingHeader findHeaderById(long receivingId) {
        String headerSql = """
                SELECT rh.receiving_id, rh.rr_no, rh.date_received, rh.direct_purchase,
                       rh.is_cancelled, rh.supplier_id, s.supplier_code, s.supplier_name,
                       COALESCE(rh.ltp_no, '') AS ltp_no,
                       rh.branch_id, b.branch_code, b.branch_name,
                       rh.currency_id, c.currency_code, c.currency_name,
                       COALESCE(rh.encoded_by, '') AS encoded_by,
                       COALESCE(rh.checked_by, '') AS checked_by,
                       COALESCE(rh.approved_by, '') AS approved_by,
                       rh.total_amount
                FROM receiving_headers rh
                JOIN suppliers s ON s.supplier_id = rh.supplier_id
                JOIN branches b ON b.branch_id = rh.branch_id
                JOIN currencies c ON c.currency_id = rh.currency_id
                WHERE rh.receiving_id = ?
                LIMIT 1
                """;

        String lineSql = """
                SELECT rd.line_no, rd.group_code, rd.product_id, p.product_code, p.description,
                       rd.qty_delivered, rd.qty_doa, rd.qty_rejected, COALESCE(rd.reject_reason, ''),
                       COALESCE(rd.tank, ''), rd.qty_bought, rd.unit_cost, rd.total_cost, rd.stop_flag
                FROM receiving_details rd
                JOIN products p ON p.product_id = rd.product_id
                WHERE rd.receiving_id = ?
                ORDER BY rd.line_no
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement psHeader = conn.prepareStatement(headerSql);
             PreparedStatement psLines = conn.prepareStatement(lineSql)) {

            psHeader.setLong(1, receivingId);
            ReceivingHeader header = null;
            try (ResultSet rs = psHeader.executeQuery()) {
                if (rs.next()) {
                    header = new ReceivingHeader();
                    header.setReceivingId(rs.getLong("receiving_id"));
                    header.setRrNo(rs.getString("rr_no"));
                    header.setDateReceived(rs.getDate("date_received").toLocalDate());
                    header.setDirectPurchase(rs.getBoolean("direct_purchase"));
                    header.setCancelled(rs.getBoolean("is_cancelled"));
                    header.setSupplierId(rs.getLong("supplier_id"));
                    header.setSupplierCode(rs.getString("supplier_code"));
                    header.setSupplierName(rs.getString("supplier_name"));
                    header.setLtpNo(rs.getString("ltp_no"));
                    header.setBranchId(rs.getLong("branch_id"));
                    header.setBranchCode(rs.getString("branch_code"));
                    header.setBranchName(rs.getString("branch_name"));
                    header.setCurrencyId(rs.getLong("currency_id"));
                    header.setCurrencyCode(rs.getString("currency_code"));
                    header.setCurrencyName(rs.getString("currency_name"));
                    header.setEncodedBy(rs.getString("encoded_by"));
                    header.setCheckedBy(rs.getString("checked_by"));
                    header.setApprovedBy(rs.getString("approved_by"));
                    header.setTotalAmount(rs.getBigDecimal("total_amount"));
                }
            }

            if (header == null) {
                return null;
            }

            psLines.setLong(1, receivingId);
            try (ResultSet rs = psLines.executeQuery()) {
                while (rs.next()) {
                    ReceivingLine line = new ReceivingLine();
                    line.setLineNo(rs.getInt("line_no"));
                    line.setGroupCode(rs.getString("group_code"));
                    line.setProductId(rs.getLong("product_id"));
                    line.setProductCode(rs.getString("product_code"));
                    line.setProductDescription(rs.getString("description"));
                    line.setQtyDelivered(rs.getBigDecimal("qty_delivered"));
                    line.setQtyDoa(rs.getBigDecimal("qty_doa"));
                    line.setQtyRejected(rs.getBigDecimal("qty_rejected"));
                    line.setRejectReason(rs.getString(9));
                    line.setTank(rs.getString(10));
                    line.setQtyBought(rs.getBigDecimal("qty_bought"));
                    line.setUnitCost(rs.getBigDecimal("unit_cost"));
                    line.setTotalCost(rs.getBigDecimal("total_cost"));
                    line.setStopFlag(rs.getBoolean("stop_flag"));
                    header.getLines().add(line);
                }
            }

            return header;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load receiving transaction: " + e.getMessage(), e);
        }
    }

    public void delete(long receivingId) {
        String selectHeaderSql = """
                SELECT receiving_id, branch_id
                FROM receiving_headers
                WHERE receiving_id = ?
                LIMIT 1
                """;

        String selectLinesSql = """
                SELECT product_id, qty_bought, unit_cost
                FROM receiving_details
                WHERE receiving_id = ?
                """;

        String deleteLedgerSql = "DELETE FROM inventory_ledger WHERE transaction_type = 'RECEIVING_PURCHASE' AND reference_id = ?";
        String deleteHeaderSql = "DELETE FROM receiving_headers WHERE receiving_id = ?";
        String getProductSql = "SELECT total_quantity, average_cost FROM products WHERE product_id = ? FOR UPDATE";
        String updateProductSql = """
                UPDATE products
                SET total_quantity = ?,
                    average_cost = ?,
                    updated_at = NOW()
                WHERE product_id = ?
                """;

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psHeader = conn.prepareStatement(selectHeaderSql);
                 PreparedStatement psLines = conn.prepareStatement(selectLinesSql);
                 PreparedStatement psDeleteLedger = conn.prepareStatement(deleteLedgerSql);
                 PreparedStatement psDeleteHeader = conn.prepareStatement(deleteHeaderSql);
                 PreparedStatement psGetProduct = conn.prepareStatement(getProductSql);
                 PreparedStatement psUpdateProduct = conn.prepareStatement(updateProductSql)) {

                psHeader.setLong(1, receivingId);
                try (ResultSet rs = psHeader.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Receiving transaction not found.");
                    }
                }

                psLines.setLong(1, receivingId);
                try (ResultSet rs = psLines.executeQuery()) {
                    while (rs.next()) {
                        long productId = rs.getLong("product_id");
                        BigDecimal qtyBought = rs.getBigDecimal("qty_bought");
                        if (qtyBought == null) qtyBought = BigDecimal.ZERO;

                        psGetProduct.setLong(1, productId);
                        BigDecimal currentQty = BigDecimal.ZERO;
                        BigDecimal currentAvg = BigDecimal.ZERO;
                        try (ResultSet rp = psGetProduct.executeQuery()) {
                            if (rp.next()) {
                                currentQty = rp.getBigDecimal("total_quantity");
                                currentAvg = rp.getBigDecimal("average_cost");
                                if (currentQty == null) currentQty = BigDecimal.ZERO;
                                if (currentAvg == null) currentAvg = BigDecimal.ZERO;
                            }
                        }

                        BigDecimal newQty = currentQty.subtract(qtyBought);
                        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
                            newQty = BigDecimal.ZERO;
                        }

                        psUpdateProduct.setBigDecimal(1, newQty);
                        psUpdateProduct.setBigDecimal(2, currentAvg);
                        psUpdateProduct.setLong(3, productId);
                        psUpdateProduct.addBatch();
                    }
                }

                psUpdateProduct.executeBatch();

                psDeleteLedger.setLong(1, receivingId);
                psDeleteLedger.executeUpdate();

                psDeleteHeader.setLong(1, receivingId);
                psDeleteHeader.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete receiving transaction: " + e.getMessage(), e);
        }
    }

    public void toggleCancelled(long receivingId) {
        String sql = """
                UPDATE receiving_headers
                SET is_cancelled = NOT is_cancelled,
                    updated_at = NOW()
                WHERE receiving_id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, receivingId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to toggle cancel/recall: " + e.getMessage(), e);
        }
    }

    public void save(ReceivingHeader header) {
        if (header.getReceivingId() > 0) {
            update(header);
        } else {
            insert(header);
        }
    }

    private void insert(ReceivingHeader header) {
        String insertHeaderSql = """
                INSERT INTO receiving_headers
                (rr_no, date_received, direct_purchase, is_cancelled, supplier_id, ltp_no, branch_id, currency_id,
                 encoded_by, checked_by, approved_by, total_amount, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """;

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            long receivingId;

            try (PreparedStatement psHeader = conn.prepareStatement(insertHeaderSql, Statement.RETURN_GENERATED_KEYS)) {
                bindHeader(psHeader, header, false);
                psHeader.executeUpdate();
                try (ResultSet rs = psHeader.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("Failed to create receiving header.");
                    }
                    receivingId = rs.getLong(1);
                }
            }

            header.setReceivingId(receivingId);
            replaceLinesAndLedger(conn, header, false);
            conn.commit();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new RuntimeException("Save failed. R.R. No. already exists.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to save receiving transaction: " + e.getMessage(), e);
        }
    }

    private void update(ReceivingHeader header) {
        String updateHeaderSql = """
                UPDATE receiving_headers
                SET rr_no = ?,
                    date_received = ?,
                    direct_purchase = ?,
                    is_cancelled = ?,
                    supplier_id = ?,
                    ltp_no = ?,
                    branch_id = ?,
                    currency_id = ?,
                    encoded_by = ?,
                    checked_by = ?,
                    approved_by = ?,
                    total_amount = ?,
                    updated_at = NOW()
                WHERE receiving_id = ?
                """;

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psHeader = conn.prepareStatement(updateHeaderSql)) {
                bindHeader(psHeader, header, true);
                psHeader.executeUpdate();
            }

            replaceLinesAndLedger(conn, header, true);
            conn.commit();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new RuntimeException("Save failed. R.R. No. already exists.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to update receiving transaction: " + e.getMessage(), e);
        }
    }

    private void bindHeader(PreparedStatement psHeader, ReceivingHeader header, boolean includeId) throws Exception {
        psHeader.setString(1, header.getRrNo());
        psHeader.setDate(2, Date.valueOf(header.getDateReceived()));
        psHeader.setBoolean(3, header.isDirectPurchase());
        psHeader.setBoolean(4, header.isCancelled());
        psHeader.setLong(5, header.getSupplierId());
        psHeader.setString(6, header.getLtpNo());
        psHeader.setLong(7, header.getBranchId());
        psHeader.setLong(8, header.getCurrencyId());
        psHeader.setString(9, header.getEncodedBy());
        psHeader.setString(10, header.getCheckedBy());
        psHeader.setString(11, header.getApprovedBy());
        psHeader.setBigDecimal(12, header.getTotalAmount());
        if (includeId) {
            psHeader.setLong(13, header.getReceivingId());
        }
    }

    private void replaceLinesAndLedger(Connection conn, ReceivingHeader header, boolean restoreOldFirst) throws Exception {
        String selectOldLinesSql = """
                SELECT product_id, qty_bought
                FROM receiving_details
                WHERE receiving_id = ?
                """;
        String deleteLinesSql = "DELETE FROM receiving_details WHERE receiving_id = ?";
        String deleteLedgerSql = "DELETE FROM inventory_ledger WHERE transaction_type = 'RECEIVING_PURCHASE' AND reference_id = ?";

        String insertLineSql = """
                INSERT INTO receiving_details
                (receiving_id, line_no, group_code, product_id, qty_delivered, qty_doa, qty_rejected,
                 reject_reason, tank, qty_bought, unit_cost, total_cost, stop_flag)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String insertLedgerSql = """
                INSERT INTO inventory_ledger
                (transaction_type, reference_id, reference_no, line_no, transaction_date, product_id, branch_id,
                 qty_in, qty_out, unit_cost, total_cost, remarks, created_at)
                VALUES ('RECEIVING_PURCHASE', ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?, NOW())
                """;

        String getProductSql = """
                SELECT total_quantity, average_cost
                FROM products
                WHERE product_id = ?
                FOR UPDATE
                """;

        String updateProductQtyAvgSql = """
                UPDATE products
                SET previous_cost = average_cost,
                    average_cost = ?,
                    total_quantity = ?,
                    updated_at = NOW()
                WHERE product_id = ?
                """;

        try (PreparedStatement psSelectOldLines = conn.prepareStatement(selectOldLinesSql);
             PreparedStatement psDeleteLines = conn.prepareStatement(deleteLinesSql);
             PreparedStatement psDeleteLedger = conn.prepareStatement(deleteLedgerSql);
             PreparedStatement psInsertLine = conn.prepareStatement(insertLineSql);
             PreparedStatement psInsertLedger = conn.prepareStatement(insertLedgerSql);
             PreparedStatement psGetProduct = conn.prepareStatement(getProductSql);
             PreparedStatement psUpdateProduct = conn.prepareStatement(updateProductQtyAvgSql)) {

            if (restoreOldFirst) {
                psSelectOldLines.setLong(1, header.getReceivingId());
                try (ResultSet rs = psSelectOldLines.executeQuery()) {
                    while (rs.next()) {
                        long productId = rs.getLong("product_id");
                        BigDecimal qtyBought = rs.getBigDecimal("qty_bought");
                        if (qtyBought == null) qtyBought = BigDecimal.ZERO;

                        psGetProduct.setLong(1, productId);
                        BigDecimal currentQty = BigDecimal.ZERO;
                        BigDecimal currentAvg = BigDecimal.ZERO;
                        try (ResultSet rp = psGetProduct.executeQuery()) {
                            if (rp.next()) {
                                currentQty = rp.getBigDecimal("total_quantity");
                                currentAvg = rp.getBigDecimal("average_cost");
                                if (currentQty == null) currentQty = BigDecimal.ZERO;
                                if (currentAvg == null) currentAvg = BigDecimal.ZERO;
                            }
                        }

                        BigDecimal newQty = currentQty.subtract(qtyBought);
                        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
                            newQty = BigDecimal.ZERO;
                        }
                        psUpdateProduct.setBigDecimal(1, currentAvg);
                        psUpdateProduct.setBigDecimal(2, newQty);
                        psUpdateProduct.setLong(3, productId);
                        psUpdateProduct.addBatch();
                    }
                }
                psUpdateProduct.executeBatch();
                psDeleteLedger.setLong(1, header.getReceivingId());
                psDeleteLedger.executeUpdate();
                psDeleteLines.setLong(1, header.getReceivingId());
                psDeleteLines.executeUpdate();
            }

            for (ReceivingLine line : header.getLines()) {
                line.recompute();

                psInsertLine.setLong(1, header.getReceivingId());
                psInsertLine.setInt(2, line.getLineNo());
                psInsertLine.setString(3, line.getGroupCode());
                psInsertLine.setLong(4, line.getProductId());
                psInsertLine.setBigDecimal(5, line.getQtyDelivered());
                psInsertLine.setBigDecimal(6, line.getQtyDoa());
                psInsertLine.setBigDecimal(7, line.getQtyRejected());
                psInsertLine.setString(8, line.getRejectReason());
                psInsertLine.setString(9, line.getTank());
                psInsertLine.setBigDecimal(10, line.getQtyBought());
                psInsertLine.setBigDecimal(11, line.getUnitCost());
                psInsertLine.setBigDecimal(12, line.getTotalCost());
                psInsertLine.setBoolean(13, line.isStopFlag());
                psInsertLine.addBatch();

                psInsertLedger.setLong(1, header.getReceivingId());
                psInsertLedger.setString(2, header.getRrNo());
                psInsertLedger.setInt(3, line.getLineNo());
                psInsertLedger.setDate(4, Date.valueOf(header.getDateReceived()));
                psInsertLedger.setLong(5, line.getProductId());
                psInsertLedger.setLong(6, header.getBranchId());
                psInsertLedger.setBigDecimal(7, line.getQtyBought());
                psInsertLedger.setBigDecimal(8, line.getUnitCost());
                psInsertLedger.setBigDecimal(9, line.getTotalCost());
                psInsertLedger.setString(10, "R.R. No. " + header.getRrNo());
                psInsertLedger.addBatch();

                psGetProduct.setLong(1, line.getProductId());
                BigDecimal currentQty = BigDecimal.ZERO;
                BigDecimal currentAvg = BigDecimal.ZERO;
                try (ResultSet rs = psGetProduct.executeQuery()) {
                    if (rs.next()) {
                        currentQty = rs.getBigDecimal("total_quantity");
                        currentAvg = rs.getBigDecimal("average_cost");
                        if (currentQty == null) currentQty = BigDecimal.ZERO;
                        if (currentAvg == null) currentAvg = BigDecimal.ZERO;
                    }
                }

                BigDecimal newQty = currentQty.add(line.getQtyBought());
                BigDecimal newAvg;
                if (newQty.compareTo(BigDecimal.ZERO) <= 0) {
                    newAvg = line.getUnitCost();
                } else {
                    BigDecimal oldValue = currentQty.multiply(currentAvg);
                    BigDecimal newValue = line.getQtyBought().multiply(line.getUnitCost());
                    newAvg = oldValue.add(newValue).divide(newQty, 4, RoundingMode.HALF_UP);
                }

                psUpdateProduct.setBigDecimal(1, newAvg);
                psUpdateProduct.setBigDecimal(2, newQty);
                psUpdateProduct.setLong(3, line.getProductId());
                psUpdateProduct.addBatch();
            }

            psInsertLine.executeBatch();
            psInsertLedger.executeBatch();
            psUpdateProduct.executeBatch();
        }
    }

    public static class BrowseRow {
        private final long receivingId;
        private final String rrNo;
        private final String supplierCode;
        private final String supplierName;
        private final String suppRef;
        private final String dateText;
        private final BigDecimal fishPurchase;
        private final BigDecimal cvAmount;
        private final BigDecimal amountPaid;
        private final BigDecimal balance;
        private final boolean cancelled;

        public BrowseRow(long receivingId, String rrNo, String supplierCode, String supplierName, String suppRef,
                         String dateText, BigDecimal fishPurchase, BigDecimal cvAmount, BigDecimal amountPaid,
                         BigDecimal balance, boolean cancelled) {
            this.receivingId = receivingId;
            this.rrNo = rrNo;
            this.supplierCode = supplierCode;
            this.supplierName = supplierName;
            this.suppRef = suppRef;
            this.dateText = dateText;
            this.fishPurchase = fishPurchase;
            this.cvAmount = cvAmount;
            this.amountPaid = amountPaid;
            this.balance = balance;
            this.cancelled = cancelled;
        }

        public long getReceivingId() { return receivingId; }
        public String getRrNo() { return rrNo; }
        public String getSupplierCode() { return supplierCode; }
        public String getSupplierName() { return supplierName; }
        public String getSuppRef() { return suppRef; }
        public String getDateText() { return dateText; }
        public BigDecimal getFishPurchase() { return fishPurchase; }
        public BigDecimal getCvAmount() { return cvAmount; }
        public BigDecimal getAmountPaid() { return amountPaid; }
        public BigDecimal getBalance() { return balance; }
        public boolean isCancelled() { return cancelled; }
        public Object[] toRow() {
            return new Object[]{rrNo, supplierCode, supplierName, suppRef, dateText, fishPurchase, cvAmount, amountPaid, balance};
        }
    }

    public List<BrowseRow> findBrowseRows(String orderBy) {
        String orderClause = switch (orderBy) {
            case "SUPPLIER" -> "s.supplier_code, rh.date_received ASC, rh.rr_no ASC";
            case "DATE" -> "rh.date_received ASC, rh.rr_no ASC";
            default -> "rh.rr_no ASC";
        };

        String sql = """
                SELECT rh.receiving_id, rh.rr_no, s.supplier_code, s.supplier_name,
                       COALESCE(rh.ltp_no, '') AS supp_ref,
                       DATE_FORMAT(rh.date_received, '%m/%d/%Y') AS date_text,
                       rh.total_amount AS fish_purchase,
                       rh.total_amount AS cv_amount,
                       0.00 AS amount_paid,
                       rh.total_amount AS balance,
                       rh.is_cancelled
                FROM receiving_headers rh
                JOIN suppliers s ON s.supplier_id = rh.supplier_id
                ORDER BY __ORDER__
                """.replace("__ORDER__", orderClause);

        List<BrowseRow> rows = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new BrowseRow(
                        rs.getLong("receiving_id"),
                        rs.getString("rr_no"),
                        rs.getString("supplier_code"),
                        rs.getString("supplier_name"),
                        rs.getString("supp_ref"),
                        rs.getString("date_text"),
                        rs.getBigDecimal("fish_purchase"),
                        rs.getBigDecimal("cv_amount"),
                        rs.getBigDecimal("amount_paid"),
                        rs.getBigDecimal("balance"),
                        rs.getBoolean("is_cancelled")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load receiving browse rows: " + e.getMessage(), e);
        }
        return rows;
    }
}
