package com.hei.TDOASFinal.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL      = System.getenv("DB_URL");
    private static final String USERNAME = System.getenv("DB_USERNAME");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        if (URL == null || USERNAME == null || PASSWORD == null) {
            throw new IllegalStateException(
                    "Missing env variables: DB_URL, DB_USERNAME, DB_PASSWORD"
            );
        }
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}