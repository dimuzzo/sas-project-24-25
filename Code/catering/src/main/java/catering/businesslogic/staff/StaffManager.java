package catering.businesslogic.staff;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import catering.businesslogic.holidaysrequest.HolidaysRequest;
import catering.businesslogic.staffnote.StaffNote;
import catering.businesslogic.summaryform.SummaryForm;
import catering.businesslogic.user.User;

/**
 * Handles all staff-related operations.
 * This class acts as the main controller for use cases involving staff members,
 * roles, assignments, and other related data.
 */
public class StaffManager {
    // A list of external listeners (e.g., the UI) to be notified of data changes.
    private final List<StaffEventReceiver> eventReceivers = new ArrayList<>();
    // A reference to the user performing the operations. It's final as it should not change during the session.
    private final User currentUser;

    // The list of staff data, initialized in the constructor.
    private final StaffDataList staffDataList;
 
    private SummaryForm summaryForm;
    private final Map<Staff, List<StaffNote>> staffNotesMap = new HashMap<>();
    private final Map<Staff, List<HolidaysRequest>> staffHolidaysRequestMap = new HashMap<>();

    /**
     * StaffManager constructor.
     * This is the only way to create an instance, ensuring it is always associated with a valid user.
     * @param currentUser The user who is currently operating the system. Cannot be null.
     */
    public StaffManager(User currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("CurrentUser cannot be null for StaffManager");
        }

        if (!currentUser.isOrganizer()) {
            throw new RoleException("Only organizers can manage staff");
        }

        this.currentUser = currentUser;
        
