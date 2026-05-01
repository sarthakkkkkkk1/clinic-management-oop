package model;

public class Patient {
    private final int id;
    private final String name;
    private final int age;
    private final String phone;

    public Patient(int id, String name, int age) {
        this(id, name, age, "");
    }

    public Patient(int id, String name, int age, String phone) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.phone = phone;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getPhone() { return phone; }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
