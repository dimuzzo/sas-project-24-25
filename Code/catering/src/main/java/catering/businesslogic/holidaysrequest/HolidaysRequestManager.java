package catering.businesslogic.holidaysrequest;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffManager;
import catering.businesslogic.user.User;

public class HolidaysRequestManager {
    private List<HolidaysRequestEventReceiver> eventReceivers = new ArrayList<>();
    private final StaffManager staffManager;  // riferimento a StaffManager

    public HolidaysRequestManager(StaffManager staffManager) {
        this.staffManager = staffManager;
    }

    public void addEventReceiver(HolidaysRequestEventReceiver er) {
        eventReceivers.add(er);
    }

    public void removeEventReceiver(HolidaysRequestEventReceiver er) {
        eventReceivers.remove(er);
    }

    private void notifyHolidaysRequestCreated(HolidaysRequest hr) {
        for (HolidaysRequestEventReceiver r : eventReceivers) r.updateHolidaysRequestCreated(hr);
    }

    public void notifyHolidaysRequestAssigned(HolidaysRequest hr) {
        for (HolidaysRequestEventReceiver r : eventReceivers) r.updateHolidaysRequestAssigned(hr);
    }

    private void notifyHolidaysRequestDeleted(HolidaysRequest hr) {
        for (HolidaysRequestEventReceiver r : eventReceivers) r.updateHolidaysRequestDeleted(hr);
    }

    /**
     * Ottiene uno staff usando il serialNumber come chiave univoca.
     */
    public Staff getStaffBySerialNumber(int serialNumber) {
        return staffManager.getStaff(serialNumber);
    }

    public HolidaysRequest createHolidaysRequest(User owner, Staff worker, Date period, boolean isAssigned) {
        HolidaysRequest hr = HolidaysRequest.create(owner, worker, period, isAssigned);
        notifyHolidaysRequestCreated(hr);
        return hr;
    }
}
