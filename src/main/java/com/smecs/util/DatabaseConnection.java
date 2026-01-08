package com.smecs.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/smecs";
    private static final String USER = "postgres"; // Placeholder: Modify as per environment
    private static final String PASSWORD = "password"; // Placeholder: Modify as per environment

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