        // Initializes the staff list using the correct factory method.
        // This loads data from the DB and sets the current user as the owner.
        this.staffDataList = StaffDataList.create(this.currentUser);
    }
    
    
    /**
     * Adds an event receiver to be notified of staff-related changes.
     * @param receiver The event receiver to add.
     */
    public void addEventReceiver(StaffEventReceiver er) {
        eventReceivers.add(er);
    }

    /**
     * Removes an event receiver.
     * @param receiver The event receiver to remove.
     */
    public void removeEventReceiver(StaffEventReceiver er) {
        eventReceivers.remove(er);
    }
    
    // Notifaction Methods

    public void notifyStaffAdded(Staff s) {
        for (StaffEventReceiver r : eventReceivers) r.updateStaffAdded(s);
    }

    public void notifyStaffRemoved(Staff s) {
        for (StaffEventReceiver r : eventReceivers) r.updateStaffRemoved(s);
    }

    public void notifyRoleCreated(Role rl) {
        for (StaffEventReceiver r : eventReceivers) r.updateRoleCreated(rl);
    }

    public void notifyRoleDeleted(Role rl) {
        for (StaffEventReceiver r : eventReceivers) r.updateRoleDeleted(rl);
    }

    public void notifyRoleAssigned(Role rl) {
        for (StaffEventReceiver r : eventReceivers) r.updateRoleAssigned(rl);
    }

    private void notifyRoleUnassigned(Role rl, Staff s) {
        for (StaffEventReceiver r : eventReceivers) r.updateRoleUnassigned(rl, s);
    }
    
    private void notifyRoleReassigned(Role rl, Staff s) {
        for (StaffEventReceiver r : eventReceivers) r.updateRoleReassigned(rl, s);
    }

    public void notifyStaffDataListCreated(StaffDataList sdl) {
        for (StaffEventReceiver r : eventReceivers) r.updateStaffDataListCreated(sdl);
    }

    public void notifyStaffDataListDeleted(StaffDataList sdl) {
        for (StaffEventReceiver r : eventReceivers) r.updateStaffDataListDeleted(sdl);
    }

    public void notifyStaffDataAdded(Staff s, StaffDataList sdl) {
        for (StaffEventReceiver r : eventReceivers) r.updateStaffDataAdded(s, sdl);
    }

    public void notifyStaffDataUpdated(Staff s, StaffDataList sdl) {
        for (StaffEventReceiver r : eventReceivers) r.updateStaffDataUpdated(s, sdl);
    }

    public void notifyStaffDataDeleted(Staff s, StaffDataList sdl) {
        for (StaffEventReceiver r : eventReceivers) r.updateStaffDataDeleted(s, sdl);
    }
    
    // Business Logic Methods
    
    /**
     * Search for a staff in the list by serialNumber.
     */
    public Staff getStaff(int serialNumber) {
        return staffDataList.getStaffDataList().stream()
                .filter(s -> s.getSerialNumber() == serialNumber)
                .findFirst()
                .orElse(null);
    }

    public StaffDataList getStaffDataList() {
        return staffDataList;
    }

    public SummaryForm getSummaryForm() {
        return summaryForm;
    }

    /**
     * Adds a new staff member to the system.
     * The logic is delegated to StaffDataList, which handles persistence.
     * @param serialNumber The unique serial number for the new staff.
     * @param name The name of the staff member.
     * @param permanent True if the staff member is permanent, false if occasional.
     * @return true if the staff member was added successfully, false otherwise.
     */
    public boolean addStaff(int serialNumber, String name, String email, String phoneNumber,
                            String taxCode, String primaryMansion, boolean permanent) {
        // The Staff constructor only requires 'permanent'. 'available' is set to true by default.
        boolean added = staffDataList.insertStaffData(serialNumber, name, email, phoneNumber, taxCode, primaryMansion, true, permanent);
        return added;
    }

    /**
     * Removes a staff member from the system.
     * @param s The Staff object to remove.
     * @return true if the staff member was removed successfully, false otherwise.
     */
    public boolean removeStaff(Staff s) {
        boolean removed = staffDataList.removeStaffData(s);
        return removed;
    }

    /**
     * Creates a new role.
     * @param name The name of the role.
     * @param description A description for the role.
     * @param date The date associated with the role.
     * @return The newly created Role object.
     */
    public Role createRole(Staff worker, String name, String description, Date date) {
        Role rl = Role.create(name, description, date);
        if (worker != null) {
            rl.setWorker(worker);
        }
        notifyRoleCreated(rl);
        return rl;
    }

    /**
     * Assigns a role to an available staff member.
     * @param worker The staff member to assign the role to.
     * @param rl The role to be assigned.
     * @throws RoleException if pre-conditions are not met (e.g., worker not available).
     */
    public void assignRole(Staff worker, Role rl) {
        if (worker == null || rl == null) {
            throw new RoleException("Worker and Role cannot be null for assignment.");
        }
        if (!worker.isAvailable() || rl.isAssigned()) {
            throw new RoleException("Worker not available or role already assigned.");
        }
        worker.setAvailability(false);
        rl.setWorker(worker);
        notifyRoleAssigned(rl);
    }

    /**
     * Deletes a role assignment, making the staff member available again.
     * @param worker The staff member currently assigned.
     * @param role The role to be unassigned.
     * @throws RoleException if the provided worker and role do not match the assignment.
     */
    public void deleteRoleAssignment(Staff worker, Role role) {
        // Pre-conditions
        if (role == null || worker == null || !worker.equals(role.getStaff()) || worker.isAvailable() || !role.isAssigned()) {
            throw new RoleException("Pre-conditions for deleting assignment not met.");
        }
        Staff staffToUpdate = role.getStaff();

        // Post-conditions
        role.setWorker(null);           
        staffToUpdate.setAvailability(true);
        notifyRoleUnassigned(role, staffToUpdate);
    }

    /**
     * Modifies an assignment by reassigning a role to a new worker.
     * Scenario 1: "change worker".
     * @param newWorker The new staff member who will receive the role. Must be available.
     * @param role The role to be reassigned. It must already be assigned.
     * @throws RoleException if pre-conditions are not met.
     */
    public void modifyAssignmentByChangingWorker(Staff newWorker, Role role) {
        // Pre-conditions
        if (role == null || !role.isAssigned() || newWorker == null || !newWorker.isAvailable()) {
            throw new RoleException("Pre-condizioni per cambiare lavoratore non soddisfatte.");
        }
        Staff oldWorker = role.getStaff();
        
        // Post-conditions
        oldWorker.setAvailability(true); 
        newWorker.setAvailability(false);
        role.setWorker(newWorker);
        notifyRoleReassigned(role, oldWorker);
    }

    /**
     * Modifies an assignment by giving a new role to an already assigned worker.
     * Scenario 2: "change role".
     * @param worker The worker whose role assignment is being changed.
     * @param oldRole The worker's current role, which will become unassigned.
     * @param newRole The new role to be assigned to the worker. It must be unassigned.
     * @throws RoleException if pre-conditions are not met.
     */
    public void modifyAssignmentByChangingRole(Staff worker, Role oldRole, Role newRole) {
        // Pre-conditions
        if (worker == null || oldRole == null || !oldRole.getStaff().equals(worker) || newRole == null || newRole.isAssigned()) {
            throw new RoleException("Pre-conditions for changing role not met.");
        }
        
        // Post-conditions
        oldRole.setWorker(null);     
        newRole.setWorker(worker);   
        
        notifyRoleUnassigned(oldRole, worker);
        notifyRoleAssigned(newRole);
    }

    /**
     * Adds a staff note to the in-memory map for the current session.
     * @param n The StaffNote to add.
     */
    public void addStaffNote(StaffNote n) {
        Staff staff = getStaff(n.getStaff().getSerialNumber());
        if (staff == null) {
            System.out.println("Staff not found for note.");
            return;
        }
        staffNotesMap.computeIfAbsent(staff, k -> new ArrayList<>()).add(n);
    }

    /**
     * Adds a holiday request to the in-memory map for the current session.
     * @param hr The HolidaysRequest to add.
     */
    public void addHolidaysRequest(HolidaysRequest hr) {
        Staff staff = getStaff(hr.getWorker().getSerialNumber());
        if (staff == null) {
            System.out.println("Staff not found for holidays.");
            return;
        }
        staffHolidaysRequestMap.computeIfAbsent(staff, k -> new ArrayList<>()).add(hr);
    }

    /**
     * Sets the permanent status of a staff member and persists the change.
     * @param worker The staff member to modify.
     * @param isPermanent The new permanent status.
     * @throws IllegalArgumentException if the worker is null.
     * @throws RuntimeException if the database update fails.
     */
    public void setPermanentStatus(Staff worker, boolean isPermanent) {
        if (worker == null) {
            throw new IllegalArgumentException("Worker cannot be null.");
        }
        
        // Modify the object's state in memory
        worker.setPermanent(isPermanent);
        
        // Directly save the changes to the database
        boolean updated = worker.update();
        if (!updated) {
            throw new RuntimeException("Failed to update staff permanent status in database");
        }
        
        // Notify the system that the data has changed (for UI updates, etc.)
        staffDataList.notifyStaffDataUpdated(worker);
    }
}