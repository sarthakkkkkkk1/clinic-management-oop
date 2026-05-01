package service;

import dao.AppointmentDAO;
import dao.DoctorDAO;
import dao.PatientDAO;
import dao.ScheduleDAO;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import model.Appointment;
import model.Doctor;
import model.Patient;
import model.Schedule;

public class ClinicService {
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    public void initializeSampleData() {
        if (getAllDoctors().isEmpty()) {
            addDoctor("Dr. Sarah Ahmed", "General Medicine", "0900000001");
            addDoctor("Dr. Michael Lee", "Cardiology", "0900000002");
        }
        if (getAllPatients().isEmpty()) {
            addPatient("Amina Yusuf", 26, "0911111111");
            addPatient("Daniel James", 34, "0922222222");
        }
        if (getAllSchedules().isEmpty() && !getAllDoctors().isEmpty()) {
            List<Doctor> doctors = getAllDoctors();
            addSchedule(doctors.get(0).getId(), "2026-05-01", "09:00");
            addSchedule(doctors.get(0).getId(), "2026-05-01", "10:00");
            addSchedule(doctors.get(1).getId(), "2026-05-01", "11:00");
        }
    }

    public Doctor addDoctor(String name, String specialization, String phone) {
        validateText(name, "Doctor name");
        validateText(specialization, "Specialization");
        validateText(phone, "Phone");
        return doctorDAO.addDoctor(name.trim(), specialization.trim(), phone.trim());
    }

    public boolean updateDoctor(int id, String name, String specialization, String phone) {
        validateText(name, "Doctor name");
        validateText(specialization, "Specialization");
        validateText(phone, "Phone");
        return doctorDAO.updateDoctor(new Doctor(id, name.trim(), specialization.trim(), phone.trim()));
    }

    public boolean deleteDoctor(int doctorId) {
        for (Schedule schedule : getAllSchedules()) {
            if (schedule.getDoctorId() == doctorId) {
                throw new IllegalArgumentException("Delete the doctor's schedules first.");
            }
        }
        for (Appointment appointment : getAllAppointments()) {
            if (appointment.getDoctorId() == doctorId && !"CANCELLED".equalsIgnoreCase(appointment.getStatus())) {
                throw new IllegalArgumentException("Doctor has active appointments.");
            }
        }
        return doctorDAO.deleteDoctor(doctorId);
    }

    public Patient addPatient(String name, int age, String phone) {
        validateText(name, "Patient name");
        validatePositive(age, "Age");
        validateText(phone, "Phone");
        return patientDAO.addPatient(name.trim(), age, phone.trim());
    }

    public boolean updatePatient(int id, String name, int age, String phone) {
        validateText(name, "Patient name");
        validatePositive(age, "Age");
        validateText(phone, "Phone");
        return patientDAO.updatePatient(new Patient(id, name.trim(), age, phone.trim()));
    }

    public boolean deletePatient(int patientId) {
        for (Appointment appointment : getAllAppointments()) {
            if (appointment.getPatientId() == patientId && !"CANCELLED".equalsIgnoreCase(appointment.getStatus())) {
                throw new IllegalArgumentException("Patient has active appointments.");
            }
        }
        return patientDAO.deletePatient(patientId);
    }

    public Schedule addSchedule(int doctorId, String workDate, String timeSlot) {
        validateDate(workDate);
        validateTime(timeSlot);
        requireDoctor(doctorId);

        for (Schedule schedule : getAllSchedules()) {
            boolean duplicate = schedule.getDoctorId() == doctorId
                && schedule.getWorkDate().equals(workDate)
                && schedule.getTimeSlot().equals(timeSlot);
            if (duplicate) {
                throw new IllegalArgumentException("This doctor already has that slot.");
            }
        }

        return scheduleDAO.addSchedule(doctorId, workDate.trim(), timeSlot.trim(), true);
    }

    public boolean updateSchedule(int scheduleId, int doctorId, String workDate, String timeSlot, boolean available) {
        validateDate(workDate);
        validateTime(timeSlot);
        requireDoctor(doctorId);

        for (Schedule schedule : getAllSchedules()) {
            boolean duplicate = schedule.getId() != scheduleId
                && schedule.getDoctorId() == doctorId
                && schedule.getWorkDate().equals(workDate)
                && schedule.getTimeSlot().equals(timeSlot);
            if (duplicate) {
                throw new IllegalArgumentException("Another identical schedule already exists.");
            }
        }

        return scheduleDAO.updateSchedule(new Schedule(scheduleId, doctorId, workDate.trim(), timeSlot.trim(), available));
    }

    public boolean deleteSchedule(int scheduleId) {
        for (Appointment appointment : getAllAppointments()) {
            if (appointment.getScheduleId() == scheduleId && !"CANCELLED".equalsIgnoreCase(appointment.getStatus())) {
                throw new IllegalArgumentException("Schedule has an active appointment.");
            }
        }
        return scheduleDAO.deleteSchedule(scheduleId);
    }

