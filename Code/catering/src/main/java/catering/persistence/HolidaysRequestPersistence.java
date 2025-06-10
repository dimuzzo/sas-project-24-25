package catering.persistence;

import catering.businesslogic.holidaysrequest.HolidaysRequest;
import catering.businesslogic.holidaysrequest.HolidaysRequestEventReceiver;
import catering.businesslogic.staff.Staff;

/**
 * Handles database persistence for HolidaysRequest objects by listening to events.
 */
public class HolidaysRequestPersistence implements HolidaysRequestEventReceiver {

    @Override
    public void updateHolidaysRequestCreated(HolidaysRequest hr) {
        // When the manager creates a request, we save it to the database.
        hr.save();
    }

    @Override
    public void updateHolidaysRequestAssigned(HolidaysRequest hr) {
        // When the manager assigns a request, we update its status and the
        // worker's availability in the database.
        Staff worker = hr.getWorker();
        hr.update();
        if (worker != null) {
            worker.update();
        }
    }

    @Override
    public void updateHolidaysRequestDeleted(HolidaysRequest hr) {
        // When the manager deletes a request, we delete it from the database.
        hr.delete();
    }
}