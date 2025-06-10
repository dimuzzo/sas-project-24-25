package catering.businesslogic.holidaysrequest;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffManager;
import catering.businesslogic.user.User;

/**
 * Manages all business logic related to holiday requests.
 * It acts as a controller for creating, assigning (approving), and deleting requests.
 */
public class HolidaysRequestManager {
    private List<HolidaysRequestEventReceiver> eventReceivers = new ArrayList<>();
    private final StaffManager staffManager; // A reference to get staff information if needed

    /**
     * Constructs a StaffNoteManager.
     * @param staffManager A reference to the main StaffManager.
     */
    public HolidaysRequestManager(StaffManager staffManager) {
        this.staffManager = staffManager;
    }

    /**
     * Adds an event receiver to be notified of holidays request-related changes.
     * @param receiver The event receiver to add.
     */
    public void addEventReceiver(HolidaysRequestEventReceiver er) {
        eventReceivers.add(er);
    }

    /**
     * Removes an event receiver.
     * @param receiver The event receiver to remove.
     */
    public void removeEventReceiver(HolidaysRequestEventReceiver er) {
        eventReceivers.remove(er);
    }

    // Notifaction Methods

    private void notifyHolidaysRequestCreated(HolidaysRequest hr) {
        for (HolidaysRequestEventReceiver r : eventReceivers) r.updateHolidaysRequestCreated(hr);
    }

    private void notifyHolidaysRequestAssigned(HolidaysRequest hr) {
        for (HolidaysRequestEventReceiver r : eventReceivers) r.updateHolidaysRequestAssigned(hr);
    }

    private void notifyHolidaysRequestDeleted(HolidaysRequest hr) {
        for (HolidaysRequestEventReceiver r : eventReceivers) r.updateHolidaysRequestDeleted(hr);
    }

    /**
     * Creates a new holiday request and triggers its persistence.
     * Corresponds to UC8.
     * @return The created HolidaysRequest, or null if saving fails.
     */    
    public HolidaysRequest createHolidaysRequest(User owner, Staff worker, Date period) {
        HolidaysRequest hr = HolidaysRequest.create(owner, worker, period, false);
        if (hr.save()) {
            notifyHolidaysRequestCreated(hr);
            return hr;
        }
        return null;
    }

    /**
     * Assigns (approves) a holiday request, updating the request and the worker's availability.
     * @param worker The worker associated with the request.
     * @param hr The holiday request to be assigned.
     * @throws HolidaysRequestException if preconditions are not met.
     */
    public void assignHolidaysRequest(Staff worker, HolidaysRequest hr) throws HolidaysRequestException {
        // Precondition checks
        if (!hr.getWorker().equals(worker)) {
            throw new HolidaysRequestException("The request does not belong to the specified worker.");
        }
        if (hr.isAssigned()) {
            throw new HolidaysRequestException("Exception: This holiday request has already been assigned.");
        }
        if (!worker.isAvailable()) {
            throw new HolidaysRequestException("The worker is not available.");
        }

        hr.setAssigned(true);
        worker.setAvailability(false);

        hr.update();
        worker.update();

        notifyHolidaysRequestAssigned(hr);
    }

    /**
     * Deletes a holiday request.
     * @param worker The worker associated with the request.
     * @param hr The holiday request to be deleted.
     * @throws HolidaysRequestException if the request does not belong to the specified worker.
     */
    public void deleteHolidaysRequest(Staff worker, HolidaysRequest hr) throws HolidaysRequestException {
        if (!hr.getWorker().equals(worker)) {
            throw new HolidaysRequestException("The request does not belong to the specified worker.");
        }
        if (hr.delete()) {
            notifyHolidaysRequestDeleted(hr);
        }
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