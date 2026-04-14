package com.rvsfishworld.dao;

import com.rvsfishworld.db.Database;
import com.rvsfishworld.db.PasswordUtil;
import com.rvsfishworld.model.UserAccount;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AppUserDAO {
    public List<String> listActiveUsernames() {
        String sql = "SELECT username FROM app_users WHERE is_active = TRUE ORDER BY username";
        List<String> names = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                names.add(rs.getString(1));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load users: " + e.getMessage(), e);
        }
        return names;
    }

    public UserAccount validateLogin(String username, String password) {
        String sql = """
                SELECT user_id, username, display_name, password_hash, must_reset_password,
                       rights_csv, raw_flags_csv, photo_hint, is_active, last_login_at
                FROM app_users
                WHERE UPPER(username) = UPPER(?)
                LIMIT 1
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                if (!rs.getBoolean("is_active")) {
                    return null;
                }
                String storedUsername = rs.getString("username");
                String storedHash = rs.getString("password_hash");
                if (!PasswordUtil.matches(password, storedHash, storedUsername)) {
                    return null;
                }
                UserAccount user = mapRow(rs);
                touchLastLogin(conn, user.getUserId());
                return user;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate login: " + e.getMessage(), e);
        }
    }

    public List<UserAccount> listUsers() {
        String sql = """
                SELECT user_id, username, display_name, password_hash, must_reset_password,
                       rights_csv, raw_flags_csv, photo_hint, is_active, last_login_at
                FROM app_users
                ORDER BY username
                """;
        List<UserAccount> users = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load users: " + e.getMessage(), e);
        }
    }

    public UserAccount findByUsername(String username) {
        String sql = """
                SELECT user_id, username, display_name, password_hash, must_reset_password,
                       rights_csv, raw_flags_csv, photo_hint, is_active, last_login_at
                FROM app_users
                WHERE UPPER(username) = UPPER(?)
                LIMIT 1
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load user: " + e.getMessage(), e);
        }
    }

    public void save(UserAccount user, String plainPassword) {
        boolean insert = user.getUserId() <= 0;
        String insertSql = """
                INSERT INTO app_users (
                    username, display_name, password_hash, must_reset_password,
                    rights_csv, raw_flags_csv, photo_hint, is_active
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        String updateSql = """
                UPDATE app_users
                SET username=?, display_name=?, password_hash=?, must_reset_password=?,
                    rights_csv=?, raw_flags_csv=?, photo_hint=?, is_active=?, updated_at=CURRENT_TIMESTAMP
                WHERE user_id=?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(insert ? insertSql : updateSql, Statement.RETURN_GENERATED_KEYS)) {
            int index = 1;
            ps.setString(index++, user.getUsername());
            ps.setString(index++, user.getDisplayName());
            ps.setString(index++, resolvePasswordHash(user, plainPassword));
            ps.setBoolean(index++, user.isMustResetPassword());
            ps.setString(index++, user.getRightsCsv());
            ps.setString(index++, user.getRawFlagsCsv());
            ps.setString(index++, user.getPhotoHint());
            ps.setBoolean(index++, user.isActive());
            if (!insert) {
                ps.setLong(index, user.getUserId());
            }
            ps.executeUpdate();
            if (insert) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getLong(1));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        }
    }

    private void touchLastLogin(Connection conn, long userId) {
        String sql = "UPDATE app_users SET last_login_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    private UserAccount mapRow(ResultSet rs) throws Exception {
        UserAccount user = new UserAccount();
        user.setUserId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setDisplayName(rs.getString("display_name"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setMustResetPassword(rs.getBoolean("must_reset_password"));
        user.setRightsCsv(rs.getString("rights_csv"));
        user.setRawFlagsCsv(rs.getString("raw_flags_csv"));
        user.setPhotoHint(rs.getString("photo_hint"));
        user.setActive(rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("last_login_at");
        user.setLastLoginAt(ts == null ? null : ts.toLocalDateTime());
        return user;
    }

    private String resolvePasswordHash(UserAccount user, String plainPassword) {
        String password = plainPassword == null ? "" : plainPassword.trim();
        if (!password.isEmpty()) {
            return PasswordUtil.sha256(password);
        }
        if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) {
            return user.getPasswordHash();
        }
        String username = user.getUsername() == null ? "" : user.getUsername().trim();
        return username.isEmpty() ? "" : PasswordUtil.sha256(username);
    }
}
