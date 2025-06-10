package catering.businesslogic.staffnote;

/**
 * An interface for receiving events related to StaffNote management.
 * Classes implementing this can listen to creation, update, and deletion events.
 */
public interface StaffNoteEventReceiver {
    /**
     * Called when a new staff note has been created.
     * @param n The newly created {@link StaffNote}.
     */
    void updateStaffNoteCreated(StaffNote n);

    /**
     * Called when an existing staff note has been modified.
     * @param n The modified {@link StaffNote}.
     */
    void updateStaffNoteUpdated(StaffNote n);

    /**
     * Called when a staff note has been deleted.
     * @param n The {@link StaffNote} that was deleted.
     */
    void updateStaffNoteDeleted(StaffNote n);
}