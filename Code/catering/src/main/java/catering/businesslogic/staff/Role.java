package catering.businesslogic.staff;

import java.sql.*;

public class Role {

    private Staff worker;
    private String name;
    private String description;
    private Date date;
    private boolean isAssigned;

    // Costruttore privato: uso obbligatorio di create()
    Role() {}

    public static Role create(String name, String description, Date date) {
        Role role = new Role();
        role.name = name;
        role.description = description;
        role.date = date;
        role.isAssigned = false;
        role.worker = null;
        return role;
    }

    // Getters
    public Staff getStaff() {
        return worker;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    public boolean isAssigned() {
        return isAssigned;
    }

    // Setters
    public void setWorker(Staff worker) {
        this.worker = worker;
        this.isAssigned = (worker != null);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setAssigned(boolean assigned) {
        this.isAssigned = assigned;
    }

    @Override
    public String toString() {
        return "Role{" +
                "name='" + name + '\'' +
                ", isAssigned=" + isAssigned +
                ", worker=" + (worker != null ? worker.getSerialNumber() : "null") +
                '}';
    }
}
