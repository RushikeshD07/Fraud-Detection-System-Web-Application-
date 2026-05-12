package com.frauddetection.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // ── Database credentials – change these to match your MySQL setup ──
    private static final String URL      = "jdbc:mysql://localhost:3306/fraud_detection_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private static final String USER     = "fraud_app";       // your MySQL username
    private static final String PASSWORD = "fraud123";       // your MySQL password

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DB] Connected to MySQL successfully.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DB ERROR] MySQL JDBC Driver not found. Add mysql-connector-java to classpath.");
            throw new SQLException("Driver not found", e);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("[DB] Connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("[DB ERROR] Failed to close connection: " + e.getMessage());
            }
        }
    }
}
