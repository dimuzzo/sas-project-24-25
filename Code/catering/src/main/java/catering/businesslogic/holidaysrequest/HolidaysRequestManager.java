package catering.businesslogic.holidaysrequest;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffManager;
import catering.businesslogic.user.User;

public class HolidaysRequestManager {
    private List<HolidaysRequestEventReceiver> eventReceivers = new ArrayList<>();
    private final StaffManager staffManager;

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

    private void notifyHolidaysRequestAssigned(HolidaysRequest hr) {
        for (HolidaysRequestEventReceiver r : eventReceivers) r.updateHolidaysRequestAssigned(hr);
    }

    private void notifyHolidaysRequestDeleted(HolidaysRequest hr) {
        for (HolidaysRequestEventReceiver r : eventReceivers) r.updateHolidaysRequestDeleted(hr);
    }

    public Staff getStaffBySerialNumber(int serialNumber) {
        return staffManager.getStaff(serialNumber);
    }

    // Il create necessita dell'owner per creare l'oggetto, quindi la sua firma rimane così
    public HolidaysRequest createHolidaysRequest(User owner, Staff worker, Date period) {
        HolidaysRequest hr = HolidaysRequest.create(owner, worker, period, false);
        if (hr.save()) {
            notifyHolidaysRequestCreated(hr);
            return hr;
        }
        return null;
    }

    // MODIFICATO: La firma ora rispecchia il contratto 8a
    public void assignHolidaysRequest(Staff worker, HolidaysRequest hr) throws HolidaysRequestException {
        if (!hr.getWorker().equals(worker)) {
            throw new HolidaysRequestException("Incoerenza: la richiesta non appartiene al lavoratore specificato.");
        }
        if (hr.isAssigned()) {
            throw new HolidaysRequestException("La richiesta di ferie risulta gia' assegnata.");
        }
        if (!worker.isAvailable()) {
            throw new HolidaysRequestException("Il lavoratore non e' disponibile.");
        }

        hr.setAssigned(true);
        worker.setAvailability(false);

        hr.update();
        worker.update();

        notifyHolidaysRequestAssigned(hr);
    }

    // MODIFICATO: La firma ora rispecchia il contratto 8b
    public void deleteHolidaysRequest(Staff worker, HolidaysRequest hr) throws HolidaysRequestException {
        if (!hr.getWorker().equals(worker)) {
            throw new HolidaysRequestException("Incoerenza: la richiesta non appartiene al lavoratore specificato.");
        }
        if (hr.delete()) {
            notifyHolidaysRequestDeleted(hr);
        }
    }
}