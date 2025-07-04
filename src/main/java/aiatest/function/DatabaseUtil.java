package aiatest.function;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
     private static final String URL = "jdbc:mysql://localhost:3306/azuredb";
    private static final String USER = "root";
    private static final String PASSWORD = "Malee@2000";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
