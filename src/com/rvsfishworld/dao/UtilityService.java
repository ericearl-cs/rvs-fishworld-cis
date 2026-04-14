package com.rvsfishworld.dao;

import com.rvsfishworld.AppRuntime;
import com.rvsfishworld.db.Database;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class UtilityService {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public Path createBackup() {
        try {
            Path dir = Path.of("logs", "backups");
            Files.createDirectories(dir);
            Path file = dir.resolve("backup_" + TS.format(LocalDateTime.now()) + ".txt");
            Map<String, String> summary = buildDatabaseSummary();
            StringBuilder text = new StringBuilder();
            text.append("RVS FISHWORLD CIS BACKUP SUMMARY").append(System.lineSeparator());
            text.append("Generated: ").append(LocalDateTime.now()).append(System.lineSeparator());
            text.append("User: ").append(AppRuntime.username()).append(System.lineSeparator()).append(System.lineSeparator());
            summary.forEach((key, value) -> text.append(key).append(": ").append(value).append(System.lineSeparator()));
            Files.writeString(file, text.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logBackupJob(file, "CREATED", "Summary backup generated.");
            return file;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create backup: " + e.getMessage(), e);
        }
    }

    public Path postTodayInventory(LocalDate date) {
        try {
            Path dir = Path.of("logs", "inventory_posts");
            Files.createDirectories(dir);
            Path file = dir.resolve("inventory_" + date + ".txt");
            String text = buildInventorySummary(date);
            Files.writeString(file, text, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            logMaintenance(date, "POST_TODAY_INVENTORY", "SUCCESS", "Inventory summary written to " + file.toAbsolutePath());
            return file;
        } catch (Exception e) {
            throw new RuntimeException("Failed to post today's inventory: " + e.getMessage(), e);
        }
    }

    private Map<String, String> buildDatabaseSummary() throws Exception {
        Map<String, String> summary = new LinkedHashMap<>();
        try (Connection conn = Database.getConnection()) {
            summary.put("Products", count(conn, "SELECT COUNT(*) FROM products"));
            summary.put("Suppliers", count(conn, "SELECT COUNT(*) FROM suppliers"));
            summary.put("Customers", count(conn, "SELECT COUNT(*) FROM customers"));
            summary.put("Receiving Headers", count(conn, "SELECT COUNT(*) FROM receiving_headers"));
            summary.put("Proformas", count(conn, "SELECT COUNT(*) FROM generic_documents WHERE document_type = 'PROFORMA'"));
            summary.put("Sales Invoices", count(conn, "SELECT COUNT(*) FROM generic_documents WHERE document_type = 'SALES_INVOICE'"));
            summary.put("Mortality", count(conn, "SELECT COUNT(*) FROM generic_documents WHERE document_type = 'MORTALITY'"));
        }
        return summary;
    }

    private String buildInventorySummary(LocalDate date) throws Exception {
        try (Connection conn = Database.getConnection()) {
            BigDecimal productQty = sum(conn, "SELECT COALESCE(SUM(total_quantity), 0) FROM products");
            BigDecimal receivingToday = sum(conn,
                    "SELECT COALESCE(SUM(total_amount), 0) FROM receiving_headers WHERE DATE(date_received) = ?", Date.valueOf(date));
            BigDecimal invoicesToday = sum(conn,
                    "SELECT COALESCE(SUM(total_payables), 0) FROM generic_documents WHERE document_type = 'SALES_INVOICE' AND document_date = ?",
                    Date.valueOf(date));
            BigDecimal mortalityToday = sum(conn,
                    "SELECT COALESCE(SUM(amount), 0) FROM generic_documents WHERE document_type = 'MORTALITY' AND document_date = ?",
                    Date.valueOf(date));
            return """
                    RVS FISHWORLD CIS INVENTORY POST
                    Date: %s
                    User: %s

                    Product Total Quantity: %s
                    Receiving Today: %s
                    Sales Invoice Today: %s
                    Mortality Today: %s
                    """.formatted(
                    date,
                    AppRuntime.username(),
                    productQty.toPlainString(),
                    receivingToday.toPlainString(),
                    invoicesToday.toPlainString(),
                    mortalityToday.toPlainString());
        }
    }

    private void logBackupJob(Path file, String status, String details) throws Exception {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO backup_jobs(created_by, file_path, status, details) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, AppRuntime.username());
            ps.setString(2, file.toAbsolutePath().toString());
            ps.setString(3, status);
            ps.setString(4, details);
            ps.executeUpdate();
        }
    }

    private void logMaintenance(LocalDate date, String operation, String status, String details) throws Exception {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO maintenance_runs(run_by, operation_name, status, details) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, AppRuntime.username());
            ps.setString(2, operation + " " + date);
            ps.setString(3, status);
            ps.setString(4, details);
            ps.executeUpdate();
        }
    }

    private String count(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return Long.toString(rs.getLong(1));
        }
    }

    private BigDecimal sum(Connection conn, String sql, Object... params) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1) == null ? BigDecimal.ZERO : rs.getBigDecimal(1);
            }
        }
    }
}
