package catering.businesslogic.staff;

import catering.businesslogic.user.User;
import catering.businesslogic.summaryform.SummaryForm;
import catering.businesslogic.staffnote.StaffNote;
import catering.businesslogic.holidays.Holidays;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class StaffManager {
    private List<StaffEventReceiver> receivers = new ArrayList<>();
    private StaffDataList staffDataList = new StaffDataList();
    private SummaryForm summaryForm;

    public void addReceiver(StaffEventReceiver er) {
        receivers.add(er);
    }

    public void removeReceiver(StaffEventReceiver er) {
        receivers.remove(er);
    }

    public void notifyStaffAdded(Staff s) {
        for (StaffEventReceiver r : receivers) r.updateStaffAdded(s);
    }

    public void notifyStaffRemoved(Staff s) {
        for (StaffEventReceiver r : receivers) r.updateStaffRemoved(s);
    }

    public void notifyRoleCreated(Role rl) {
        for (StaffEventReceiver r : receivers) r.updateRoleCreated(rl);
    }

    public void notifyRoleDeleted(Role rl) {
        for (StaffEventReceiver r : receivers) r.updateRoleDeleted(rl);
    }

    public void notifyRoleAssigned(Role rl) {
        for (StaffEventReceiver r : receivers) r.updateRoleAssigned(rl);
    }

    public void notifyStaffDataListCreated(StaffDataList sdl) {
        for (StaffEventReceiver r : receivers) r.updateStaffDataListCreated(sdl);
    }

    public void notifyStaffDataListDeleted(StaffDataList sdl) {
        for (StaffEventReceiver r : receivers) r.updateStaffDataListDeleted(sdl);
    }

    public void notifyStaffDataAdded(Staff s, StaffDataList sdl) {
        for (StaffEventReceiver r : receivers) r.updateStaffDataAdded(s, sdl);
    }

    public void notifyStaffDataDeleted(Staff s, StaffDataList sdl) {
        for (StaffEventReceiver r : receivers) r.updateStaffDataDeleted(s, sdl);
    }

    /**
     * Cerca uno staff nella lista tramite serialNumber.
     */
    public Staff getStaffBySerialNumber(int serialNumber) {
        for (Staff s : staffDataList.getStaff()) {
            if (s.getSerialNumber() == serialNumber) {
                return s;
            }
        }
        return null;
    }

    public StaffDataList getStaffDataList() {
        return staffDataList;
    }

    public SummaryForm getSummaryForm() {
        return summaryForm;
    }

    public Role createRole(Staff worker, String name, String description, Date date, boolean assigned) {
        Role rl = new Role();
        rl.setWorker(worker);
        rl.setName(name);
        rl.setDescription(description);
        rl.setDate(date);
        rl.setAssigned(assigned);
        notifyRoleCreated(rl);
        return rl;
    }

    public void assignRole(Staff worker, Role rl) {
        rl.setWorker(worker);
        rl.setAssigned(true);
        notifyRoleAssigned(rl);
    }

    public void addStaffNote(StaffNote n) {
        // logica per associare nota a staff
    }

    public void addHolidays(Holidays h) {
        // logica per associare ferie a staff
    }
}
