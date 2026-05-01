package ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import model.Appointment;
import model.Doctor;
import model.Patient;
import model.Schedule;
import service.ClinicService;

public class MainUI {
    private final ClinicService clinicService = new ClinicService();

    private final JLabel doctorCountLabel = new JLabel();
    private final JLabel patientCountLabel = new JLabel();
    private final JLabel availableSlotCountLabel = new JLabel();
    private final JLabel appointmentCountLabel = new JLabel();

    private final JTextField doctorIdField = new JTextField();
    private final JTextField doctorNameField = new JTextField();
    private final JTextField doctorSpecializationField = new JTextField();
    private final JTextField doctorPhoneField = new JTextField();
    private final DefaultTableModel doctorTableModel = createTableModel("ID", "Name", "Specialization", "Phone");
    private final JTable doctorTable = new JTable(doctorTableModel);

    private final JTextField patientIdField = new JTextField();
    private final JTextField patientNameField = new JTextField();
    private final JTextField patientAgeField = new JTextField();
    private final JTextField patientPhoneField = new JTextField();
    private final DefaultTableModel patientTableModel = createTableModel("ID", "Name", "Age", "Phone");
    private final JTable patientTable = new JTable(patientTableModel);

    private final JTextField scheduleIdField = new JTextField();
    private final JComboBox<Doctor> scheduleDoctorCombo = new JComboBox<>();
    private final JTextField scheduleDateField = new JTextField("2026-05-01");
    private final JTextField scheduleTimeField = new JTextField("09:00");
    private final JCheckBox scheduleAvailableCheck = new JCheckBox("Available", true);
    private final DefaultTableModel scheduleTableModel =
        createTableModel("ID", "Doctor", "Date", "Time", "Available");
    private final JTable scheduleTable = new JTable(scheduleTableModel);

    private final JComboBox<Doctor> bookingDoctorCombo = new JComboBox<>();
    private final JComboBox<Patient> bookingPatientCombo = new JComboBox<>();
    private final JComboBox<Schedule> bookingScheduleCombo = new JComboBox<>();

