/**
 * * @author Alessandro Demo, Matricola 1049825
 */
package catering.businesslogic.staffnote;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffManager;
import catering.businesslogic.user.User;

/**
 * Manages all business logic related to staff notes.
 * It acts as a controller for creating, updating, deleting, and retrieving notes,
 * following the use case contracts.
 */
public class StaffNoteManager {

    private List<StaffNoteEventReceiver> eventReceivers = new ArrayList<>();
    private final StaffManager staffManager; // A reference to get staff information if needed

    /**
     * Constructs a StaffNoteManager.
     * @param staffManager A reference to the main StaffManager.
     */
    public StaffNoteManager(StaffManager staffManager) {
        this.staffManager = staffManager;
    }

    /**
     * Adds an event receiver to be notified of staff note-related changes.
     * @param receiver The event receiver to add.
     */
    public void addEventReceiver(StaffNoteEventReceiver er) {
        eventReceivers.add(er);
    }

    /**
     * Removes an event receiver.
     * @param receiver The event receiver to remove.
     */
    public void removeEventReceiver(StaffNoteEventReceiver er) {
        eventReceivers.remove(er);
    }

    // Notifaction Methods

    private void notifyStaffNoteCreated(StaffNote n) {
        for (StaffNoteEventReceiver r : eventReceivers) r.updateStaffNoteCreated(n);
    }

    private void notifyStaffNoteUpdated(StaffNote n) {
        for (StaffNoteEventReceiver r : eventReceivers) r.updateStaffNoteUpdated(n);
    }
    
    private void notifyStaffNoteDeleted(StaffNote n) {
        for (StaffNoteEventReceiver r : eventReceivers) r.updateStaffNoteDeleted(n);
    }

    // Business Logic Methods

    /**
     * Creates a new staff note and persists it to the database.
     * @param owner The user creating the note.
     * @param worker The staff member the note is about.
     * @param description The content of the note.
     * @param date The date of the note.
     * @return The created and saved StaffNote, or null if saving fails.
     */
    public StaffNote createStaffNote(User owner, Staff worker, String description, Date date) {
        StaffNote n = StaffNote.create(owner, worker, description, date);
        if (n.save()) { // Saves the note to the DB
            notifyStaffNoteCreated(n);
            return n;
        }
        return null;
    }

    /**
     * Updates an existing staff note.
     * Corresponds to UC6a: modificaNotaPersonale.
     * @param worker The staff member who the note belongs to (for verification).
     * @param note The note to be updated.
     * @param newDescription The new text for the note.
     * @param newDate The new date for the note.
     * @throws StaffNoteException if the note does not belong to the specified worker.
     */
    public void updateStaffNote(Staff worker, StaffNote note, String newDescription, Date newDate) throws StaffNoteException {
        if (!note.getStaff().equals(worker)) {
            throw new StaffNoteException("Exception: the note does not belong to the specified worker.");
        }
        note.setDescription(newDescription);
        note.setDate(newDate);
        if (note.update()) {
            notifyStaffNoteUpdated(note);
        }
    }

    /**
     * Deletes a staff note.
     * @param worker The staff member who the note belongs to (for verification).
     * @param note The note to be deleted.
     * @throws StaffNoteException if the note does not belong to the specified worker.
     */
    public void deleteStaffNote(Staff worker, StaffNote note) throws StaffNoteException {
        if (!note.getStaff().equals(worker)) {
            throw new StaffNoteException("Exception: the note does not belong to the specified worker.");
        }
        if (note.delete()) {
            notifyStaffNoteDeleted(note);
        }
    }

    /**
     * Retrieves all notes associated with a specific staff member.
     * @param worker The staff member whose notes are to be retrieved.
     * @return A list of all staff notes for the given worker.
     */
    public List<StaffNote> getNotesFor(Staff worker) {
        if (worker == null) return new ArrayList<>();
        return StaffNote.loadAllFor(worker);
    }

    /**
     * Finds a staff member in the local list by their serial number.
     * @param serialNumber The serial number to search for.
     * @return The Staff object if found in the list, otherwise null.
     */
    public Staff getStaffBySerialNumber(int serialNumber) {
        return staffManager.getStaff(serialNumber);
    }
}