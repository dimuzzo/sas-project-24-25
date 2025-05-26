package catering.businesslogic.holidays;

import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HolidaysManager {
    private List<HolidaysEventReceiver> receivers = new ArrayList<>();
    private final StaffManager staffManager;  // riferimento a StaffManager

    public HolidaysManager(StaffManager staffManager) {
        this.staffManager = staffManager;
    }

    public void addReceiver(HolidaysEventReceiver er) {
        receivers.add(er);
    }

    public void removeReceiver(HolidaysEventReceiver er) {
        receivers.remove(er);
    }

    private void notifyHolidaysCreated(Holidays h) {
        for (HolidaysEventReceiver r : receivers) r.updateHolidaysCreated(h);
    }

    private void notifyHolidaysDeleted(Holidays h) {
        for (HolidaysEventReceiver r : receivers) r.updateHolidaysDeleted(h);
    }

    // Ora getStaff delega a StaffManager
    public Staff getStaff(int id) {
        return staffManager.getStaff(id);
    }

    public Holidays createHolidays(Staff worker, Date period) {
        Holidays h = new Holidays();
        h.setWorker(worker);
        h.setPeriod(period);
        notifyHolidaysCreated(h);
        return h;
    }
}
