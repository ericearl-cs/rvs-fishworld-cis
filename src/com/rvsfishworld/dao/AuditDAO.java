package com.rvsfishworld.dao;

import com.rvsfishworld.db.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AuditDAO {
    public void log(String actionType, String moduleName, String username, String details) {
        String sql = """
                INSERT INTO audit_events(username, action_type, module_name, details)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, actionType);
            ps.setString(3, moduleName);
            ps.setString(4, details);
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }
}
