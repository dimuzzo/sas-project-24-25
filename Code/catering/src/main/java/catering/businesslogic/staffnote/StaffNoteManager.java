package catering.businesslogic.staffnote;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffManager;
import catering.businesslogic.user.User;

public class StaffNoteManager {
    private List<StaffNoteEventReceiver> eventReceivers = new ArrayList<>();
    private final StaffManager staffManager;

    public StaffNoteManager(StaffManager staffManager) {
        this.staffManager = staffManager;
    }

    public void addEventReceiver(StaffNoteEventReceiver er) {
        eventReceivers.add(er);
    }

    public void removeEventReceiver(StaffNoteEventReceiver er) {
        eventReceivers.remove(er);
    }

    private void notifyStaffNoteCreated(StaffNote n) {
        for (StaffNoteEventReceiver r : eventReceivers) r.updateStaffNoteCreated(n);
    }

    // Aggiunto per il caso d'uso 6a
    private void notifyStaffNoteUpdated(StaffNote n) {
        for (StaffNoteEventReceiver r : eventReceivers) r.updateStaffNoteUpdated(n);
    }
    
    private void notifyStaffNoteDeleted(StaffNote n) {
        for (StaffNoteEventReceiver r : eventReceivers) r.updateStaffNoteDeleted(n);
    }

    public Staff getStaffBySerialNumber(int serialNumber) {
        return staffManager.getStaff(serialNumber);
    }

    public List<StaffNote> getNotesFor(Staff worker) {
        if (worker == null) return new ArrayList<>();
        return StaffNote.loadAllFor(worker);
    }

    public StaffNote createStaffNote(User owner, Staff worker, String description, Date date) {
        StaffNote n = StaffNote.create(owner, worker, description, date);
        if (n.save()) { // Salva la nota nel DB
            notifyStaffNoteCreated(n);
            return n;
        }
        return null; // Ritorna null se il salvataggio fallisce
    }

    public void updateStaffNote(Staff worker, StaffNote note, String newDescription, Date newDate) throws StaffNoteException {
        if (!note.getStaff().equals(worker)) {
            throw new StaffNoteException("Eccezione: la nota non appartiene al lavoratore specificato.");
        }
        note.setDescription(newDescription);
        note.setDate(newDate);
        if (note.update()) {
            notifyStaffNoteUpdated(note);
        }
    }

    public void deleteStaffNote(Staff worker, StaffNote note) throws StaffNoteException {
        if (!note.getStaff().equals(worker)) {
            throw new StaffNoteException("Eccezione: la nota non appartiene al lavoratore specificato.");
        }
        if (note.delete()) {
            notifyStaffNoteDeleted(note);
        }
    }
}