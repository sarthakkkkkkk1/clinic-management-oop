package model;

public class Doctor {
    private final int id;
    private final String name;
    private final String specialization;
    private final String phone;

    public Doctor(int id, String name, String specialization) {
        this(id, name, specialization, "");
    }

    public Doctor(int id, String name, String specialization, String phone) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public String getPhone() { return phone; }

    @Override
    public String toString() {
        return id + " - " + name + " (" + specialization + ")";
    }
}
