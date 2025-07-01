package aiatest.function;

import java.sql.*;

public class SqlServerConnector {

    public static void main(String[] args) {
        Connection con = null;

        try {
            // Load SQL Server JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            
            // String url = "jdbc:sqlserver://nilufreesqldbserver.database.windows.net:1433;"
            //            + "database=myFreeDB;"
            //            + "user=nilrandha@nilufreesqldbserver;"
            //            + "password=testaia@123;" 
            //            + "encrypt=true;"
            //            + "trustServerCertificate=false;"
            //            + "hostNameInCertificate=*.database.windows.net;"
            //            + "loginTimeout=30;";

            String url = System.getenv("DB_SETTEINGS");
             con = DriverManager.getConnection(url);
            // Establish connection
            con = DriverManager.getConnection(url);
            System.out.println("Connection established.");

            // Example query execution
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT first_name FROM users ");

            while (rs.next()) {
                System.out.println("Row: " + rs.getString(1));
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (con != null) con.close(); } catch (Exception e) {}
        }
    }
}