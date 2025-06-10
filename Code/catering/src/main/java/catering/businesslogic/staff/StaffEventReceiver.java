package catering.businesslogic.staff;

public interface StaffEventReceiver {
    public void updateStaffAdded(Staff s);
    public void updateStaffRemoved(Staff s);
    public void updateRoleCreated(Role rl);
    public void updateRoleDeleted(Role rl);
    public void updateRoleAssigned(Role rl);
    public void updateRoleUnassigned(Role rl, Staff s); 
    public void updateRoleReassigned(Role rl, Staff s); 
    public void updateStaffDataListCreated(StaffDataList sdl);
    public void updateStaffDataListDeleted(StaffDataList sdl);
    public void updateStaffDataAdded(Staff s, StaffDataList sdl);
    public void updateStaffDataUpdated(Staff s, StaffDataList sdl);
    public void updateStaffDataDeleted(Staff s, StaffDataList sdl);
}
