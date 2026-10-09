package vn.edu.ictu.qlkh.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException(
                        "Không tìm thấy file db.properties."
                );
            }

            PROPERTIES.load(input);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Không thể đọc cấu hình database.",
                    e
            );
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "Không tìm thấy MySQL JDBC Driver.",
                    e
            );
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {

        String url = System.getProperty(
                "db.url",
                PROPERTIES.getProperty("db.url")
        );

        String username = System.getProperty(
                "db.username",
                PROPERTIES.getProperty("db.username")
        );

        String password = System.getProperty(
                "db.password",
                PROPERTIES.getProperty("db.password")
        );

        if (url == null || url.isBlank()
                || username == null || username.isBlank()
                || password == null) {
            throw new SQLException(
                    "Thiếu cấu hình kết nối database."
            );
        }

        return DriverManager.getConnection(
                url,
                username,
                password
        );
    }
}