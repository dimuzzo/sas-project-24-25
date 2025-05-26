package catering.businesslogic.staff;

public interface StaffEventReceiver {
    void updateStaffAdded(Staff s);
    void updateStaffRemoved(Staff s);
    void updateRoleCreated(Role rl);
    void updateRoleDeleted(Role rl);
    void updateRoleAssigned(Role rl);
    void updateStaffDataListCreated(StaffDataList sdl);
    void updateStaffDataListDeleted(StaffDataList sdl);
    void updateStaffDataAdded(Staff s, StaffDataList sdl);
    void updateStaffDataUpdated(Staff s, StaffDataList sdl);
    void updateStaffDataDeleted(Staff s, StaffDataList sdl);
}
