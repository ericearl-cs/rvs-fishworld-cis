package com.rvsfishworld.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    public static Connection getConnection() throws SQLException {
        DatabaseConfig config = DatabaseConfig.getInstance();
        return DriverManager.getConnection(
                config.getUrl(),
                config.getUsername(),
                config.getPassword());
    }
}
