package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Schedule;

public class ScheduleDAO {
    public ScheduleDAO() {
        SchemaInitializer.initialize();
    }

    public Schedule addSchedule(int doctorId, String workDate, String timeSlot, boolean available) {
        int scheduleId = ClinicMemoryStore.nextScheduleId();
        Schedule schedule = new Schedule(scheduleId, doctorId, workDate, timeSlot, available);
        Connection con = DBConnection.getConnection();
        if (con == null) {
            return ClinicMemoryStore.saveSchedule(schedule);
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement("INSERT INTO schedule VALUES (?, ?, ?, ?, ?)")) {
            ps.setInt(1, schedule.getId());
            ps.setInt(2, schedule.getDoctorId());
            ps.setString(3, schedule.getWorkDate());
            ps.setString(4, schedule.getTimeSlot());
            ps.setBoolean(5, schedule.isAvailable());
            ps.executeUpdate();
            return schedule;
        } catch (Exception e) {
            return ClinicMemoryStore.saveSchedule(schedule);
        }
    }

    public boolean updateSchedule(Schedule schedule) {
        Connection con = DBConnection.getConnection();
        if (con == null) {
            ClinicMemoryStore.saveSchedule(schedule);
            return true;
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement(
                 "UPDATE schedule SET doctor_id=?, work_date=?, time_slot=?, available=? WHERE id=?"
             )) {
            ps.setInt(1, schedule.getDoctorId());
            ps.setString(2, schedule.getWorkDate());
            ps.setString(3, schedule.getTimeSlot());
            ps.setBoolean(4, schedule.isAvailable());
            ps.setInt(5, schedule.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            ClinicMemoryStore.saveSchedule(schedule);
            return true;
        }
    }

    public boolean deleteSchedule(int scheduleId) {
        Connection con = DBConnection.getConnection();
        if (con == null) {
            return ClinicMemoryStore.deleteSchedule(scheduleId);
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement("DELETE FROM schedule WHERE id=?")) {
            ps.setInt(1, scheduleId);
            boolean deleted = ps.executeUpdate() > 0;
            if (!deleted) {
                return ClinicMemoryStore.deleteSchedule(scheduleId);
            }
            return true;
        } catch (Exception e) {
            return ClinicMemoryStore.deleteSchedule(scheduleId);
        }
    }

    public Schedule findScheduleById(int scheduleId) {
        Connection con = DBConnection.getConnection();
        if (con == null) {
            return ClinicMemoryStore.findScheduleById(scheduleId);
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM schedule WHERE id=?")) {
            ps.setInt(1, scheduleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSchedule(rs);
                }
            }
        } catch (Exception e) {
            return ClinicMemoryStore.findScheduleById(scheduleId);
        }

        return null;
    }

    public List<Schedule> getAllSchedules() {
        Connection con = DBConnection.getConnection();
        if (con == null) {
            return ClinicMemoryStore.getSchedules();
        }

        try (Connection connection = con;
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM schedule ORDER BY work_date, time_slot");
             ResultSet rs = ps.executeQuery()) {
            List<Schedule> schedules = new ArrayList<>();
            while (rs.next()) {
                schedules.add(mapSchedule(rs));
            }
            return schedules;
        } catch (Exception e) {
            return ClinicMemoryStore.getSchedules();
        }
    }

    public List<Schedule> getAvailableSchedules() {
        List<Schedule> availableSchedules = new ArrayList<>();
        for (Schedule schedule : getAllSchedules()) {
            if (schedule.isAvailable()) {
                availableSchedules.add(schedule);
            }
        }
        return availableSchedules;
    }

    private Schedule mapSchedule(ResultSet rs) throws Exception {
        return new Schedule(
            rs.getInt("id"),
            rs.getInt("doctor_id"),
            rs.getString("work_date"),
            rs.getString("time_slot"),
            rs.getBoolean("available")
        );
    }
}
