package com.mycompany.mathgame.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseConnection {
    private static Connection connection;

    public static Connection getConnection() throws Exception {
        if (connection == null || connection.isClosed()) {
            Properties props = new Properties();
            try (InputStream in = DatabaseConnection.class
                    .getClassLoader()
                    .getResourceAsStream("config.properties")) {
                if (in == null) throw new IllegalStateException("config.properties not found");
                props.load(in);
            }
            String url  = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String pass = props.getProperty("db.password");
            connection = DriverManager.getConnection(url, user, pass);
            System.out.println("✅ SQL Server connected");
        }
        return connection;
    }
}
