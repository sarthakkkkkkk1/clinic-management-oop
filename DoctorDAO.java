package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import model.Doctor;

public class DoctorDAO {
    public DoctorDAO() {
        SchemaInitializer.initialize();
    }

    public Doctor addDoctor(String name, String specialization, String phone) {
        int doctorId = ClinicMemoryStore.nextDoctorId();
        Doctor doctor = new Doctor(doctorId, name, specialization, phone);
        Connection con = DBConnection.getConnection();
        if (con == null) {
            return ClinicMemoryStore.saveDoctor(doctor);
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement("INSERT INTO doctor VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, doctor.getId());
            ps.setString(2, doctor.getName());
            ps.setString(3, doctor.getSpecialization());
            ps.setString(4, doctor.getPhone());
            ps.executeUpdate();
            return doctor;
        } catch (Exception e) {
            return ClinicMemoryStore.saveDoctor(doctor);
        }
    }

    public boolean updateDoctor(Doctor doctor) {
        Connection con = DBConnection.getConnection();
        if (con == null) {
            ClinicMemoryStore.saveDoctor(doctor);
            return true;
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement(
                 "UPDATE doctor SET name=?, specialization=?, phone=? WHERE id=?"
             )) {
            ps.setString(1, doctor.getName());
            ps.setString(2, doctor.getSpecialization());
            ps.setString(3, doctor.getPhone());
            ps.setInt(4, doctor.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            ClinicMemoryStore.saveDoctor(doctor);
            return true;
        }
    }

    public boolean deleteDoctor(int doctorId) {
        Connection con = DBConnection.getConnection();
        if (con == null) {
            return ClinicMemoryStore.deleteDoctor(doctorId);
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement("DELETE FROM doctor WHERE id=?")) {
            ps.setInt(1, doctorId);
            boolean deleted = ps.executeUpdate() > 0;
            if (!deleted) {
                return ClinicMemoryStore.deleteDoctor(doctorId);
            }
            return true;
        } catch (Exception e) {
            return ClinicMemoryStore.deleteDoctor(doctorId);
        }
    }

    public Doctor findDoctorById(int doctorId) {
        Connection con = DBConnection.getConnection();
        if (con == null) {
            return ClinicMemoryStore.findDoctorById(doctorId);
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM doctor WHERE id=?")) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDoctor(rs);
                }
            }
        } catch (Exception e) {
            return ClinicMemoryStore.findDoctorById(doctorId);
        }

        return null;
    }

    public List<Doctor> getAllDoctors() {
        Connection con = DBConnection.getConnection();
        if (con == null) {
            return ClinicMemoryStore.getDoctors();
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM doctor ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            List<Doctor> doctors = new java.util.ArrayList<>();
            while (rs.next()) {
                doctors.add(mapDoctor(rs));
            }
            return doctors;
        } catch (Exception e) {
            return ClinicMemoryStore.getDoctors();
        }
    }

    private Doctor mapDoctor(ResultSet rs) throws Exception {
        return new Doctor(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("specialization"),
            rs.getString("phone")
        );
    }
}
