package ccrrs.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages JDBC connection to MySQL Database.
 * If database is not configured or offline, CCRRS continues smoothly
 * in high-performance in-memory data structure mode.
 */
public class DBConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/ccrrs_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "MyNewPass@2026";

    private static boolean isAvailable = false;
    private static boolean checked = false;

    public static synchronized Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DEFAULT_URL, DEFAULT_USER, DEFAULT_PASSWORD);
            isAvailable = true;
            return conn;
        } catch (ClassNotFoundException e) {
            if (!checked) {
                System.out.println("  [DB Note] MySQL JDBC Driver not found in classpath. Running in in-memory mode.");
                checked = true;
            }
            isAvailable = false;
            return null;
        } catch (SQLException e) {
            if (!checked) {
                System.out.println("  [DB Note] MySQL Server not reachable (" + e.getMessage() + "). Running in in-memory mode.");
                checked = true;
            }
            isAvailable = false;
            return null;
        }
    }

    public static boolean isDatabaseAvailable() {
        if (!checked) {
            try (Connection c = getConnection()) {
                isAvailable = (c != null && !c.isClosed());
            } catch (Exception e) {
                isAvailable = false;
            }
            checked = true;
        }
        return isAvailable;
    }
}
