package com.rvsfishworld.dao;

import com.rvsfishworld.db.Database;
import java.util.ArrayList;
import java.util.List;

public class SpecialOfWeekDAO {
    public record Row(long id, String productCode, String startDate, String endDate, String remarks, boolean active) {
    }

    public List<Row> listCurrent() {
        String sql = """
                SELECT special_week_id, product_code,
                       COALESCE(DATE_FORMAT(start_date, '%m/%d/%Y'), ''),
                       COALESCE(DATE_FORMAT(end_date, '%m/%d/%Y'), ''),
                       COALESCE(remarks, ''),
                       is_active
                FROM special_of_week
                ORDER BY start_date DESC, product_code
                """;
        List<Row> rows = new ArrayList<>();
        try (var conn = Database.getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Row(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getBoolean(6)));
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load Special Of Week: " + e.getMessage(), e);
        }
    }
}
