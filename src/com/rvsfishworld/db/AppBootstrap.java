package com.rvsfishworld.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class AppBootstrap {
    private AppBootstrap() {
    }

    public static void ensureReady() {
        try (Connection conn = Database.getConnection()) {
            ensureInvoicePricingSchema(conn);
            ensureGenericDocumentSchema(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Application bootstrap failed: " + e.getMessage(), e);
        }
    }

    private static void ensureInvoicePricingSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS invoice_special_prices (
                        invoice_special_price_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        product_code VARCHAR(40) NOT NULL,
                        special_price DECIMAL(18,4) NOT NULL DEFAULT 0,
                        remarks VARCHAR(255),
                        is_active BIT NOT NULL DEFAULT b'1',
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS dated_special_prices (
                        dated_special_price_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        pricing_date DATE NOT NULL,
                        product_code VARCHAR(40) NOT NULL,
                        special_price DECIMAL(18,4) NOT NULL DEFAULT 0,
                        remarks VARCHAR(255),
                        is_active BIT NOT NULL DEFAULT b'1',
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    )
                    """);
        }

        ensureIndex(conn, "invoice_special_prices", "idx_invoice_special_prices_product_active",
                "CREATE INDEX idx_invoice_special_prices_product_active ON invoice_special_prices(product_code, is_active)");
        ensureIndex(conn, "dated_special_prices", "idx_dated_special_prices_product_date_active",
                "CREATE INDEX idx_dated_special_prices_product_date_active ON dated_special_prices(product_code, pricing_date, is_active)");
    }

    private static void ensureGenericDocumentSchema(Connection conn) throws SQLException {
        ensureColumn(conn, "generic_documents", "source_proforma_id",
                "ALTER TABLE generic_documents ADD COLUMN source_proforma_id BIGINT NULL");
        ensureColumn(conn, "generic_documents", "source_proforma_no",
                "ALTER TABLE generic_documents ADD COLUMN source_proforma_no VARCHAR(40) NULL");
        ensureColumn(conn, "generic_documents", "invoice_no",
                "ALTER TABLE generic_documents ADD COLUMN invoice_no VARCHAR(40) NULL");
        ensureColumn(conn, "generic_documents", "price_code",
                "ALTER TABLE generic_documents ADD COLUMN price_code VARCHAR(20) NULL");
        ensureColumn(conn, "generic_documents", "apply_formula",
                "ALTER TABLE generic_documents ADD COLUMN apply_formula BIT NOT NULL DEFAULT b'0'");
        ensureColumn(conn, "generic_documents", "rate2_amount",
                "ALTER TABLE generic_documents ADD COLUMN rate2_amount DECIMAL(18,4) NOT NULL DEFAULT 0");
        ensureIndex(conn, "generic_documents", "idx_generic_documents_type_status_date",
                "CREATE INDEX idx_generic_documents_type_status_date ON generic_documents(document_type, status, document_date)");
        ensureIndex(conn, "generic_documents", "idx_generic_documents_type_docno",
                "CREATE INDEX idx_generic_documents_type_docno ON generic_documents(document_type, document_no)");
        ensureIndex(conn, "generic_documents", "idx_generic_documents_source_proforma",
                "CREATE INDEX idx_generic_documents_source_proforma ON generic_documents(source_proforma_no)");
        ensureIndex(conn, "generic_document_lines", "idx_generic_document_lines_document_line",
                "CREATE INDEX idx_generic_document_lines_document_line ON generic_document_lines(document_id, line_no)");
    }

    private static void ensureColumn(Connection conn, String tableName, String columnName, String ddl) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
            if (rs.next()) {
                return;
            }
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        }
    }

    private static void ensureIndex(Connection conn, String tableName, String indexName, String ddl) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getIndexInfo(null, null, tableName, false, false)) {
            while (rs.next()) {
                String existing = rs.getString("INDEX_NAME");
                if (existing != null && existing.equalsIgnoreCase(indexName)) {
                    return;
                }
            }
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        }
    }
}
