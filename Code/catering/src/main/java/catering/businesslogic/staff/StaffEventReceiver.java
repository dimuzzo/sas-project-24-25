/**
 * * @author Alessandro Demo, Matricola 1049825
 */
package catering.businesslogic.staff;

/**
 * An interface for receiving events related to staff management.
 * Classes that implement this interface can register with a {@link StaffManager}
 * to be notified of changes in the business logic, such as the creation of a role
 * or the assignment of a staff member. This is typically used to trigger persistence
 * or update a user interface.
 */
public interface StaffEventReceiver {

    /**
     * Called when a new staff member has been created and added.
     * @param s The newly created {@link Staff} object.
     */
    public void updateStaffAdded(Staff s);

    /**
     * Called when a staff member has been removed.
     * @param s The {@link Staff} object that was removed.
     */
    public void updateStaffRemoved(Staff s);

    /**
     * Called when a new role has been created.
     * @param rl The newly created {@link Role} object.
     */
    public void updateRoleCreated(Role rl);

    /**
     * Called when a role has been deleted.
     * @param rl The {@link Role} object that was deleted.
     */
    public void updateRoleDeleted(Role rl);

    /**
     * Called when a role has been assigned to a staff member.
     * @param rl The {@link Role} object, which now contains the assignment details.
     */
    public void updateRoleAssigned(Role rl);

    /**
     * Called when a role assignment has been removed.
     * @param rl The {@link Role} object that is now unassigned.
     * @param s The {@link Staff} member who was previously assigned to the role.
     */
    public void updateRoleUnassigned(Role rl, Staff s); 

    /**
     * Called when a role has been reassigned from one staff member to another.
     * @param rl The {@link Role} object, which now contains the reference to the new worker.
     * @param s The old {@link Staff} member who was previously assigned to the role.
     */
    public void updateRoleReassigned(Role rl, Staff s); 

    /**
     * Called when a new {@link StaffDataList} is created for a user session.
     * @param sdl The newly created {@link StaffDataList}.
     */
    public void updateStaffDataListCreated(StaffDataList sdl);

    /**
     * Called when a {@link StaffDataList} is deleted.
     * @param sdl The {@link StaffDataList} that was deleted.
     */
    public void updateStaffDataListDeleted(StaffDataList sdl);

    /**
     * Called when a staff member is associated with a {@link StaffDataList}.
     * @param s The {@link Staff} member that was added to the list.
     * @param sdl The {@link StaffDataList} to which the member was added.
     */
    public void updateStaffDataAdded(Staff s, StaffDataList sdl);

    /**
     * Called when the data of a staff member within a list has been updated.
     * @param s The {@link Staff} member with the updated data.
     * @param sdl The {@link StaffDataList} containing the member.
     */
    public void updateStaffDataUpdated(Staff s, StaffDataList sdl);

    /**
     * Called when a staff member is disassociated from a {@link StaffDataList}.
     * @param s The {@link Staff} member that was removed from the list.
     * @param sdl The {@link StaffDataList} from which the member was removed.
     */
    public void updateStaffDataDeleted(Staff s, StaffDataList sdl);
}