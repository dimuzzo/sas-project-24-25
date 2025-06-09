package catering.businesslogic.staff;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

public class Role {

    private int id; // L'ID che verrà generato dal DB
    private Staff worker;
    private String name;
    private String description;
    private Date date;
    private boolean isAssigned;

    // Il costruttore privato assicura che gli oggetti vengano creati tramite il factory method
    private Role() {
        this.id = 0; // Un nuovo ruolo non ha ID finché non viene salvato
    }

    /**
     * Factory method per creare un nuovo ruolo non assegnato
     */
    public static Role create(String name, String description, Date date, boolean isAssigned) {
        Role role = new Role();
        role.name = name;
        role.description = description;
        role.date = date;
        role.isAssigned = false;
        role.worker = null;
        return role;
    }

    // Getter e setter

    public int getId() { 
        return id; 
    }

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

     /**
     * Salva un nuovo ruolo nel DB e recupera l'ID.
     */
    public static void create(Role roleToSave) throws SQLException {
        if (roleToSave.id != 0) return;

        String insertQuery = "INSERT INTO EventRoles (name, description, date, is_assigned, staff_id) VALUES (?, ?, ?, ?, ?)";
        Integer staffId = (roleToSave.worker != null) ? roleToSave.worker.getSerialNumber() : null;

        int rows = PersistenceManager.executeUpdate(insertQuery,
                roleToSave.name,
                roleToSave.description,
                roleToSave.date,
                roleToSave.isAssigned,
                staffId
        );

        if (rows > 0) {
            roleToSave.id = PersistenceManager.getLastId();
        }
    }

    public boolean update() {
        if (this.id == 0) return false;
        String query = "UPDATE EventRoles SET name = ?, description = ?, date = ?, is_assigned = ?, staff_id = ? WHERE id = ?";
        Integer staffId = (worker != null) ? worker.getSerialNumber() : null;
        int rows = PersistenceManager.executeUpdate(query, this.name, this.description, this.date, this.isAssigned, staffId, this.id);
        return rows > 0;
    }

    public boolean delete() throws RoleException {
        if (this.id == 0) return false;
        if (this.isAssigned()) {
            throw new RoleException("Il ruolo che si sta cercando di eliminare è in uso.");
        }
        String query = "DELETE FROM EventRoles WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, this.id);
        return rows > 0;
    }

    // CODICE CORRETTO per Role.java
    public static Role loadRole(String name) {
        final Role[] resultHolder = new Role[1];
        String query = "SELECT * FROM EventRoles WHERE name = ?";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                resultHolder[0] = fromResultSet(rs);
            }
        }, name);
        return resultHolder[0];
    }

    private static Role fromResultSet(ResultSet rs) throws SQLException {
        Role r = new Role();
        r.id = rs.getInt("id");
        r.name = rs.getString("name");
        r.description = rs.getString("description");
        r.date = rs.getDate("date");
        r.isAssigned = rs.getBoolean("is_assigned");
        int staffId = rs.getInt("staff_id");
        if (!rs.wasNull()) {
            r.worker = Staff.loadStaff(staffId);
        }
        return r;
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
