package catering.businesslogic.staff;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;

/**
 * Represents a list of staff members managed by a specific owner (User).
 * This class acts as a repository for an organizer's session, holding an in-memory
 * list of staff and handling business logic for adding, updating, and removing them.
 */
public class StaffDataList {

    private List<StaffEventReceiver> receivers;
    private List<Staff> staffDataList;
    private User owner;

    /**
     * Factory method to create a new StaffDataList for a specific owner.
     * Note: This implementation loads ALL existing staff members from the database,
     * not just those associated with the owner.
     * @param owner The User who owns this list. Must not be null.
     * @return A new instance of StaffDataList.
     */
    public static StaffDataList create(User owner) {
        StaffDataList dataList = new StaffDataList();
        dataList.owner = Objects.requireNonNull(owner, "Owner cannot be null");
        dataList.receivers = new ArrayList<>();
        // Loads all staff from the DB upon creation
        dataList.staffDataList = Staff.loadAllStaff();
        return dataList;
    }

    /**
     * Adds an event receiver to be notified of staff data list-related changes.
     * @param receiver The event receiver to add.
     */
    public void addReceiver(StaffEventReceiver receiver) {
        if (this.receivers == null) {
            this.receivers = new ArrayList<>();
        }
        this.receivers.add(receiver);
    }

    // Notifaction Methods

    public void notifyStaffDataAdded(Staff s) {
        if (receivers != null) {
            for (StaffEventReceiver r : receivers) {
                r.updateStaffDataAdded(s, this);
            }
        }
    }

    public void notifyStaffDataUpdated(Staff s) {
        if (receivers != null) {
            for (StaffEventReceiver r : receivers) {
                r.updateStaffDataUpdated(s, this);
            }
        }
    }

    public void notifyStaffDataDeleted(Staff s) {
        if (receivers != null) {
            for (StaffEventReceiver r : receivers) {
                r.updateStaffDataDeleted(s, this);
            }
        }
    }

    /**
     * Checks if the given user is the owner of this list.
     * @param user The user to check.
     * @return true if the user is the owner, false otherwise.
     */
    public boolean isOwner(User user) {
        return this.owner != null && this.owner.equals(user);
    }

    /**
     * Returns a safe copy of the internal staff list.
     * @return A new list containing the staff members.
     */
    public List<Staff> getStaffDataList() {
        return new ArrayList<>(staffDataList);
    }

    /**
     * Returns the owner of this list.
     * This method is needed by the StaffPersistence class.
     * @return The owner User object.
     */
    public User getOwner() {
        return this.owner;
    }

    // Business Logic Methods

    /**
     * A safe gateway for inserting a new staff member. It checks for user authorization first.
     * @throws StaffDataListException if the current user is not the owner of the list.
     */
    public boolean tryInsertStaff(User currentUser, int serialNumber, String name, String email, String phoneNumber,
                                  String taxCode, String primaryMansion, boolean available, boolean permanent) throws StaffDataListException {
        if (!isOwner(currentUser)) {
            throw new StaffDataListException("User is not authorized to modify this list as it is not an organizer.");
        }
        return insertStaffData(serialNumber, name, email, phoneNumber, taxCode, primaryMansion, available, permanent);
    }

    /**
     * Inserts a new staff member into the system. This involves saving the Staff
     * entity and then updating the in-memory list.
     * @return true if the staff member was created and saved successfully, false otherwise.
     */
    public boolean insertStaffData(int serialNumber, String name, String email, String phoneNumber,
                                   String taxCode, String primaryMansion,
                                   boolean available, boolean permanent) {
        if (getStaff(serialNumber) != null) {
            return false; // already exists
        }

        // The Staff constructor only requires 'permanent'. 'available' is set afterwards.
        Staff s = new Staff(serialNumber, name, email, phoneNumber, taxCode, primaryMansion, permanent);
        s.setAvailability(available);

        if (s.save()) { // saves to the DB
            staffDataList.add(s);
            // This notification is used by StaffPersistence to save the association in the StaffDataList table
            notifyStaffDataAdded(s);
            return true;
        }
        return false;
    }

    /**
     * A safe gateway for updating a staff member. It checks for user authorization first.
     * @throws StaffDataListException if the current user is not the owner of the list.
     */
    public boolean tryUpdateStaff(User currentUser, Staff s, String name, String newEmail, String newPhoneNumber,
                                  String taxCode, String newPrimaryMansion,
                                  boolean availability, boolean permanent) throws StaffDataListException {
        if (!isOwner(currentUser)) {
            throw new StaffDataListException("User is not authorized to modify this list as it is not an organizer.");
        }
        return updateStaffData(s, name, newEmail, newPhoneNumber, taxCode, newPrimaryMansion, availability, permanent);
    }

