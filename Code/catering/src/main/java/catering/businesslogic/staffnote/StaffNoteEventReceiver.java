package catering.businesslogic.staffnote;

public interface StaffNoteEventReceiver {
    void updateStaffNoteCreated(StaffNote n);
    void updateStaffNoteUpdated(StaffNote n);
    void updateStaffNoteDeleted(StaffNote n);
}
