package dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/clinic",
                "root",
                "root"
            );
        } catch (Exception e) {
            return null;
        }
    }
}
