package catering.businesslogic.staffnote;

import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffManager;
import catering.businesslogic.user.User;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class StaffNoteManager {
    private List<StaffNoteEventReceiver> receivers = new ArrayList<>();
    private final StaffManager staffManager;

    public StaffNoteManager(StaffManager staffManager) {
        this.staffManager = staffManager;
    }

    public void addReceiver(StaffNoteEventReceiver er) {
        receivers.add(er);
    }

    public void removeReceiver(StaffNoteEventReceiver er) {
        receivers.remove(er);
    }

    private void notifyStaffNoteCreated(StaffNote n) {
        for (StaffNoteEventReceiver r : receivers) r.updateStaffNoteCreated(n);
    }

    private void notifyStaffNoteDeleted(StaffNote n) {
        for (StaffNoteEventReceiver r : receivers) r.updateStaffNoteDeleted(n);
    }

    public Staff getStaffBySerialNumber(int serialNumber) {
        return staffManager.getStaffBySerialNumber(serialNumber);
    }

    public StaffNote createStaffNote(User owner, Staff worker, String description, Date date) {
        StaffNote n = StaffNote.create(owner, worker, description, date);
        notifyStaffNoteCreated(n);
        return n;
    }
}
