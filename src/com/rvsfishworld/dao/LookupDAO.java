package com.rvsfishworld.dao;

import com.rvsfishworld.db.Database;
import com.rvsfishworld.model.LookupItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LookupDAO {

    public List<LookupItem> findSuppliers(String keyword) {
        String sql = """
                SELECT supplier_id, supplier_code, supplier_name
                FROM suppliers
                WHERE is_active = TRUE
                  AND (supplier_code LIKE ? OR supplier_name LIKE ?)
                ORDER BY supplier_code
                """;
        return findItems(sql, keyword);
    }

    public List<LookupItem> findBranches(String keyword) {
        String sql = """
                SELECT branch_id, branch_code, branch_name
                FROM branches
                WHERE is_active = TRUE
                  AND (branch_code LIKE ? OR branch_name LIKE ?)
                ORDER BY branch_code
                """;
        return findItems(sql, keyword);
    }

    public List<LookupItem> findCurrencies(String keyword) {
        String sql = """
                SELECT currency_id, currency_code, currency_name
                FROM currencies
                WHERE is_active = TRUE
                  AND (currency_code LIKE ? OR currency_name LIKE ?)
                ORDER BY currency_code
                """;
        return findItems(sql, keyword);
    }

    public List<LookupItem> findCustomers(String keyword) {
        String sql = """
                SELECT customer_id, customer_code, customer_name
                FROM customers
                WHERE is_active = TRUE
                  AND (customer_code LIKE ? OR customer_name LIKE ?)
                ORDER BY customer_code
                """;
        return findItems(sql, keyword);
    }

    public List<LookupItem> findSalesmen(String keyword) {
        String sql = """
                SELECT salesman_id, salesman_code, salesman_name
                FROM salesmen
                WHERE is_active = TRUE
                  AND (salesman_code LIKE ? OR salesman_name LIKE ?)
                ORDER BY salesman_code
                """;
        return findItems(sql, keyword);
    }

    public List<LookupItem> findProducts(String keyword) {
        String sql = """
                SELECT product_id, product_code, description
                FROM products
                WHERE is_active = TRUE
                  AND (product_code LIKE ? OR description LIKE ?)
                ORDER BY product_code
                """;
        return findItems(sql, keyword);
    }

    public LookupItem findProductByCode(String code) {
        String sql = """
                SELECT product_id, product_code, description
                FROM products
                WHERE product_code = ? AND is_active = TRUE
                LIMIT 1
                """;
        return findOne(sql, code);
    }

    public LookupItem findSupplierByCode(String code) {
        String sql = """
                SELECT supplier_id, supplier_code, supplier_name
                FROM suppliers
                WHERE supplier_code = ? AND is_active = TRUE
                LIMIT 1
                """;
        return findOne(sql, code);
    }

    public LookupItem findBranchByCode(String code) {
        String sql = """
                SELECT branch_id, branch_code, branch_name
                FROM branches
                WHERE branch_code = ? AND is_active = TRUE
                LIMIT 1
                """;
        return findOne(sql, code);
    }

    public LookupItem findCurrencyByCode(String code) {
        String sql = """
                SELECT currency_id, currency_code, currency_name
                FROM currencies
                WHERE is_active = TRUE
                  AND (currency_code = ?
                       OR (? = 'PESO' AND currency_code = 'PHP')
                       OR (? = 'PHP' AND currency_code = 'PESO'))
                LIMIT 1
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, code);
            ps.setString(3, code);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LookupItem(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3)
                    );
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lookup failed: " + e.getMessage(), e);
        }
        return null;
    }

    public LookupItem findCustomerByCode(String code) {
        String sql = """
                SELECT customer_id, customer_code, customer_name
                FROM customers
                WHERE customer_code = ? AND is_active = TRUE
                LIMIT 1
                """;
        return findOne(sql, code);
    }

    public LookupItem findSalesmanByCode(String code) {
        String sql = """
                SELECT salesman_id, salesman_code, salesman_name
                FROM salesmen
                WHERE salesman_code = ? AND is_active = TRUE
                LIMIT 1
                """;
        return findOne(sql, code);
    }


    public java.math.BigDecimal findSuggestedUnitCost(long productId) {
        String productSql = """
                SELECT average_cost, previous_cost
                FROM products
                WHERE product_id = ?
                LIMIT 1
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(productSql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.math.BigDecimal average = rs.getBigDecimal("average_cost");
                    java.math.BigDecimal previous = rs.getBigDecimal("previous_cost");
                    if (average != null && average.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        return average;
                    }
                    if (previous != null && previous.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        return previous;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lookup failed: " + e.getMessage(), e);
        }
        return java.math.BigDecimal.ZERO;
    }

    private List<LookupItem> findItems(String sql, String keyword) {
        List<LookupItem> items = new ArrayList<>();
        String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new LookupItem(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3)
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lookup failed: " + e.getMessage(), e);
        }

        return items;
    }

    private LookupItem findOne(String sql, String code) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LookupItem(
                            rs.getLong(1),
                            rs.getString(2),
                            rs.getString(3)
                    );
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lookup failed: " + e.getMessage(), e);
        }
        return null;
    }
}
