package com.rvsfishworld.dao;

import com.rvsfishworld.db.Database;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OperationalTableDAO {
    public record Row(long id, String code, String name, String extra, boolean active) {
    }

    public List<Row> browse(String kind) {
        Config config = config(kind);
        List<Row> rows = new ArrayList<>();
        try (var conn = Database.getConnection();
             var ps = conn.prepareStatement(config.browseSql);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Row(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getBoolean(5)));
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load " + config.title + ": " + e.getMessage(), e);
        }
    }

    public Map<String, Object> find(String kind, long id) {
        Config config = config(kind);
        try (var conn = Database.getConnection();
             var ps = conn.prepareStatement(config.findSql)) {
            ps.setLong(1, id);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Map.of();
                }
                Map<String, Object> values = new LinkedHashMap<>();
                values.put(config.idKey, rs.getLong(config.idKey));
                values.put(config.codeKey, rs.getString(config.codeKey));
                values.put(config.nameKey, rs.getString(config.nameKey));
                values.put(config.extraKey, rs.getString(config.extraKey));
                values.put("is_active", rs.getBoolean("is_active"));
                return values;
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to load " + config.title + ": " + e.getMessage(), e);
        }
    }

    public void save(String kind, Map<String, Object> values) {
        Config config = config(kind);
        boolean insert = blank(values.get(config.idKey));
        try (var conn = Database.getConnection();
             var ps = conn.prepareStatement(insert ? config.insertSql : config.updateSql)) {
            int index = 1;
            ps.setString(index++, text(values.get(config.codeKey)));
            ps.setString(index++, text(values.get(config.nameKey)));
            if (!config.extraKey.isBlank()) {
                ps.setString(index++, text(values.get(config.extraKey)));
            }
            ps.setBoolean(index++, parseBoolean(values.get("is_active")));
            if (!insert) {
                ps.setLong(index, Long.parseLong(String.valueOf(values.get(config.idKey))));
            }
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Unable to save " + config.title + ": " + e.getMessage(), e);
        }
    }

    public void delete(String kind, long id) {
        Config config = config(kind);
        try (var conn = Database.getConnection();
             var ps = conn.prepareStatement(config.deleteSql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Unable to delete " + config.title + ": " + e.getMessage(), e);
        }
    }

    public CrudConfig crudConfig(String kind) {
        Config config = config(kind);
        return new CrudConfig(
                config.title,
                config.idKey,
                config.codeKey,
                config.nameKey,
                config.extraKey,
                config.extraLabel);
    }

    private Config config(String kind) {
        String key = kind == null ? "SALESMAN" : kind.toUpperCase();
        return switch (key) {
            case "AREA" -> new Config(
                    "Area",
                    "area_id",
                    "area_code",
                    "area_name",
                    "",
                    "",
                    "SELECT area_id, area_code, area_name, '' AS notes, is_active FROM areas ORDER BY area_code",
                    "SELECT area_id, area_code, area_name, '' AS notes, is_active FROM areas WHERE area_id = ?",
                    "INSERT INTO areas(area_code, area_name, is_active) VALUES (?, ?, ?)",
                    "UPDATE areas SET area_code = ?, area_name = ?, is_active = ? WHERE area_id = ?",
                    "DELETE FROM areas WHERE area_id = ?");
            case "BANK" -> new Config(
                    "Bank",
                    "bank_id",
                    "bank_code",
                    "bank_name",
                    "account_no",
                    "Account No",
                    "SELECT bank_id, bank_code, bank_name, COALESCE(account_no, ''), is_active FROM banks ORDER BY bank_code",
                    "SELECT bank_id, bank_code, bank_name, COALESCE(account_no, ''), is_active FROM banks WHERE bank_id = ?",
                    "INSERT INTO banks(bank_code, bank_name, account_no, is_active) VALUES (?, ?, ?, ?)",
                    "UPDATE banks SET bank_code = ?, bank_name = ?, account_no = ?, is_active = ? WHERE bank_id = ?",
                    "DELETE FROM banks WHERE bank_id = ?");
            default -> new Config(
                    "Salesman",
                    "salesman_id",
                    "salesman_code",
                    "salesman_name",
                    "phone",
                    "Phone",
                    "SELECT salesman_id, salesman_code, salesman_name, COALESCE(phone, ''), is_active FROM salesmen ORDER BY salesman_code",
                    "SELECT salesman_id, salesman_code, salesman_name, COALESCE(phone, ''), is_active FROM salesmen WHERE salesman_id = ?",
                    "INSERT INTO salesmen(salesman_code, salesman_name, phone, is_active) VALUES (?, ?, ?, ?)",
                    "UPDATE salesmen SET salesman_code = ?, salesman_name = ?, phone = ?, is_active = ? WHERE salesman_id = ?",
                    "DELETE FROM salesmen WHERE salesman_id = ?");
        };
    }

    private boolean blank(Object value) {
        return value == null || String.valueOf(value).trim().isBlank();
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private boolean parseBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = value == null ? "" : String.valueOf(value);
        return "1".equals(text) || "YES".equalsIgnoreCase(text) || "TRUE".equalsIgnoreCase(text);
    }

    public record CrudConfig(String title, String idKey, String codeKey, String nameKey, String extraKey, String extraLabel) {
    }

    private record Config(
            String title,
            String idKey,
            String codeKey,
            String nameKey,
            String extraKey,
            String extraLabel,
            String browseSql,
            String findSql,
            String insertSql,
            String updateSql,
            String deleteSql) {
    }
}