    public Appointment bookAppointment(int doctorId, int patientId, int scheduleId) {
        Doctor doctor = requireDoctor(doctorId);
        Patient patient = requirePatient(patientId);
        Schedule schedule = requireSchedule(scheduleId);

        if (schedule.getDoctorId() != doctor.getId()) {
            throw new IllegalArgumentException("Selected schedule does not belong to that doctor.");
        }
        if (!schedule.isAvailable()) {
            throw new IllegalArgumentException("Selected schedule is not available.");
        }
        if (!appointmentDAO.isSlotAvailable(schedule.getDoctorId(), schedule.getWorkDate(), schedule.getTimeSlot())) {
            throw new IllegalArgumentException("This slot is already booked.");
        }

        Appointment appointment = new Appointment(
            0,
            doctor.getId(),
            patient.getId(),
            schedule.getId(),
            schedule.getWorkDate(),
            schedule.getTimeSlot(),
            "BOOKED"
        );
        Appointment saved = appointmentDAO.saveAppointment(appointment);
        scheduleDAO.updateSchedule(schedule.withAvailability(false));
        return saved;
    }

    public boolean cancelAppointment(int appointmentId) {
        Appointment appointment = requireAppointment(appointmentId);
        if ("CANCELLED".equalsIgnoreCase(appointment.getStatus())) {
            return true;
        }

        boolean updated = appointmentDAO.updateAppointment(appointment.withStatus("CANCELLED"));
        Schedule schedule = scheduleDAO.findScheduleById(appointment.getScheduleId());
        if (schedule != null) {
            scheduleDAO.updateSchedule(schedule.withAvailability(true));
        }
        return updated;
    }

    public Appointment rescheduleAppointment(int appointmentId, int newScheduleId) {
        Appointment appointment = requireAppointment(appointmentId);
        Schedule newSchedule = requireSchedule(newScheduleId);

        if (!newSchedule.isAvailable()) {
            throw new IllegalArgumentException("New schedule is not available.");
        }
        if (!appointmentDAO.isSlotAvailable(newSchedule.getDoctorId(), newSchedule.getWorkDate(), newSchedule.getTimeSlot())) {
            throw new IllegalArgumentException("New schedule already has a booking.");
        }

        Schedule oldSchedule = scheduleDAO.findScheduleById(appointment.getScheduleId());
        Appointment updatedAppointment = appointment.rescheduledTo(
            newSchedule.getDoctorId(),
            newSchedule.getId(),
            newSchedule.getWorkDate(),
            newSchedule.getTimeSlot()
        );

        appointmentDAO.updateAppointment(updatedAppointment);
        scheduleDAO.updateSchedule(newSchedule.withAvailability(false));
        if (oldSchedule != null) {
            scheduleDAO.updateSchedule(oldSchedule.withAvailability(true));
        }
        return updatedAppointment;
    }

    public List<Doctor> getAllDoctors() {
        return doctorDAO.getAllDoctors();
    }

    public List<Patient> getAllPatients() {
        return patientDAO.getAllPatients();
    }

    public List<Schedule> getAllSchedules() {
        return scheduleDAO.getAllSchedules();
    }

    public List<Schedule> getAvailableSchedules() {
        return scheduleDAO.getAvailableSchedules();
    }

    public List<Schedule> getAvailableSchedulesByDoctor(int doctorId) {
        List<Schedule> schedules = new ArrayList<>();
        for (Schedule schedule : getAvailableSchedules()) {
            if (schedule.getDoctorId() == doctorId) {
                schedules.add(schedule);
            }
        }
        return schedules;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentDAO.getAllAppointments();
    }

    public Doctor getDoctorById(int doctorId) {
        return doctorDAO.findDoctorById(doctorId);
    }

    public Patient getPatientById(int patientId) {
        return patientDAO.findPatientById(patientId);
    }

    public Schedule getScheduleById(int scheduleId) {
        return scheduleDAO.findScheduleById(scheduleId);
    }

    public int getDoctorCount() {
        return getAllDoctors().size();
    }

    public int getPatientCount() {
        return getAllPatients().size();
    }

    public int getAvailableScheduleCount() {
        return getAvailableSchedules().size();
    }

    public int getAppointmentCount() {
        return getAllAppointments().size();
    }

    private Doctor requireDoctor(int doctorId) {
        Doctor doctor = doctorDAO.findDoctorById(doctorId);
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor not found.");
        }
        return doctor;
    }

    private Patient requirePatient(int patientId) {
        Patient patient = patientDAO.findPatientById(patientId);
        if (patient == null) {
            throw new IllegalArgumentException("Patient not found.");
        }
        return patient;
    }

    private Schedule requireSchedule(int scheduleId) {
        Schedule schedule = scheduleDAO.findScheduleById(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule not found.");
        }
        return schedule;
    }

    private Appointment requireAppointment(int appointmentId) {
        Appointment appointment = appointmentDAO.findAppointmentById(appointmentId);
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment not found.");
        }
        return appointment;
    }

    private void validateText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    private void validatePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
    }

    private void validateDate(String date) {
        validateText(date, "Date");
        try {
            LocalDate.parse(date.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Date must use yyyy-mm-dd format.");
        }
    }

    private void validateTime(String time) {
        validateText(time, "Time");
        try {
            LocalTime.parse(time.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Time must use HH:mm format.");
        }
    }
}
