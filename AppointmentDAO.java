package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import model.Appointment;

public class AppointmentDAO {
    public AppointmentDAO() {
        SchemaInitializer.initialize();
    }

    public boolean isSlotAvailable(int doctorId, String date, String time) {
        for (Appointment appointment : getAllAppointments()) {
            boolean matchesSlot = appointment.getDoctorId() == doctorId
                && appointment.getDate().equals(date)
                && appointment.getTime().equals(time);
            if (matchesSlot && !"CANCELLED".equalsIgnoreCase(appointment.getStatus())) {
                return false;
            }
        }
        return true;
    }

    public void bookAppointment(Appointment appt) {
        saveAppointment(appt);
    }

    public Appointment saveAppointment(Appointment appointment) {
        Appointment toSave = appointment;
        if (appointment.getId() <= 0) {
            toSave = new Appointment(
                ClinicMemoryStore.nextAppointmentId(),
                appointment.getDoctorId(),
                appointment.getPatientId(),
                appointment.getScheduleId(),
                appointment.getDate(),
                appointment.getTime(),
                appointment.getStatus()
            );
        }

        Connection con = DBConnection.getConnection();
        if (con == null) {
            return ClinicMemoryStore.saveAppointment(toSave);
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement("INSERT INTO appointment VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, toSave.getId());
            ps.setInt(2, toSave.getDoctorId());
            ps.setInt(3, toSave.getPatientId());
            ps.setInt(4, toSave.getScheduleId());
            ps.setString(5, toSave.getDate());
            ps.setString(6, toSave.getTime());
            ps.setString(7, toSave.getStatus());
            ps.executeUpdate();
            return toSave;
        } catch (Exception e) {
            return ClinicMemoryStore.saveAppointment(toSave);
        }
    }

    public boolean updateAppointment(Appointment appointment) {
        Connection con = DBConnection.getConnection();
        if (con == null) {
            ClinicMemoryStore.saveAppointment(appointment);
            return true;
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement(
                 "UPDATE appointment SET doctor_id=?, patient_id=?, schedule_id=?, appointment_date=?, appointment_time=?, status=? WHERE id=?"
             )) {
            ps.setInt(1, appointment.getDoctorId());
            ps.setInt(2, appointment.getPatientId());
            ps.setInt(3, appointment.getScheduleId());
            ps.setString(4, appointment.getDate());
            ps.setString(5, appointment.getTime());
            ps.setString(6, appointment.getStatus());
            ps.setInt(7, appointment.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            ClinicMemoryStore.saveAppointment(appointment);
            return true;
        }
    }

    public Appointment findAppointmentById(int appointmentId) {
        Connection con = DBConnection.getConnection();
        if (con == null) {
            return ClinicMemoryStore.findAppointmentById(appointmentId);
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM appointment WHERE id=?")) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAppointment(rs);
                }
            }
        } catch (Exception e) {
            return ClinicMemoryStore.findAppointmentById(appointmentId);
        }

        return null;
    }

    public List<Appointment> getAllAppointments() {
        Connection con = DBConnection.getConnection();
        if (con == null) {
            return ClinicMemoryStore.getAppointments();
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement(
                 "SELECT * FROM appointment ORDER BY appointment_date, appointment_time"
             );
             ResultSet rs = ps.executeQuery()) {
            List<Appointment> appointments = new java.util.ArrayList<>();
            while (rs.next()) {
                appointments.add(mapAppointment(rs));
            }
            return appointments;
        } catch (Exception e) {
            return ClinicMemoryStore.getAppointments();
        }
    }

    private Appointment mapAppointment(ResultSet rs) throws Exception {
        return new Appointment(
            rs.getInt("id"),
            rs.getInt("doctor_id"),
            rs.getInt("patient_id"),
            rs.getInt("schedule_id"),
            rs.getString("appointment_date"),
            rs.getString("appointment_time"),
            rs.getString("status")
        );
}
}
