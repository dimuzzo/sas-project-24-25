/**
 * * @author Alessandro Demo, Matricola 1049825
 */
package catering.businesslogic.holidaysrequest;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import catering.businesslogic.staff.Staff;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;

/**
 * The HolidaysRequest class represents a holiday/leave request made by a staff member.
 * It holds details about the worker, the requested period, and its approval status.
 * This class also encapsulates the logic for its own database persistence.
 */
public class HolidaysRequest {

    private int id; // The unique identifier for the holidays request in the database. It is set only after saving.
    private Staff worker;
    private Date period;
    private boolean isAssigned; // 'Assigned' here means 'approved'
    private final User owner;

    /**
     * Private constructor to enforce object creation through the factory method.
     * @param owner The user who creates/manages the request.
     * @param worker The staff member requesting the holiday.
     * @param period The date of the requested leave.
     * @param isAssigned The approval status.
     */
    private HolidaysRequest(User owner, Staff worker, Date period, boolean isAssigned) {
        this.id = 0; // An unsaved request has an ID of 0
        this.owner = owner;
        this.worker = worker;
        this.period = period;
        this.isAssigned = isAssigned;
    }

    /**
     * Factory method for creating a new HolidaysRequest.
     * @param owner The user creating the request.
     * @param worker The staff member this request is for.
     * @param period The date of the leave.
     * @param isAssigned The initial approval status (typically false).
     * @return A new instance of HolidaysRequest.
     */
    public static HolidaysRequest create(User owner, Staff worker, Date period, boolean isAssigned) {
        return new HolidaysRequest(owner, worker, period, isAssigned);
    }

    // Getters and Setters

    public int getId() { 
        return id; 
    }

    public Staff getWorker() {
        return worker;
    }

    public Date getPeriod() {
        return period;
    }

    public boolean isAssigned() {
        return isAssigned;
    }

    public User getOwner() {
        return owner;
    }

    public void setWorker(Staff worker) {
        this.worker = worker;
    }

    public void setPeriod(Date period) {
        this.period = period;
    }

    public void setAssigned(boolean assigned) {
        this.isAssigned = assigned;
    }

    /**
     * Checks if the specified user is the creator of this note.
     * @param user The user to check.
     * @return true if the user is the owner, false otherwise.
     */
    public boolean isOwner(User user) {
        return this.owner != null && this.owner.equals(user);
    }

    // Persistence Methods

    /**
     * Saves a new holiday request to the database.
     * This method should only be called once on a new instance, as it performs an INSERT operation.
     * If the object already has a database ID (id != 0), the operation is aborted.
     * @return true if the request was saved successfully and an ID was generated, false otherwise.
     */
    public boolean save() {
        if (this.id != 0) return false; // Already saved
        String query = "INSERT INTO HolidaysRequest (worker_id, period, owner_id, is_assigned) VALUES (?, ?, ?, ?)";
        int assignedInt = this.isAssigned ? 1 : 0;
        int rows = PersistenceManager.executeUpdate(query, this.worker.getSerialNumber(), this.period, this.owner.getId(), assignedInt);
        if (rows > 0) {
            this.id = PersistenceManager.getLastId();
            return true;
        }
        return false;
    }

    /**
     * Updates the current holiday request's data in the database.
     * If the request has not been saved yet (ID is 0), the operation will fail.
     * @return true if the update was successful (at least one row affected), false otherwise.
     */
    public boolean update() {
        if (this.id == 0) return false;
        String query = "UPDATE HolidaysRequest SET worker_id = ?, period = ?, is_assigned = ? WHERE id = ?";
        int assignedInt = this.isAssigned ? 1 : 0;
        int rows = PersistenceManager.executeUpdate(query, this.worker.getSerialNumber(), this.period, assignedInt, this.id);
        return rows > 0;
    }

    /**
     * Deletes this holiday request from the database.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean delete() {
        if (this.id == 0) return false;
        String query = "DELETE FROM HolidaysRequest WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, this.id);
        return rows > 0;
    }

    /**
     * Loads a single holiday request from the database by its unique ID.
     * @param requestId The unique ID of the holiday request to load.
     * @return The loaded HolidaysRequest object, or null if no request with that ID is found.
     */
    public static HolidaysRequest load(int requestId) {
        final HolidaysRequest[] result = new HolidaysRequest[1];
        String query = "SELECT * FROM HolidaysRequest WHERE id = ?";
        
        PersistenceManager.executeQuery(query, rs -> {
            result[0] = fromResultSet(rs);
        }, requestId);

        return result[0];
    }

    /**
     * Private helper method to create a HolidaysRequest instance from a database row.
     * This method maps the ResultSet columns to the object's fields.
     * @param rs The ResultSet already positioned on the row to be mapped.
     * @return A new, populated HolidaysRequest object.
     * @throws SQLException if a database access error occurs.
     */
    private static HolidaysRequest fromResultSet(ResultSet rs) throws SQLException {
        User owner = User.load(rs.getInt("owner_id"));
        Staff worker = Staff.loadStaff(rs.getInt("worker_id"));
        
        HolidaysRequest hr = new HolidaysRequest(owner, worker, rs.getDate("period"), rs.getBoolean("is_assigned"));
        hr.id = rs.getInt("id");
        return hr;
    }

    // Object Overrides

    /**
     * Compares this holiday request to another object for equality.
     * Two requests are considered equal if they have the same non-zero ID.
     * @param o The object to compare with.
     * @return true if the objects are the same holiday request, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HolidaysRequest that = (HolidaysRequest) o;
        return id == that.id && id != 0;
    }

    /**
     * Generates a hash code for this holiday request based on its unique ID.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}