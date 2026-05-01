package dao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.Appointment;
import model.Doctor;
import model.Patient;
import model.Schedule;

final class ClinicMemoryStore {
    private static final List<Doctor> DOCTORS = new ArrayList<>();
    private static final List<Patient> PATIENTS = new ArrayList<>();
    private static final List<Schedule> SCHEDULES = new ArrayList<>();
    private static final List<Appointment> APPOINTMENTS = new ArrayList<>();

    private static int nextDoctorId = 1;
    private static int nextPatientId = 1;
    private static int nextScheduleId = 1;
    private static int nextAppointmentId = 1;

    private ClinicMemoryStore() {
    }

    static synchronized int nextDoctorId() {
        return nextDoctorId++;
    }

    static synchronized int nextPatientId() {
        return nextPatientId++;
    }

    static synchronized int nextScheduleId() {
        return nextScheduleId++;
    }

    static synchronized int nextAppointmentId() {
        return nextAppointmentId++;
    }

    static synchronized Doctor saveDoctor(Doctor doctor) {
        upsertDoctor(doctor);
        return doctor;
    }

    static synchronized Patient savePatient(Patient patient) {
        upsertPatient(patient);
        return patient;
    }

    static synchronized Schedule saveSchedule(Schedule schedule) {
        upsertSchedule(schedule);
        return schedule;
    }

    static synchronized Appointment saveAppointment(Appointment appointment) {
        upsertAppointment(appointment);
        return appointment;
    }

    static synchronized boolean deleteDoctor(int doctorId) {
        return DOCTORS.removeIf(doctor -> doctor.getId() == doctorId);
    }

    static synchronized boolean deletePatient(int patientId) {
        return PATIENTS.removeIf(patient -> patient.getId() == patientId);
    }

    static synchronized boolean deleteSchedule(int scheduleId) {
        return SCHEDULES.removeIf(schedule -> schedule.getId() == scheduleId);
    }

    static synchronized Doctor findDoctorById(int doctorId) {
        for (Doctor doctor : DOCTORS) {
            if (doctor.getId() == doctorId) {
                return doctor;
            }
        }
        return null;
    }

    static synchronized Patient findPatientById(int patientId) {
        for (Patient patient : PATIENTS) {
            if (patient.getId() == patientId) {
                return patient;
            }
        }
        return null;
    }

    static synchronized Schedule findScheduleById(int scheduleId) {
        for (Schedule schedule : SCHEDULES) {
            if (schedule.getId() == scheduleId) {
                return schedule;
            }
        }
        return null;
    }

    static synchronized Appointment findAppointmentById(int appointmentId) {
        for (Appointment appointment : APPOINTMENTS) {
            if (appointment.getId() == appointmentId) {
                return appointment;
            }
        }
        return null;
    }

    static synchronized List<Doctor> getDoctors() {
        List<Doctor> copy = new ArrayList<>(DOCTORS);
        copy.sort(Comparator.comparingInt(Doctor::getId));
        return copy;
    }

    static synchronized List<Patient> getPatients() {
        List<Patient> copy = new ArrayList<>(PATIENTS);
        copy.sort(Comparator.comparingInt(Patient::getId));
        return copy;
    }

    static synchronized List<Schedule> getSchedules() {
        List<Schedule> copy = new ArrayList<>(SCHEDULES);
        copy.sort(Comparator.comparing(Schedule::getWorkDate).thenComparing(Schedule::getTimeSlot));
        return copy;
    }

    static synchronized List<Appointment> getAppointments() {
        List<Appointment> copy = new ArrayList<>(APPOINTMENTS);
        copy.sort(Comparator.comparing(Appointment::getDate).thenComparing(Appointment::getTime));
        return copy;
    }

    private static void upsertDoctor(Doctor doctor) {
        for (int i = 0; i < DOCTORS.size(); i++) {
            if (DOCTORS.get(i).getId() == doctor.getId()) {
                DOCTORS.set(i, doctor);
                return;
            }
        }
        DOCTORS.add(doctor);
    }

    private static void upsertPatient(Patient patient) {
        for (int i = 0; i < PATIENTS.size(); i++) {
            if (PATIENTS.get(i).getId() == patient.getId()) {
                PATIENTS.set(i, patient);
                return;
            }
        }
        PATIENTS.add(patient);
    }

    private static void upsertSchedule(Schedule schedule) {
        for (int i = 0; i < SCHEDULES.size(); i++) {
            if (SCHEDULES.get(i).getId() == schedule.getId()) {
                SCHEDULES.set(i, schedule);
                return;
            }
        }
        SCHEDULES.add(schedule);
    }

    private static void upsertAppointment(Appointment appointment) {
        for (int i = 0; i < APPOINTMENTS.size(); i++) {
            if (APPOINTMENTS.get(i).getId() == appointment.getId()) {
                APPOINTMENTS.set(i, appointment);
                return;
            }
        }
        APPOINTMENTS.add(appointment);
    }
}
