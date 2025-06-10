/**
 * * @author Alessandro Demo, Matricola 1049825
 */
package catering.persistence;

import catering.businesslogic.staffnote.StaffNote;
import catering.businesslogic.staffnote.StaffNoteEventReceiver;

/**
 * Handles database persistence for StaffNote objects by listening to events
 * from a {@link catering.businesslogic.staffnote.StaffNoteManager}.
 */
public class StaffNotePersistence implements StaffNoteEventReceiver {

    @Override
    public void updateStaffNoteCreated(StaffNote n) {
        // When the manager creates a note, we save it.
        n.save();
    }

    @Override
    public void updateStaffNoteUpdated(StaffNote n) {
        // When the manager updates a note, we persist the changes.
        n.update();
    }

    @Override
    public void updateStaffNoteDeleted(StaffNote n) {
        // When the manager deletes a note, we delete it from the DB.
        n.delete();
    }
}