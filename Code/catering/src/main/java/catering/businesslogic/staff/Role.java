package catering.businesslogic.staff;

import catering.persistence.BatchUpdateHandler;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
     * Salva un nuovo ruolo nel database e recupera l'ID auto-generato,
     * seguendo il pattern di Menu.create(Menu m).
     * @param roleToSave L'oggetto Role da salvare, con ID=0.
     */
    public static void create(Role roleToSave) throws SQLException {
        if (roleToSave.id != 0) return; // Già salvato
        String query = "INSERT INTO EventRoles (name, description, date, is_assigned, staff_id) VALUES (?, ?, ?, ?, ?)";
        Integer staffId = (roleToSave.worker != null) ? roleToSave.worker.getSerialNumber() : null;

        PersistenceManager.executeBatchUpdate(query, 1, new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int batchCount) throws SQLException {
                ps.setString(1, roleToSave.name);
                ps.setString(2, roleToSave.description);
                ps.setDate(3, roleToSave.date);
                ps.setBoolean(4, roleToSave.isAssigned);
                if (staffId != null) {
                    ps.setInt(5, staffId);
                } else {
                    ps.setNull(5, java.sql.Types.INTEGER);
                }
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) throws SQLException {
                // Assegna l'ID generato dal DB all'oggetto originale.
                if (count == 0) {
                    roleToSave.id = rs.getInt(1);
                }
            }
        });
    }

    /**
     * Aggiorna un ruolo esistente nel DB. Questo rimane un metodo di istanza.
     * @return true se l'aggiornamento è riuscito, false altrimenti.
     */
    public boolean update() {
        if (this.id == 0) return false; // Non si può aggiornare un ruolo non salvato.
        String query = "UPDATE EventRoles SET name = ?, description = ?, date = ?, is_assigned = ?, staff_id = ? WHERE id = ?";
        Integer staffId = (worker != null) ? worker.getSerialNumber() : null;
        int rows = PersistenceManager.executeUpdate(query, this.name, this.description, this.date, this.isAssigned, staffId, this.id);
        return rows > 0;
    }

    /**
     * Cancella un ruolo dal DB. Questo rimane un metodo di istanza.
     * @return true se la cancellazione è riuscita, false altrimenti.
     */
    public boolean delete() throws RoleException {
        if (this.id == 0) return false;
        if (this.isAssigned()) {
            // Lancia un'eccezione come specificato nel caso d'uso 2a.1a
            throw new RoleException("Il ruolo che si sta cercando di eliminare è in uso.");
        }
        String query = "DELETE FROM EventRoles WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, this.id);
        return rows > 0;
    }

    public static Role loadRole(String name) {
        final Role[] resultHolder = new Role[1];
        String query = "SELECT * FROM EventRoles WHERE id = ?";

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
        String query = "SELECT * FROM EventRoles";

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

    /**
     * Metodo di supporto per creare un oggetto Role da un ResultSet del database.
     */
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
