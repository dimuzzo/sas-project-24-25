package catering.businesslogic.holidays;

import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffManager;
import catering.businesslogic.user.User;

import java.sql.Date;
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

    /**
     * Ottiene uno staff usando il serialNumber come chiave univoca.
     */
    public Staff getStaffBySerialNumber(int serialNumber) {
        return staffManager.getStaffBySerialNumber(serialNumber);
    }

    public Holidays createHolidays(User owner, Staff worker, Date period) {
        Holidays h = Holidays.create(owner, worker, period);
        notifyHolidaysCreated(h);
        return h;
    }
}
