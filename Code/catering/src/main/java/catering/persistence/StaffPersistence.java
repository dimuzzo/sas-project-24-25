/**
 * * @author Alessandro Demo, Matricola 1049825
 */
package catering.persistence;

import java.sql.SQLException;

import catering.businesslogic.staff.Role;
import catering.businesslogic.staff.RoleException;
import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffDataList;
import catering.businesslogic.staff.StaffEventReceiver;

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
        try {
            Role.create(rl);
        } catch (SQLException e) {
            // In a real application, a more robust error handling would be implemented
            e.printStackTrace();
        }
    }

    @Override
    public void updateRoleDeleted(Role rl) {
        try {
            rl.delete();
        } catch (RoleException e) {
            // Business rule violation (e.g., trying to delete an assigned role)
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateRoleAssigned(Role rl) {
        // Both the role (with its new staff_id) and the staff member (now unavailable) must be updated.
        rl.getStaff().update();
        rl.update();
    }

    @Override
    public void updateRoleUnassigned(Role rl, Staff s) {
        // Both the role (now unassigned) and the old worker (now available) must be updated.
        s.update();
        rl.update();
    }

    @Override
    public void updateRoleReassigned(Role rl, Staff s) {
        // The old worker (now available), the new worker (now unavailable), and the role (with a new staff_id) must all be updated.
        s.update();
        rl.getStaff().update(); // rl.getStaff() now returns the new worker
        rl.update();
    }

    @Override
    public void updateStaffDataListCreated(StaffDataList sdl) {
        // This event is fired when a list is created for a user session.
        // In this architecture, creating the list does not automatically persist any associations.
        // Associations are persisted one by one via the updateStaffDataAdded event.
    }

    @Override
    public void updateStaffDataListDeleted(StaffDataList sdl) {
        // This event deletes all associations for a specific owner.
        sdl.deleteAllAssociations();
    }

    @Override
    public void updateStaffDataAdded(Staff s, StaffDataList sdl) {
        // A staff member was added to the list, so we persist the new association.
        sdl.addStaffAssociation(s);
    }

    @Override
    public void updateStaffDataUpdated(Staff s, StaffDataList sdl) {
        // A staff member's data was updated, so we persist the changes to the Staff entity.
        s.update();
    }

    @Override
    public void updateStaffDataDeleted(Staff s, StaffDataList sdl) {
        // A staff member was removed from the list, so we persist the removal of the association.
        sdl.removeStaffAssociation(s);
    }
}