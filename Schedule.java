package model;

public class Schedule {
    private final int id;
    private final int doctorId;
    private final String workDate;
    private final String timeSlot;
    private final boolean available;

    public Schedule(int id, int doctorId, String workDate, String timeSlot, boolean available) {
        this.id = id;
        this.doctorId = doctorId;
        this.workDate = workDate;
        this.timeSlot = timeSlot;
        this.available = available;
    }

    public int getId() { return id; }
    public int getDoctorId() { return doctorId; }
    public String getWorkDate() { return workDate; }
    public String getTimeSlot() { return timeSlot; }
    public boolean isAvailable() { return available; }

    public Schedule withAvailability(boolean newAvailability) {
        return new Schedule(id, doctorId, workDate, timeSlot, newAvailability);
    }

    public Schedule updatedTo(int newDoctorId, String newWorkDate, String newTimeSlot, boolean newAvailability) {
        return new Schedule(id, newDoctorId, newWorkDate, newTimeSlot, newAvailability);
    }

    @Override
    public String toString() {
        String status = available ? "Available" : "Booked";
        return id + " - " + workDate + " " + timeSlot + " (" + status + ")";
    }
}
