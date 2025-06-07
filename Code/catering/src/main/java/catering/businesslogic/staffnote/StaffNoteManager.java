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

    private void notifyStaffNoteDeleted(StaffNote n) {
        for (StaffNoteEventReceiver r : eventReceivers) r.updateStaffNoteDeleted(n);
    }

    public Staff getStaffBySerialNumber(int serialNumber) {
        return staffManager.getStaff(serialNumber);
    }

    public StaffNote createStaffNote(User owner, Staff worker, String description, Date date) {
        StaffNote n = StaffNote.create(owner, worker, description, date);
        notifyStaffNoteCreated(n);
        return n;
    }
}