    /**
     * Updates an existing staff member's information.
     * @return true if the staff member was found and updated successfully, false otherwise.
     */
    public boolean updateStaffData(Staff s, String name, String newEmail, String newPhoneNumber,
                                   String taxCode, String newPrimaryMansion,
                                   boolean availability, boolean permanent) {
        Staff existing = getStaff(s.getSerialNumber());
        if (existing != null) {
            existing.setName(name);
            existing.setEmail(newEmail);
            existing.setPhoneNumber(newPhoneNumber);
            existing.setTaxCode(taxCode);
            existing.setPrimaryMansion(newPrimaryMansion);
            existing.setAvailability(availability);
            existing.setPermanent(permanent);
            if (existing.update()) { // updates the DB
                notifyStaffDataUpdated(existing);
                return true;
            }
        }
        return false;
    }

    /**
     * A safe gateway for removing a staff member. It checks for user authorization first.
     * @throws StaffDataListException if the current user is not the owner of the list.
     */
    public boolean tryRemoveStaff(User currentUser, Staff s) throws StaffDataListException {
        if (!isOwner(currentUser)) {
            throw new StaffDataListException("User is not authorized to modify this list as it is not an organizer.");
        }
        return removeStaffData(s);
    }

    /**
     * Removes a staff member completely from the system.
     * This deletes the staff member from the main Staff table and removes them from the in-memory list.
     * @param s The Staff object to remove.
     * @return true if the staff member was deleted successfully, false otherwise.
     */
    public boolean removeStaffData(Staff s) {
        if (s.delete()) {  // first, delete from the DB
            boolean removed = staffDataList.removeIf(existing -> existing.getSerialNumber() == s.getSerialNumber());
            if (removed) {
                notifyStaffDataDeleted(s);
            }
            return removed;
        }
        return false;
    }

    // Pure Persistence Methods
    
    /**
     * Creates the association in the database between this list's owner and a staff member.
     * This method does NOT fire any events to prevent recursive loops.
     * @param staff The staff member to associate.
     * @return true if the operation is successful.
     */
    public boolean addStaffAssociation(Staff staff) {
        if (owner != null && staff != null) {
            String query = "INSERT INTO StaffDataList (owner_id, staff_serial_number) VALUES (?, ?)";
            int rows = PersistenceManager.executeUpdate(query, owner.getId(), staff.getSerialNumber());
            return rows > 0;
        }
        return false;
    }

    /**
     * Removes the association in the database between this list's owner and a staff member.
     * This method does NOT fire any events.
     * @param staff The staff member to disassociate.
     * @return true if the operation is successful.
     */
    public boolean removeStaffAssociation(Staff staff) {
        if (owner != null && staff != null) {
            String query = "DELETE FROM StaffDataList WHERE owner_id = ? AND staff_serial_number = ?";
            int rows = PersistenceManager.executeUpdate(query, owner.getId(), staff.getSerialNumber());
            return rows > 0;
        }
        return false;
    }

    /**
     * Deletes all staff associations for this list's owner from the database.
     * This is a bulk operation.
     * @return true if the operation was successful, false otherwise.
     */
    public boolean deleteAllAssociations() {
        if (owner == null) return false;
        String query = "DELETE FROM StaffDataList WHERE owner_id = ?";
        try {
            PersistenceManager.executeUpdate(query, owner.getId());
            return true;
        } catch (Exception e) {
            System.err.println("Error while deleting all staff data list associations: " + e.getMessage());
            return false;
        }
    }

    // Filter Methods

    /**
     * Filters the current list to find staff members by their availability.
     * @param availability The availability status to filter by.
     * @return A new list containing only the matching staff members.
     */
    public List<Staff> getAvailable(boolean availability) {
        List<Staff> result = new ArrayList<>();
        for (Staff s : staffDataList) {
            if (s.isAvailable() == availability) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Filters the current list to find staff members by their employment type.
     * @param permanent The permanent status to filter by.
     * @return A new list containing only the matching staff members.
     */
    public List<Staff> getPermanent(boolean permanent) {
        List<Staff> result = new ArrayList<>();
        for (Staff s : staffDataList) {
            if (s.isPermanent() == permanent) {
                result.add(s);
            }
        }
        return result;
    }

    // Internal Utilities

    /**
     * Finds a staff member in the local list by their serial number.
     * @param serialNumber The serial number to search for.
     * @return The Staff object if found in the list, otherwise null.
     */
    private Staff getStaff(int serialNumber) {
        for (Staff s : staffDataList) {
            if (s.getSerialNumber() == serialNumber) {
                return s;
            }
        }
        return null;
    }
}