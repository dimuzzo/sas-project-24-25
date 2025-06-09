package catering.persistence;

import catering.businesslogic.holidaysrequest.HolidaysRequest;
import catering.businesslogic.holidaysrequest.HolidaysRequestEventReceiver;
import catering.businesslogic.staff.Staff;

public class HolidaysRequestPersistence implements HolidaysRequestEventReceiver {

    @Override
    public void updateHolidaysRequestCreated(HolidaysRequest hr) {
        // Quando il manager crea una richiesta, noi la salviamo.
        hr.save();
    }

    @Override
    public void updateHolidaysRequestAssigned(HolidaysRequest hr) {
        // Quando il manager assegna una richiesta, noi aggiorniamo il suo stato
        // e lo stato del lavoratore nel database.
        Staff worker = hr.getWorker();
        hr.update();
        if (worker != null) {
            worker.update();
        }
    }

    @Override
    public void updateHolidaysRequestDeleted(HolidaysRequest hr) {
        // Quando il manager cancella una richiesta, noi la cancelliamo dal DB.
        hr.delete();
    }
}