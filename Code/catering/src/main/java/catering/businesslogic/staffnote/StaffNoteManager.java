package catering.businesslogic.staffnote;

import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffNoteManager {
    private List<StaffNoteEventReceiver> receivers = new ArrayList<>();
    private final StaffManager staffManager;  // riferimento a StaffManager

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

    // getStaff delegato a StaffManager con id
    public Staff getStaff(int id) {
        return staffManager.getStaff(id);
    }

    public StaffNote createStaffNote(Staff worker, String description, Date date) {
        StaffNote n = new StaffNote();
        n.setWorker(worker);
        n.setDescription(description);
        n.setDate(date);
        notifyStaffNoteCreated(n);
        return n;
    }
}
