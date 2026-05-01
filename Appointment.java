package model;

public class Appointment {
    private final int id;
    private final int doctorId;
    private final int patientId;
    private final int scheduleId;
    private final String date;
    private final String time;
    private final String status;

    public Appointment(int id, int doctorId, int patientId, String date, String time) {
        this(id, doctorId, patientId, 0, date, time, "BOOKED");
    }

    public Appointment(int id, int doctorId, int patientId, int scheduleId, String date, String time, String status) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.scheduleId = scheduleId;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public int getId() { return id; }
    public int getDoctorId() { return doctorId; }
    public int getPatientId() { return patientId; }
    public int getScheduleId() { return scheduleId; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }

    public Appointment withStatus(String newStatus) {
        return new Appointment(id, doctorId, patientId, scheduleId, date, time, newStatus);
    }

    public Appointment rescheduledTo(int newDoctorId, int newScheduleId, String newDate, String newTime) {
        return new Appointment(id, newDoctorId, patientId, newScheduleId, newDate, newTime, "BOOKED");
    }
}
