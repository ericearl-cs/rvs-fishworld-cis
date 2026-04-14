package com.rvsfishworld.dao;

import com.rvsfishworld.db.Database;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MasterFileDAO {
    public List<String> listCodes(String tableName) {
        return Collections.emptyList();
    }

    public List<Object[]> loadSupplierProducts(String supplierCode) {
        List<Object[]> rows = new ArrayList<>();
        String sql = """
                SELECT product_code, description
                FROM products
                ORDER BY product_code
                LIMIT 200
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{rs.getString(1), rs.getString(2)});
            }
        } catch (Exception ignored) {
        }
        return rows;
    }

    public Map<String, Object> findCategory(String categoryCode) {
        String sql = """
                SELECT category_code, category_name, COALESCE(sort_code, '') AS sort_code
                FROM categories
                WHERE category_code = ?
                LIMIT 1
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> values = new LinkedHashMap<>();
                    values.put("category_code", rs.getString("category_code"));
                    values.put("category_name", rs.getString("category_name"));
                    values.put("sort_code", rs.getString("sort_code"));
                    return values;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load category: " + e.getMessage(), e);
        }
        return Map.of();
    }

    public void saveCategory(Map<String, Object> values) {
        String code = text(values.get("category_code"));
        String name = text(values.get("category_name"));
        String sort = text(values.get("sort_code"));
        if (code.isBlank() || name.isBlank()) {
            throw new IllegalArgumentException("Category code and name are required.");
        }
        try (Connection conn = Database.getConnection()) {
            Long existingId = null;
            try (PreparedStatement find = conn.prepareStatement("SELECT category_id FROM categories WHERE category_code = ? LIMIT 1")) {
                find.setString(1, code);
                try (ResultSet rs = find.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getLong(1);
                    }
                }
            }
            if (existingId == null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO categories (category_code, category_name, sort_code) VALUES (?, ?, ?)")) {
                    ps.setString(1, code);
                    ps.setString(2, name);
                    ps.setString(3, blankToNull(sort));
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE categories SET category_name = ?, sort_code = ? WHERE category_id = ?")) {
                    ps.setString(1, name);
                    ps.setString(2, blankToNull(sort));
                    ps.setLong(3, existingId);
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save category: " + e.getMessage(), e);
        }
    }

    public void deleteCategory(String categoryCode) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM categories WHERE category_code = ?")) {
            ps.setString(1, categoryCode);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete category: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> findTransShipper(String code) {
        String sql = """
                SELECT trans_shipper_code, trans_shipper_name, COALESCE(legacy_parent_customer_code, '') AS legacy_parent_customer_code
                FROM trans_shippers
                WHERE trans_shipper_code = ?
                LIMIT 1
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> values = new LinkedHashMap<>();
                    values.put("trans_shipper_code", rs.getString("trans_shipper_code"));
                    values.put("trans_shipper_name", rs.getString("trans_shipper_name"));
                    values.put("legacy_parent_customer_code", rs.getString("legacy_parent_customer_code"));
                    return values;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load tran-shipper: " + e.getMessage(), e);
        }
        return Map.of();
    }

    public void saveTransShipper(Map<String, Object> values) {
        String code = text(values.get("trans_shipper_code"));
        String name = text(values.get("trans_shipper_name"));
        String parent = text(values.get("legacy_parent_customer_code"));
        if (code.isBlank() || name.isBlank()) {
            throw new IllegalArgumentException("Tran-shipper code and name are required.");
        }
        try (Connection conn = Database.getConnection()) {
            Long existingId = null;
            try (PreparedStatement find = conn.prepareStatement("SELECT trans_shipper_id FROM trans_shippers WHERE trans_shipper_code = ? LIMIT 1")) {
                find.setString(1, code);
                try (ResultSet rs = find.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getLong(1);
                    }
                }
            }
            if (existingId == null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO trans_shippers (trans_shipper_code, trans_shipper_name, legacy_parent_customer_code, is_active) VALUES (?, ?, ?, TRUE)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, code);
                    ps.setString(2, name);
                    ps.setString(3, blankToNull(parent));
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE trans_shippers SET trans_shipper_name = ?, legacy_parent_customer_code = ? WHERE trans_shipper_id = ?")) {
                    ps.setString(1, name);
                    ps.setString(2, blankToNull(parent));
                    ps.setLong(3, existingId);
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save tran-shipper: " + e.getMessage(), e);
        }
    }

    public void deleteTransShipper(String code) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM trans_shippers WHERE trans_shipper_code = ?")) {
            ps.setString(1, code);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete tran-shipper: " + e.getMessage(), e);
        }
    }

    private String text(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
