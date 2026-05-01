package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import model.Patient;

public class PatientDAO {
    public PatientDAO() {
        SchemaInitializer.initialize();
    }

    public Patient addPatient(String name, int age, String phone) {
        int patientId = ClinicMemoryStore.nextPatientId();
        Patient patient = new Patient(patientId, name, age, phone);
        Connection con = DBConnection.getConnection();
        if (con == null) {
            return ClinicMemoryStore.savePatient(patient);
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement("INSERT INTO patient VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, patient.getId());
            ps.setString(2, patient.getName());
            ps.setInt(3, patient.getAge());
            ps.setString(4, patient.getPhone());
            ps.executeUpdate();
            return patient;
        } catch (Exception e) {
            return ClinicMemoryStore.savePatient(patient);
        }
    }

    public boolean updatePatient(Patient patient) {
        Connection con = DBConnection.getConnection();
        if (con == null) {
            ClinicMemoryStore.savePatient(patient);
            return true;
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement(
                 "UPDATE patient SET name=?, age=?, phone=? WHERE id=?"
             )) {
            ps.setString(1, patient.getName());
            ps.setInt(2, patient.getAge());
            ps.setString(3, patient.getPhone());
            ps.setInt(4, patient.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            ClinicMemoryStore.savePatient(patient);
            return true;
        }
    }

    public boolean deletePatient(int patientId) {
        Connection con = DBConnection.getConnection();
        if (con == null) {
            return ClinicMemoryStore.deletePatient(patientId);
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement("DELETE FROM patient WHERE id=?")) {
            ps.setInt(1, patientId);
            boolean deleted = ps.executeUpdate() > 0;
            if (!deleted) {
                return ClinicMemoryStore.deletePatient(patientId);
            }
            return true;
        } catch (Exception e) {
            return ClinicMemoryStore.deletePatient(patientId);
        }
    }

    public Patient findPatientById(int patientId) {
        Connection con = DBConnection.getConnection();
        if (con == null) {
            return ClinicMemoryStore.findPatientById(patientId);
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM patient WHERE id=?")) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPatient(rs);
                }
            }
        } catch (Exception e) {
            return ClinicMemoryStore.findPatientById(patientId);
        }

        return null;
    }

    public List<Patient> getAllPatients() {
        Connection con = DBConnection.getConnection();
        if (con == null) {
            return ClinicMemoryStore.getPatients();
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM patient ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            List<Patient> patients = new java.util.ArrayList<>();
            while (rs.next()) {
                patients.add(mapPatient(rs));
            }
            return patients;
        } catch (Exception e) {
            return ClinicMemoryStore.getPatients();
        }
    }

    private Patient mapPatient(ResultSet rs) throws Exception {
        return new Patient(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getInt("age"),
            rs.getString("phone")
        );
    }
}
