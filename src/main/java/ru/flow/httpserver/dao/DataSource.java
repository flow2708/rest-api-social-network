package ru.flow.httpserver.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {
    private static final HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(System.getenv("JDBC_URL"));
            config.setUsername(System.getenv("DB_USER"));
            config.setPassword(System.getenv("DB_PASSWORD"));
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.addDataSourceProperty("sslMode", "REQUIRED");
            config.addDataSourceProperty("serverTimezone", "UTC");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");

            dataSource = new HikariDataSource(config);
            testConnection();
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Не удалось инициализировать пул: " + e.getMessage());
        }
    }
    private static void testConnection() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(2)) {
                throw new SQLException("Connection test failed");
            }
        }
    }
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
