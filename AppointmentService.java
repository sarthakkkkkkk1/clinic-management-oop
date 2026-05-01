package service;

import dao.AppointmentDAO;
import model.Appointment;

public class AppointmentService {

    private final AppointmentDAO dao = new AppointmentDAO();

    public boolean book(Appointment appt) {
        if (dao.isSlotAvailable(appt.getDoctorId(), appt.getDate(), appt.getTime())) {
            dao.bookAppointment(appt);
            return true;
        } else {
            return false;
        }
    }
}
