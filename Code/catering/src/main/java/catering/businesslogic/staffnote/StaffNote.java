package catering.businesslogic.staffnote;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import catering.businesslogic.staff.Staff;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;

public class StaffNote {
    private int id; // Aggiunto ID per il database
    private Staff worker;
    private String description;
    private Date date;
    private User owner;

    public static StaffNote create(User owner, Staff worker, String description, Date date) {
        StaffNote note = new StaffNote();
        note.owner = owner;
        note.worker = worker;
        note.description = description;
        note.date = date;
        return note;
    }

    public int getId() { 
        return id;
    }

    public Staff getStaff() {
        return worker;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    public void setWorker(Staff worker) {
        this.worker = worker;
    }

    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isOwner(User user) {
        return this.owner != null && this.owner.equals(user);
    }

    // METODI DI PERSISTENZA AGGIUNTI
    public boolean save() {
        if (this.id != 0) return false; // Già salvata
        String query = "INSERT INTO StaffNotes (worker_serial_number, owner_id, description, note_date) VALUES (?, ?, ?, ?)";
        int rows = PersistenceManager.executeUpdate(query, this.worker.getSerialNumber(), this.owner.getId(), this.description, this.date);
        if (rows > 0) {
            this.id = PersistenceManager.getLastId();
            return true;
        }
        return false;
    }

    public boolean update() {
        if (this.id == 0) return false; // Mai salvata
        String query = "UPDATE StaffNotes SET description = ?, note_date = ? WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, this.description, this.date, this.id);
        return rows > 0;
    }

    public boolean delete() {
        if (this.id == 0) return false;
        String query = "DELETE FROM StaffNotes WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, this.id);
        return rows > 0;
    }

    public static StaffNote load(int noteId) {
        final StaffNote[] result = new StaffNote[1];
        String query = "SELECT * FROM StaffNotes WHERE id = ?";
        PersistenceManager.executeQuery(query, rs -> {
            result[0] = fromResultSet(rs);
        }, noteId);
        return result[0];
    }

    public static ArrayList<StaffNote> loadAllFor(Staff worker) {
        ArrayList<StaffNote> notes = new ArrayList<>();
        String query = "SELECT * FROM StaffNotes WHERE worker_serial_number = ?";
        PersistenceManager.executeQuery(query, rs -> {
            notes.add(fromResultSet(rs));
        }, worker.getSerialNumber());
        return notes;
    }

    private static StaffNote fromResultSet(ResultSet rs) throws SQLException {
        int ownerId = rs.getInt("owner_id");
        int workerSn = rs.getInt("worker_serial_number");
        User owner = User.load(ownerId);
        Staff worker = Staff.loadStaff(workerSn);

        StaffNote note = new StaffNote();
        note.id = rs.getInt("id");
        note.owner = owner;
        note.worker = worker;
        note.description = rs.getString("description");
        note.date = rs.getDate("note_date");
        return note;
    }
}
