package catering.businesslogic.staff;

import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class Role {

    private Staff worker;
    private String name;
    private String description;
    private Date date;
    private boolean isAssigned;

    /**
     * Costruttore per caricamento da DB
     */
    public Role() {}

    /**
     * Factory method per creare un nuovo ruolo non assegnato
     */
    public static Role create(String name, String description, Date date) {
        Role role = new Role();
        role.name = name;
        role.description = description;
        role.date = date;
        role.isAssigned = false;
        role.worker = null;
        return role;
    }

    // Getter e setter

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

    // ===========
    // Persistence
    // ===========

    public boolean save() {
        // Inserisce il ruolo nel DB (assumiamo che 'name' sia PK o almeno unico)
        String query = "INSERT INTO Role (name, description, date, is_assigned, staff_id) VALUES (?, ?, ?, ?, ?)";
        Integer serialNumber = (worker != null) ? worker.getSerialNumber() : null;
        int rows = PersistenceManager.executeUpdate(query, name, description, date, isAssigned, serialNumber);
        return rows > 0;
    }

    public boolean update() {
        // Aggiorna il ruolo nel DB
        String query = "UPDATE Role SET description = ?, date = ?, is_assigned = ?, staff_id = ? WHERE name = ?";
        Integer serialNumber = (worker != null) ? worker.getSerialNumber() : null;
        int rows = PersistenceManager.executeUpdate(query, description, date, isAssigned, serialNumber, name);
        return rows > 0;
    }

    public boolean delete() {
        String query = "DELETE FROM Role WHERE name = ?";
        int rows = PersistenceManager.executeUpdate(query, name);
        return rows > 0;
    }

    public static Role loadRole(String name) {
        final Role[] resultHolder = new Role[1];
        String query = "SELECT * FROM Role WHERE name = ?";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Role r = new Role();
                r.name = rs.getString("name");
                r.description = rs.getString("description");
                r.date = rs.getDate("date");
                r.isAssigned = rs.getBoolean("is_assigned");
                int staffId = rs.getInt("staff_id");
                if (!rs.wasNull()) {
                    r.worker = Staff.loadStaff(staffId);
                } else {
                    r.worker = null;
                }
                resultHolder[0] = r;
            }
        }, name);

        return resultHolder[0];
    }

    public static ArrayList<Role> loadAllRoles() {
        ArrayList<Role> roles = new ArrayList<>();
        String query = "SELECT * FROM Role";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Role r = new Role();
                r.name = rs.getString("name");
                r.description = rs.getString("description");
                r.date = rs.getDate("date");
                r.isAssigned = rs.getBoolean("is_assigned");
                int staffId = rs.getInt("staff_id");
                if (!rs.wasNull()) {
                    r.worker = Staff.loadStaff(staffId);
                } else {
                    r.worker = null;
                }
                roles.add(r);
            }
        });

        return roles;
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
