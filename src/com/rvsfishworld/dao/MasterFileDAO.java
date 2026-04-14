package com.rvsfishworld.dao;

import com.rvsfishworld.db.Database;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.math.BigDecimal;
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
                WHERE is_active = TRUE
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

    public Map<String, Object> findProduct(String productCode) {
        String sql = """
                SELECT
                    p.product_code,
                    p.description,
                    COALESCE(p.scientific_name, '') AS scientific_name,
                    COALESCE(c.category_code, '') AS category_code,
                    COALESCE(c.category_name, '') AS category_name,
                    COALESCE(b.brand_code, '') AS brand_code,
                    COALESCE(b.brand_name, '') AS brand_name,
                    COALESCE(p.unit_of_measure, '') AS unit_of_measure,
                    p.is_invertebrate,
                    COALESCE(p.extended_description, '') AS extended_description,
                    COALESCE(p.reorder_point, 0) AS reorder_point,
                    COALESCE(p.maximum_point, 0) AS maximum_point,
                    COALESCE(p.price_a, 0) AS price_a,
                    COALESCE(p.price_b, 0) AS price_b,
                    COALESCE(p.price_c, 0) AS price_c,
                    COALESCE(p.price_d, 0) AS price_d,
                    COALESCE(p.price_e, 0) AS price_e,
                    COALESCE(p.price_f, 0) AS price_f,
                    COALESCE(p.price_g, 0) AS price_g,
                    COALESCE(p.special_price, 0) AS special_price,
                    COALESCE(p.local_sales_price, 0) AS local_sales_price,
                    COALESCE(p.deliveries_price, 0) AS deliveries_price,
                    COALESCE(p.total_quantity, 0) AS total_quantity,
                    COALESCE(p.previous_cost, 0) AS previous_cost,
                    COALESCE(p.average_cost, 0) AS average_cost
                FROM products p
                LEFT JOIN categories c ON c.category_id = p.category_id
                LEFT JOIN brands b ON b.brand_id = p.brand_id
                WHERE p.product_code = ?
                LIMIT 1
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Map.of();
                }
                Map<String, Object> values = new LinkedHashMap<>();
                values.put("original_product_code", rs.getString("product_code"));
                values.put("product_code", rs.getString("product_code"));
                values.put("description", rs.getString("description"));
                values.put("scientific_name", rs.getString("scientific_name"));
                values.put("category_code", rs.getString("category_code"));
                values.put("category_name", rs.getString("category_name"));
                values.put("brand_code", rs.getString("brand_code"));
                values.put("brand_name", rs.getString("brand_name"));
                values.put("unit_of_measure", rs.getString("unit_of_measure"));
                values.put("is_invertebrate", rs.getBoolean("is_invertebrate"));
                values.put("extended_description", rs.getString("extended_description"));
                values.put("reorder_point", rs.getBigDecimal("reorder_point"));
                values.put("maximum_point", rs.getBigDecimal("maximum_point"));
                values.put("price_a", rs.getBigDecimal("price_a"));
                values.put("price_b", rs.getBigDecimal("price_b"));
                values.put("price_c", rs.getBigDecimal("price_c"));
                values.put("price_d", rs.getBigDecimal("price_d"));
                values.put("price_e", rs.getBigDecimal("price_e"));
                values.put("price_f", rs.getBigDecimal("price_f"));
                values.put("price_g", rs.getBigDecimal("price_g"));
                values.put("special_price", rs.getBigDecimal("special_price"));
                values.put("local_sales_price", rs.getBigDecimal("local_sales_price"));
                values.put("deliveries_price", rs.getBigDecimal("deliveries_price"));
                values.put("total_quantity", rs.getBigDecimal("total_quantity"));
                values.put("previous_cost", rs.getBigDecimal("previous_cost"));
                values.put("average_cost", rs.getBigDecimal("average_cost"));
                return values;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load product: " + e.getMessage(), e);
        }
    }

    public void saveProduct(Map<String, Object> values) {
        String originalCode = text(values.get("original_product_code"));
        String code = text(values.get("product_code"));
        String description = text(values.get("description"));
        String scientificName = text(values.get("scientific_name"));
        String categoryCode = text(values.get("category_code"));
        String brandCode = text(values.get("brand_code"));
        String unitOfMeasure = text(values.get("unit_of_measure"));
        boolean isInvertebrate = truthy(values.get("is_invertebrate"));
        String extendedDescription = text(values.get("extended_description"));
        BigDecimal reorderPoint = decimal(values.get("reorder_point"));
        BigDecimal maximumPoint = decimal(values.get("maximum_point"));
        BigDecimal priceA = decimal(values.get("price_a"));
        BigDecimal priceB = decimal(values.get("price_b"));
        BigDecimal priceC = decimal(values.get("price_c"));
        BigDecimal priceD = decimal(values.get("price_d"));
        BigDecimal priceE = decimal(values.get("price_e"));
        BigDecimal priceF = decimal(values.get("price_f"));
        BigDecimal priceG = decimal(values.get("price_g"));
        BigDecimal specialPrice = decimal(values.get("special_price"));
        BigDecimal localSalesPrice = decimal(values.get("local_sales_price"));
        BigDecimal deliveriesPrice = decimal(values.get("deliveries_price"));

        if (code.isBlank() || description.isBlank() || categoryCode.isBlank()) {
            throw new IllegalArgumentException("Product code, description, and category are required.");
        }

        try (Connection conn = Database.getConnection()) {
            Long categoryId = findCategoryId(conn, categoryCode);
            if (categoryId == null) {
                throw new IllegalArgumentException("Category code not found: " + categoryCode);
            }
            Long brandId = null;
            if (!brandCode.isBlank()) {
                brandId = findBrandId(conn, brandCode);
                if (brandId == null) {
                    throw new IllegalArgumentException("Brand code not found: " + brandCode);
                }
            }

            Long productId = findProductId(conn, originalCode.isBlank() ? code : originalCode);
            if (productId == null) {
                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO products (
                            product_code, description, scientific_name, category_id, brand_id,
                            unit_of_measure, is_invertebrate, extended_description,
                            reorder_point, maximum_point,
                            price_a, price_b, price_c, price_d, price_e, price_f, price_g,
                            special_price, local_sales_price, deliveries_price, is_active
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)
                        """)) {
                    bindProduct(ps, code, description, scientificName, categoryId, brandId, unitOfMeasure, isInvertebrate,
                            extendedDescription, reorderPoint, maximumPoint, priceA, priceB, priceC, priceD, priceE,
                            priceF, priceG, specialPrice, localSalesPrice, deliveriesPrice);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement("""
                        UPDATE products
                        SET product_code = ?,
                            description = ?,
                            scientific_name = ?,
                            category_id = ?,
                            brand_id = ?,
                            unit_of_measure = ?,
                            is_invertebrate = ?,
                            extended_description = ?,
                            reorder_point = ?,
                            maximum_point = ?,
                            price_a = ?,
                            price_b = ?,
                            price_c = ?,
                            price_d = ?,
                            price_e = ?,
                            price_f = ?,
                            price_g = ?,
                            special_price = ?,
                            local_sales_price = ?,
                            deliveries_price = ?,
                            is_active = TRUE
                        WHERE product_id = ?
                        """)) {
                    bindProduct(ps, code, description, scientificName, categoryId, brandId, unitOfMeasure, isInvertebrate,
                            extendedDescription, reorderPoint, maximumPoint, priceA, priceB, priceC, priceD, priceE,
                            priceF, priceG, specialPrice, localSalesPrice, deliveriesPrice);
                    ps.setLong(21, productId);
                    ps.executeUpdate();
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save product: " + e.getMessage(), e);
        }
    }

    public void deleteProduct(String productCode) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE products SET is_active = FALSE WHERE product_code = ?")) {
            ps.setString(1, productCode);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete product: " + e.getMessage(), e);
        }
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

    private boolean truthy(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = value == null ? "" : value.toString().trim().toUpperCase();
        return "TRUE".equals(text) || "T".equals(text) || "1".equals(text) || "Y".equals(text);
    }

    private BigDecimal decimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        String text = value == null ? "" : value.toString().trim().replace(",", "");
        if (text.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(text);
    }

    private Long findCategoryId(Connection conn, String code) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT category_id FROM categories WHERE category_code = ? LIMIT 1")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    private Long findBrandId(Connection conn, String code) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT brand_id FROM brands WHERE brand_code = ? LIMIT 1")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    private Long findProductId(Connection conn, String code) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT product_id FROM products WHERE product_code = ? LIMIT 1")) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    private void bindProduct(
            PreparedStatement ps,
            String code,
            String description,
            String scientificName,
            Long categoryId,
            Long brandId,
            String unitOfMeasure,
            boolean isInvertebrate,
            String extendedDescription,
            BigDecimal reorderPoint,
            BigDecimal maximumPoint,
            BigDecimal priceA,
            BigDecimal priceB,
            BigDecimal priceC,
            BigDecimal priceD,
            BigDecimal priceE,
            BigDecimal priceF,
            BigDecimal priceG,
            BigDecimal specialPrice,
            BigDecimal localSalesPrice,
            BigDecimal deliveriesPrice) throws Exception {
        ps.setString(1, code);
        ps.setString(2, description);
        ps.setString(3, blankToNull(scientificName));
        ps.setLong(4, categoryId);
        if (brandId == null) {
            ps.setNull(5, java.sql.Types.BIGINT);
        } else {
            ps.setLong(5, brandId);
        }
        ps.setString(6, blankToNull(unitOfMeasure));
        ps.setBoolean(7, isInvertebrate);
        ps.setString(8, blankToNull(extendedDescription));
        ps.setBigDecimal(9, reorderPoint);
        ps.setBigDecimal(10, maximumPoint);
        ps.setBigDecimal(11, priceA);
        ps.setBigDecimal(12, priceB);
        ps.setBigDecimal(13, priceC);
        ps.setBigDecimal(14, priceD);
        ps.setBigDecimal(15, priceE);
        ps.setBigDecimal(16, priceF);
        ps.setBigDecimal(17, priceG);
        ps.setBigDecimal(18, specialPrice);
        ps.setBigDecimal(19, localSalesPrice);
        ps.setBigDecimal(20, deliveriesPrice);
    }
}
