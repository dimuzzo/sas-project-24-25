package catering.persistence;

import catering.businesslogic.staff.*;

public class StaffPersistence implements StaffEventReceiver {

    @Override
    public void updateStaffAdded(Staff s) {
        s.save();
    }

    @Override
    public void updateStaffRemoved(Staff s) {
        s.delete();
    }

    @Override
    public void updateRoleCreated(Role rl) {
        rl.save();
    }

    @Override
    public void updateRoleDeleted(Role rl) {
        rl.delete();
    }

    @Override
    public void updateRoleAssigned(Role rl) {
        rl.update();
    }

    @Override
    public void updateStaffDataListCreated(StaffDataList sdl) {
    }

    @Override
    public void updateStaffDataListDeleted(StaffDataList sdl) {
    }

    @Override
    public void updateStaffDataAdded(Staff s, StaffDataList sdl) {

    }

    @Override
    public void updateStaffDataUpdated(Staff s, StaffDataList sdl) {

    }

    @Override
    public void updateStaffDataDeleted(Staff s, StaffDataList sdl) {

    }
}
