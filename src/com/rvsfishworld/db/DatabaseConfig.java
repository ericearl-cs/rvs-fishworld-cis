package com.rvsfishworld.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConfig {
    private static final String CONFIG_FILE = "config.properties";
    private static DatabaseConfig instance;

    private final Properties properties = new Properties();

    private DatabaseConfig() {
        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read config.properties. Please create it in the project root.", e);
        }
    }

    public static DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public String getUrl() {
        return properties.getProperty("db.url");
    }

    public String getUsername() {
        return properties.getProperty("db.username");
    }

    public String getPassword() {
        return properties.getProperty("db.password");
    }
}
