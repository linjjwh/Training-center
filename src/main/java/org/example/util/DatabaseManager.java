package org.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {

    private static final String CONFIG_FILE = "/database.properties";

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties properties = new Properties();

        try (InputStream input =
                     DatabaseManager.class
                             .getResourceAsStream(CONFIG_FILE)) {

            if (input == null) {
                throw new IllegalStateException(
                        "Не найден файл " + CONFIG_FILE +
                                ". Проверь src/main/resources/database.properties"
                );
            }

            properties.load(input);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Не удалось прочитать " + CONFIG_FILE, e
            );
        }

        URL = properties.getProperty("db.url");
        USER = properties.getProperty("db.user");
        PASSWORD = properties.getProperty("db.password");

        if (URL == null || URL.isBlank() ||
                USER == null || USER.isBlank() ||
                PASSWORD == null) {

            throw new IllegalStateException(
                    "В " + CONFIG_FILE +
                            " должны быть заполнены db.url, db.user и db.password"
            );
        }
    }

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String getUrl() {
        return URL;
    }
}
