package catering.persistence;

import catering.businesslogic.staffnote.StaffNote;
import catering.businesslogic.staffnote.StaffNoteEventReceiver;

public class StaffNotePersistence implements StaffNoteEventReceiver {

    @Override
    public void updateStaffNoteCreated(StaffNote n) {
        // Quando il manager crea una nota, noi la salviamo.
        n.save();
    }

    @Override
    public void updateStaffNoteUpdated(StaffNote n) {
        n.update();
    }

    @Override
    public void updateStaffNoteDeleted(StaffNote n) {
        // Quando il manager cancella una nota, noi la cancelliamo dal DB.
        n.delete();
    }
}