    private final DefaultTableModel historyTableModel =
        createTableModel("Appointment ID", "Doctor", "Patient", "Date", "Time", "Status", "Schedule ID");
    private final JTable historyTable = new JTable(historyTableModel);
    private final JComboBox<Schedule> rescheduleCombo = new JComboBox<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainUI().createAndShowUI());
    }

    private void createAndShowUI() {
        clinicService.initializeSampleData();

        doctorIdField.setEditable(false);
        patientIdField.setEditable(false);
        scheduleIdField.setEditable(false);

        doctorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JFrame frame = new JFrame("Clinic Appointment System");
        frame.setSize(1100, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(createTabs());
        frame.setLocationRelativeTo(null);

        registerListeners();
        refreshAllData();

        frame.setVisible(true);
    }

    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", createDashboardPanel());
        tabs.addTab("Doctors", createDoctorPanel());
        tabs.addTab("Patients", createPatientPanel());
        tabs.addTab("Schedules", createSchedulePanel());
        tabs.addTab("Book Appointment", createBookingPanel());
        tabs.addTab("Appointment History", createHistoryPanel());
        return tabs;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.add(doctorCountLabel);
        panel.add(patientCountLabel);
        panel.add(availableSlotCountLabel);
        panel.add(appointmentCountLabel);
        return panel;
    }

    private JPanel createDoctorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));

        JButton addButton = new JButton("Add Doctor");
        JButton updateButton = new JButton("Update Doctor");
        JButton deleteButton = new JButton("Delete Doctor");
        JButton clearButton = new JButton("Clear");

        addButton.addActionListener(e -> runAction(() -> {
            clinicService.addDoctor(
                doctorNameField.getText(),
                doctorSpecializationField.getText(),
                doctorPhoneField.getText()
            );
            clearDoctorForm();
            refreshAllData();
        }));
        updateButton.addActionListener(e -> runAction(() -> {
            clinicService.updateDoctor(
                parseRequiredId(doctorIdField, "doctor"),
                doctorNameField.getText(),
                doctorSpecializationField.getText(),
                doctorPhoneField.getText()
            );
            clearDoctorForm();
            refreshAllData();
        }));
        deleteButton.addActionListener(e -> runAction(() -> {
            clinicService.deleteDoctor(parseRequiredId(doctorIdField, "doctor"));
            clearDoctorForm();
            refreshAllData();
        }));
        clearButton.addActionListener(e -> clearDoctorForm());

        form.add(new JLabel("Doctor ID"));
        form.add(doctorIdField);
        form.add(new JLabel("Name"));
        form.add(doctorNameField);
        form.add(new JLabel("Specialization"));
        form.add(doctorSpecializationField);
        form.add(new JLabel("Phone"));
        form.add(doctorPhoneField);
        form.add(addButton);
        form.add(updateButton);

        JPanel bottomButtons = new JPanel(new GridLayout(1, 2, 8, 8));
        bottomButtons.add(deleteButton);
        bottomButtons.add(clearButton);

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(doctorTable), BorderLayout.CENTER);
        panel.add(bottomButtons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPatientPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));

        JButton addButton = new JButton("Add Patient");
        JButton updateButton = new JButton("Update Patient");
        JButton deleteButton = new JButton("Delete Patient");
        JButton clearButton = new JButton("Clear");

        addButton.addActionListener(e -> runAction(() -> {
            clinicService.addPatient(
                patientNameField.getText(),
                Integer.parseInt(patientAgeField.getText().trim()),
                patientPhoneField.getText()
            );
            clearPatientForm();
            refreshAllData();
        }));
        updateButton.addActionListener(e -> runAction(() -> {
            clinicService.updatePatient(
                parseRequiredId(patientIdField, "patient"),
                patientNameField.getText(),
                Integer.parseInt(patientAgeField.getText().trim()),
                patientPhoneField.getText()
            );
            clearPatientForm();
            refreshAllData();
        }));
        deleteButton.addActionListener(e -> runAction(() -> {
            clinicService.deletePatient(parseRequiredId(patientIdField, "patient"));
            clearPatientForm();
            refreshAllData();
        }));
        clearButton.addActionListener(e -> clearPatientForm());

        form.add(new JLabel("Patient ID"));
        form.add(patientIdField);
        form.add(new JLabel("Name"));
        form.add(patientNameField);
        form.add(new JLabel("Age"));
        form.add(patientAgeField);
        form.add(new JLabel("Phone"));
        form.add(patientPhoneField);
        form.add(addButton);
        form.add(updateButton);

        JPanel bottomButtons = new JPanel(new GridLayout(1, 2, 8, 8));
        bottomButtons.add(deleteButton);
        bottomButtons.add(clearButton);

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(patientTable), BorderLayout.CENTER);
        panel.add(bottomButtons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));

        JButton addButton = new JButton("Add Schedule");
        JButton updateButton = new JButton("Update Schedule");
        JButton deleteButton = new JButton("Delete Schedule");
        JButton clearButton = new JButton("Clear");

        addButton.addActionListener(e -> runAction(() -> {
            Doctor doctor = requireComboSelection(scheduleDoctorCombo, "doctor");
            clinicService.addSchedule(doctor.getId(), scheduleDateField.getText(), scheduleTimeField.getText());
            clearScheduleForm();
            refreshAllData();
        }));
        updateButton.addActionListener(e -> runAction(() -> {
            Doctor doctor = requireComboSelection(scheduleDoctorCombo, "doctor");
            clinicService.updateSchedule(
                parseRequiredId(scheduleIdField, "schedule"),
                doctor.getId(),
                scheduleDateField.getText(),
                scheduleTimeField.getText(),
                scheduleAvailableCheck.isSelected()
            );
            clearScheduleForm();
            refreshAllData();
        }));
        deleteButton.addActionListener(e -> runAction(() -> {
            clinicService.deleteSchedule(parseRequiredId(scheduleIdField, "schedule"));
            clearScheduleForm();
            refreshAllData();
        }));
        clearButton.addActionListener(e -> clearScheduleForm());

        form.add(new JLabel("Schedule ID"));
        form.add(scheduleIdField);
        form.add(new JLabel("Doctor"));
        form.add(scheduleDoctorCombo);
        form.add(new JLabel("Date (yyyy-mm-dd)"));
        form.add(scheduleDateField);
        form.add(new JLabel("Time (HH:mm)"));
        form.add(scheduleTimeField);
        form.add(new JLabel("Status"));
        form.add(scheduleAvailableCheck);
        form.add(addButton);
        form.add(updateButton);

        JPanel bottomButtons = new JPanel(new GridLayout(1, 2, 8, 8));
        bottomButtons.add(deleteButton);
        bottomButtons.add(clearButton);

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);
        panel.add(bottomButtons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));

        JButton bookButton = new JButton("Book Appointment");
        JButton refreshButton = new JButton("Refresh Slots");

        bookButton.addActionListener(e -> runAction(() -> {
            Doctor doctor = requireComboSelection(bookingDoctorCombo, "doctor");
            Patient patient = requireComboSelection(bookingPatientCombo, "patient");
            Schedule schedule = requireComboSelection(bookingScheduleCombo, "schedule");
            Appointment appointment = clinicService.bookAppointment(doctor.getId(), patient.getId(), schedule.getId());
            refreshAllData();
            JOptionPane.showMessageDialog(null, "Appointment booked successfully. ID: " + appointment.getId());
        }));
        refreshButton.addActionListener(e -> refreshBookingSchedules());

        form.add(new JLabel("Doctor"));
        form.add(bookingDoctorCombo);
        form.add(new JLabel("Patient"));
        form.add(bookingPatientCombo);
        form.add(new JLabel("Available Slot"));
        form.add(bookingScheduleCombo);
        form.add(bookButton);
        form.add(refreshButton);

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JLabel("Select a doctor, patient, and open slot to book an appointment."), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel controls = new JPanel(new GridLayout(2, 2, 8, 8));

        JButton cancelButton = new JButton("Cancel Selected");
        JButton rescheduleButton = new JButton("Reschedule Selected");
        JButton refreshButton = new JButton("Refresh History");

        cancelButton.addActionListener(e -> runAction(() -> {
            clinicService.cancelAppointment(getSelectedHistoryAppointmentId());
            refreshAllData();
        }));
        rescheduleButton.addActionListener(e -> runAction(() -> {
            Schedule schedule = requireComboSelection(rescheduleCombo, "new schedule");
            clinicService.rescheduleAppointment(getSelectedHistoryAppointmentId(), schedule.getId());
            refreshAllData();
        }));
        refreshButton.addActionListener(e -> refreshAllData());

        controls.add(new JLabel("Reschedule To"));
        controls.add(rescheduleCombo);
        controls.add(cancelButton);
        controls.add(rescheduleButton);

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.SOUTH);
        return panel;
    }

    private void registerListeners() {
        doctorTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && doctorTable.getSelectedRow() >= 0) {
                doctorIdField.setText(doctorTableModel.getValueAt(doctorTable.getSelectedRow(), 0).toString());
                doctorNameField.setText(doctorTableModel.getValueAt(doctorTable.getSelectedRow(), 1).toString());
                doctorSpecializationField.setText(doctorTableModel.getValueAt(doctorTable.getSelectedRow(), 2).toString());
                doctorPhoneField.setText(doctorTableModel.getValueAt(doctorTable.getSelectedRow(), 3).toString());
            }
        });

        patientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && patientTable.getSelectedRow() >= 0) {
                patientIdField.setText(patientTableModel.getValueAt(patientTable.getSelectedRow(), 0).toString());
                patientNameField.setText(patientTableModel.getValueAt(patientTable.getSelectedRow(), 1).toString());
                patientAgeField.setText(patientTableModel.getValueAt(patientTable.getSelectedRow(), 2).toString());
                patientPhoneField.setText(patientTableModel.getValueAt(patientTable.getSelectedRow(), 3).toString());
            }
        });

        scheduleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && scheduleTable.getSelectedRow() >= 0) {
                int row = scheduleTable.getSelectedRow();
                scheduleIdField.setText(scheduleTableModel.getValueAt(row, 0).toString());
                scheduleDateField.setText(scheduleTableModel.getValueAt(row, 2).toString());
                scheduleTimeField.setText(scheduleTableModel.getValueAt(row, 3).toString());
                scheduleAvailableCheck.setSelected(Boolean.parseBoolean(scheduleTableModel.getValueAt(row, 4).toString()));
                selectDoctorInCombo(scheduleDoctorCombo, Integer.parseInt(scheduleTableModel.getValueAt(row, 1).toString()));
            }
        });

        bookingDoctorCombo.addActionListener(e -> refreshBookingSchedules());
    }

    private void refreshAllData() {
        refreshDashboard();
        refreshDoctorTable();
        refreshPatientTable();
        refreshScheduleTable();
        refreshDoctorCombos();
        refreshPatientCombos();
        refreshBookingSchedules();
        refreshHistoryTable();
        refreshRescheduleCombo();
    }

    private void refreshDashboard() {
        doctorCountLabel.setText("Doctors Registered: " + clinicService.getDoctorCount());
        patientCountLabel.setText("Patients Registered: " + clinicService.getPatientCount());
        availableSlotCountLabel.setText("Available Slots: " + clinicService.getAvailableScheduleCount());
        appointmentCountLabel.setText("Appointments Recorded: " + clinicService.getAppointmentCount());
    }

    private void refreshDoctorTable() {
        resetTable(doctorTableModel);
        for (Doctor doctor : clinicService.getAllDoctors()) {
            doctorTableModel.addRow(new Object[] {
                doctor.getId(),
                doctor.getName(),
                doctor.getSpecialization(),
                doctor.getPhone()
            });
        }
    }

    private void refreshPatientTable() {
        resetTable(patientTableModel);
        for (Patient patient : clinicService.getAllPatients()) {
            patientTableModel.addRow(new Object[] {
                patient.getId(),
                patient.getName(),
                patient.getAge(),
                patient.getPhone()
            });
        }
    }

    private void refreshScheduleTable() {
        resetTable(scheduleTableModel);
        for (Schedule schedule : clinicService.getAllSchedules()) {
            scheduleTableModel.addRow(new Object[] {
                schedule.getId(),
                schedule.getDoctorId(),
                schedule.getWorkDate(),
                schedule.getTimeSlot(),
                schedule.isAvailable()
            });
        }
    }

    private void refreshHistoryTable() {
        resetTable(historyTableModel);
        for (Appointment appointment : clinicService.getAllAppointments()) {
            Doctor doctor = clinicService.getDoctorById(appointment.getDoctorId());
            Patient patient = clinicService.getPatientById(appointment.getPatientId());
            historyTableModel.addRow(new Object[] {
                appointment.getId(),
                doctor == null ? appointment.getDoctorId() : doctor.getName(),
                patient == null ? appointment.getPatientId() : patient.getName(),
                appointment.getDate(),
                appointment.getTime(),
                appointment.getStatus(),
                appointment.getScheduleId()
            });
        }
    }

    private void refreshDoctorCombos() {
        Doctor selectedScheduleDoctor = (Doctor) scheduleDoctorCombo.getSelectedItem();
        Doctor selectedBookingDoctor = (Doctor) bookingDoctorCombo.getSelectedItem();
        fillCombo(scheduleDoctorCombo, clinicService.getAllDoctors(), selectedScheduleDoctor == null ? -1 : selectedScheduleDoctor.getId());
        fillCombo(bookingDoctorCombo, clinicService.getAllDoctors(), selectedBookingDoctor == null ? -1 : selectedBookingDoctor.getId());
    }

    private void refreshPatientCombos() {
        Patient selectedPatient = (Patient) bookingPatientCombo.getSelectedItem();
        fillCombo(bookingPatientCombo, clinicService.getAllPatients(), selectedPatient == null ? -1 : selectedPatient.getId());
    }

    private void refreshBookingSchedules() {
        Doctor selectedDoctor = (Doctor) bookingDoctorCombo.getSelectedItem();
        int selectedScheduleId = ((Schedule) bookingScheduleCombo.getSelectedItem()) == null
            ? -1 : ((Schedule) bookingScheduleCombo.getSelectedItem()).getId();

        List<Schedule> schedules = selectedDoctor == null
            ? clinicService.getAvailableSchedules()
            : clinicService.getAvailableSchedulesByDoctor(selectedDoctor.getId());
        fillCombo(bookingScheduleCombo, schedules, selectedScheduleId);
    }

    private void refreshRescheduleCombo() {
        Schedule selected = (Schedule) rescheduleCombo.getSelectedItem();
        fillCombo(rescheduleCombo, clinicService.getAvailableSchedules(), selected == null ? -1 : selected.getId());
    }

    private void clearDoctorForm() {
        doctorIdField.setText("");
        doctorNameField.setText("");
        doctorSpecializationField.setText("");
        doctorPhoneField.setText("");
        doctorTable.clearSelection();
    }

    private void clearPatientForm() {
        patientIdField.setText("");
        patientNameField.setText("");
        patientAgeField.setText("");
        patientPhoneField.setText("");
        patientTable.clearSelection();
    }

    private void clearScheduleForm() {
        scheduleIdField.setText("");
        scheduleDateField.setText("2026-05-01");
        scheduleTimeField.setText("09:00");
        scheduleAvailableCheck.setSelected(true);
        scheduleTable.clearSelection();
    }

    private int getSelectedHistoryAppointmentId() {
        int row = historyTable.getSelectedRow();
        if (row < 0) {
            throw new IllegalArgumentException("Select an appointment from the history table.");
        }
        return Integer.parseInt(historyTableModel.getValueAt(row, 0).toString());
    }

    private int parseRequiredId(JTextField field, String label) {
        if (field.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Select a " + label + " first.");
        }
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + label + " ID.");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T requireComboSelection(JComboBox<T> comboBox, String label) {
        T selected = (T) comboBox.getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Select a " + label + ".");
        }
        return selected;
    }

    private void runAction(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Action Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectDoctorInCombo(JComboBox<Doctor> comboBox, int doctorId) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Doctor doctor = comboBox.getItemAt(i);
            if (doctor.getId() == doctorId) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private <T> void fillCombo(JComboBox<T> comboBox, List<T> items, int preferredId) {
        comboBox.removeAllItems();
        for (T item : items) {
            comboBox.addItem(item);
        }

        if (preferredId < 0) {
            if (comboBox.getItemCount() > 0) {
                comboBox.setSelectedIndex(0);
            }
            return;
        }

        for (int i = 0; i < comboBox.getItemCount(); i++) {
            T item = comboBox.getItemAt(i);
            if (item instanceof Doctor && ((Doctor) item).getId() == preferredId) {
                comboBox.setSelectedIndex(i);
                return;
            }
            if (item instanceof Patient && ((Patient) item).getId() == preferredId) {
                comboBox.setSelectedIndex(i);
                return;
            }
            if (item instanceof Schedule && ((Schedule) item).getId() == preferredId) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }

        if (comboBox.getItemCount() > 0) {
            comboBox.setSelectedIndex(0);
        }
    }

    private void resetTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
    }

    private DefaultTableModel createTableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
