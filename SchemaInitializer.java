package dao;

import java.sql.Connection;
import java.sql.Statement;

public final class SchemaInitializer {
    private static boolean initialized;

    private SchemaInitializer() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        Connection connection = DBConnection.getConnection();
        if (connection == null) {
            initialized = true;
            return;
        }

        try (Connection con = connection; Statement stmt = con.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS doctor ("
                    + "id INT PRIMARY KEY, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "specialization VARCHAR(100) NOT NULL, "
                    + "phone VARCHAR(30) NOT NULL)"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS patient ("
                    + "id INT PRIMARY KEY, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "age INT NOT NULL, "
                    + "phone VARCHAR(30) NOT NULL)"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS schedule ("
                    + "id INT PRIMARY KEY, "
                    + "doctor_id INT NOT NULL, "
                    + "work_date VARCHAR(20) NOT NULL, "
                    + "time_slot VARCHAR(20) NOT NULL, "
                    + "available BOOLEAN NOT NULL)"
            );
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS appointment ("
                    + "id INT PRIMARY KEY, "
                    + "doctor_id INT NOT NULL, "
                    + "patient_id INT NOT NULL, "
                    + "schedule_id INT NOT NULL, "
                    + "appointment_date VARCHAR(20) NOT NULL, "
                    + "appointment_time VARCHAR(20) NOT NULL, "
                    + "status VARCHAR(20) NOT NULL)"
            );
        } catch (Exception e) {
            // Ignore schema setup errors and let DAO calls fall back to memory.
        }

        initialized = true;
    }
}
