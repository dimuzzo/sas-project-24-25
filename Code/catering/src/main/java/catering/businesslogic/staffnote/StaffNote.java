/**
 * * @author Alessandro Demo, Matricola 1049825
 */
package catering.businesslogic.staffnote;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import catering.businesslogic.staff.Staff;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;

/**
 * The StaffNote class represents a note entity written by an organizer
 * about a specific staff member. It contains the note's content, date,
 * and references to the owner (author) and the worker.
 */
public class StaffNote {
    private int id; // The unique identifier for the note in the database. It is set only after saving.
    private Staff worker; // The staff member to whom the note refers.
    private String description; // The textual content of the note.
    private Date date; // The date the note was created or to which it refers.
    private final User owner; // The user who created the note.

    /**
     * Private constructor to enforce object creation via the factory method.
     * @param owner The user who owns this note.
     * @param worker The staff member this note is about.
     * @param description The content of the note.
     * @param date The date the note was created.
     */
    private StaffNote(User owner, Staff worker, String description, Date date) {
        this.id = 0; // A new, unsaved note has an ID of 0
        this.owner = owner;
        this.worker = worker;
        this.description = description;
        this.date = date;
    }

    /**
     * Factory method for creating a new StaffNote.
     * @param owner The User who is creating the note.
     * @param worker The Staff member the note is about.
     * @param description The content of the note.
     * @param date The date the note was made.
     * @return A new StaffNote instance.
     */
    public static StaffNote create(User owner, Staff worker, String description, Date date) {
        return new StaffNote(owner, worker, description, date);
    }

    // Getters and Setters

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

    /**
     * Checks if the specified user is the creator of this note.
     * @param user The user to check.
     * @return true if the user is the owner, false otherwise.
     */
    public boolean isOwner(User user) {
        return this.owner != null && this.owner.equals(user);
    }

    // Persistence Methods

    /**
     * Saves a new note to the database.
     * If the note already has an ID, the operation is not performed.
     * @return true if the save was successful, false otherwise.
     */
    public boolean save() {
        if (this.id != 0) return false; // Already saved
        String query = "INSERT INTO StaffNotes (worker_serial_number, owner_id, description, note_date) VALUES (?, ?, ?, ?)";
        int rows = PersistenceManager.executeUpdate(query, this.worker.getSerialNumber(), this.owner.getId(), this.description, this.date);
        if (rows > 0) {
            this.id = PersistenceManager.getLastId();
            return true;
        }
        return false;
    }

    /**
     * Updates the data of an existing note in the database.
     * If the note has never been saved (ID is 0), the operation is not performed.
     * @return true if the update was successful, false otherwise.
     */
    public boolean update() {
        if (this.id == 0) return false;
        String query = "UPDATE StaffNotes SET description = ?, note_date = ? WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, this.description, this.date, this.id);
        return rows > 0;
    }

    /**
     * Deletes this note from the database.
     * If the note has never been saved, the operation is not performed.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean delete() {
        if (this.id == 0) return false;
        String query = "DELETE FROM StaffNotes WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, this.id);
        return rows > 0;
    }

    /**
     * Loads a single note from the database using its ID.
     * @param noteId The ID of the note to load.
     * @return The corresponding StaffNote object, or null if not found.
     */
    public static StaffNote load(int noteId) {
        final StaffNote[] result = new StaffNote[1];
        String query = "SELECT * FROM StaffNotes WHERE id = ?";
        PersistenceManager.executeQuery(query, rs -> {
            result[0] = fromResultSet(rs);
        }, noteId);
        return result[0];
    }

    /**
     * Loads all notes associated with a specific staff member.
     * @param worker The staff member whose notes are to be loaded.
     * @return An ArrayList of StaffNote objects.
     */
    public static ArrayList<StaffNote> loadAllFor(Staff worker) {
        ArrayList<StaffNote> notes = new ArrayList<>();
        String query = "SELECT * FROM StaffNotes WHERE worker_serial_number = ?";
        PersistenceManager.executeQuery(query, rs -> {
            notes.add(fromResultSet(rs));
        }, worker.getSerialNumber());
        return notes;
    }

    /**
     * Private helper method to create a StaffNote instance from a ResultSet row.
     * @param rs The ResultSet positioned on the correct row.
     * @return A populated StaffNote object.
     * @throws SQLException if a database access error occurs.
     */
    private static StaffNote fromResultSet(ResultSet rs) throws SQLException {
        User owner = User.load(rs.getInt("owner_id"));
        Staff worker = Staff.loadStaff(rs.getInt("worker_serial_number"));
        StaffNote note = new StaffNote(owner, worker, rs.getString("description"), rs.getDate("note_date"));
        note.id = rs.getInt("id");
        return note;
    }
}