package catering.businesslogic.summaryform;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import catering.businesslogic.event.Event;
import catering.businesslogic.staff.Role;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;

public class SummaryForm {
    private int id; // Aggiunto ID per il database
    private String description;
    private boolean inUse;
    private User owner;

    private List<Event> associatedEvents;
    private List<Role> associatedRoles;

    public static SummaryForm create(User owner, String description) {
        SummaryForm sf = new SummaryForm(owner, description);
        // Nota: le liste associate non sono gestite a livello di DB in questa tabella
        return sf;
    }

    private SummaryForm(User owner, String description) {
        this.id = 0; // Un form non salvato ha id 0
        this.owner = owner;
        this.description = description;
        this.inUse = false;
        this.associatedEvents = new ArrayList<>();
        this.associatedRoles = new ArrayList<>();
    }
    
    public int getId() { 
        return id; 
    }

    public boolean isOwner(User user) {
        return this.owner != null && this.owner.equals(user);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public boolean containsEvent(Event ev) {
        return associatedEvents.contains(ev);
    }

    public boolean containsRole(Role r) {
        return associatedRoles.contains(r);
    }

    public void addEvent(Event ev) {
        if (!associatedEvents.contains(ev)) {
            associatedEvents.add(ev);
        }
    }

    public void addRole(Role r) {
        if (!associatedRoles.contains(r)) {
            associatedRoles.add(r);
        }
    }

    public List<Event> getAssociatedEvents() {
        return associatedEvents;
    }

    public List<Role> getAssociatedRoles() {
        return associatedRoles;
    }

    public User getOwner() {
        return owner;
    }

    public boolean save() {
        if (this.id != 0) return false;
        String query = "INSERT INTO SummaryForms (description, in_use, owner_id) VALUES (?, ?, ?)";
        int inUseInt = this.inUse ? 1 : 0;
        int rows = PersistenceManager.executeUpdate(query, this.description, inUseInt, this.owner.getId());
        if (rows > 0) {
            this.id = PersistenceManager.getLastId();
            return true;
        }
        return false;
    }

    public boolean update() {
        if (this.id == 0) return false;
        String query = "UPDATE SummaryForms SET description = ?, in_use = ? WHERE id = ?";
        int inUseInt = this.inUse ? 1 : 0;
        int rows = PersistenceManager.executeUpdate(query, this.description, inUseInt, this.id);
        return rows > 0;
    }

    public boolean delete() {
        if (this.id == 0) return false;
        String query = "DELETE FROM SummaryForms WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, this.id);
        return rows > 0;
    }

    public static SummaryForm load(int formId) {
        final SummaryForm[] result = new SummaryForm[1];
        String query = "SELECT * FROM SummaryForms WHERE id = ?";
        PersistenceManager.executeQuery(query, rs -> {
            result[0] = fromResultSet(rs);
        }, formId);
        return result[0];
    }
    
    private static SummaryForm fromResultSet(ResultSet rs) throws SQLException {
        int ownerId = rs.getInt("owner_id");
        User owner = User.load(ownerId);
        SummaryForm sf = new SummaryForm(owner, rs.getString("description"));
        sf.id = rs.getInt("id");
        sf.inUse = rs.getBoolean("in_use");
        return sf;
    }
}
