package catering.businesslogic.staff;

import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * The Staff class represents a staff member entity in the CatERing system.
 * It holds personal information and availability status for a worker.
 * This class also contains the logic for its own database persistence.
 */
public class Staff {

    private int serialNumber;  // The unique and immutable identifier for the staff member.
    private String name; // The full name of the staff member.
    private String email; // The contact email address.
    private String phoneNumber; // The contact phone number.
    private String taxCode; // The staff member's tax code (codice fiscale).
    private String primaryMansion; // The primary job title or role (e.g., "Cameriere", "Barista").
    private boolean available; // Indicates if the staff member is currently available for assignments.
    private boolean permanent; // Indicates if the staff member is a permanent employee or occasional.

    /**
     * Private empty constructor used for loading an object from the database via persistence methods.
     */
    private Staff() {
    }

    /**
     * Main constructor for creating a new Staff instance.
     * The serial number is assigned only once at creation time.
     * @param serialNumber The unique serial number.
     * @param name The staff member's full name.
     * @param email The contact email.
     * @param phoneNumber The contact phone number.
     * @param taxCode The tax code.
     * @param primaryMansion The primary job title.
     * @param permanent True for a permanent employee, false for an occasional worker.
     */
    public Staff(int serialNumber, String name, String email, String phoneNumber,
                 String taxCode, String primaryMansion, boolean permanent) {
        this.serialNumber = serialNumber;
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.taxCode = Objects.requireNonNull(taxCode, "Tax code cannot be null");
        this.primaryMansion = primaryMansion;
        this.permanent = permanent;
        this.available = true;  // A new worker is available by default.
    }

    // Getters and Setters

    public int getSerialNumber() {
        return serialNumber;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public String getPrimaryMansion() {
        return primaryMansion;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = Objects.requireNonNull(taxCode, "Tax code cannot be null");
    }

    public void setPrimaryMansion(String primaryMansion) {
        this.primaryMansion = primaryMansion;
    }

    public void setAvailability(boolean availability) {
        this.available = availability;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    // Persistence Methods

    /**
     * Private helper method to create a Staff instance from a a database row.
     * @param rs The ResultSet positioned on the correct row.
     * @return A populated Staff object.
     * @throws SQLException if a database access error occurs.
     */
    private static Staff fromResultSet(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.serialNumber = rs.getInt("serial_number");
        s.name = rs.getString("name");
        s.email = rs.getString("email");
        s.phoneNumber = rs.getString("phone_number");
        s.taxCode = rs.getString("tax_code");
        s.primaryMansion = rs.getString("primary_mansion");
        s.available = rs.getInt("available") == 1; 
        s.permanent = rs.getInt("permanent") == 1;
        return s;
    }

    /**
     * Loads all staff members from the database.
     * @return An ArrayList of all Staff members.
     */
    public static ArrayList<Staff> loadAllStaff() {
        ArrayList<Staff> staffList = new ArrayList<>();

        String query = "SELECT * FROM Staff";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Staff s = fromResultSet(rs);
                staffList.add(s);
            }
        });

        return staffList;
    }

    /**
     * Loads a single staff member from the database by their serial number.
     * @param serialNumber The serial number of the staff to load.
     * @return The loaded Staff object, or null if not found.
     */
    public static Staff loadStaff(int serialNumber) {
        final Staff[] resultHolder = new Staff[1];
        String query = "SELECT * FROM Staff WHERE serial_number = ?";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                resultHolder[0] = fromResultSet(rs);
            }
        }, serialNumber);

        return resultHolder[0];
    }

    /**
     * Saves a new Staff instance to the database. This should only be called once.
     * @return true if the operation was successful, false otherwise.
     */
    public boolean save() {       
        String query = "INSERT INTO Staff (serial_number, name, email, phone_number, tax_code, primary_mansion, available, permanent) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            int availableInt = this.available ? 1 : 0;
            int permanentInt = this.permanent ? 1 : 0;
            int rows = PersistenceManager.executeUpdate(query, serialNumber, name, email, phoneNumber, taxCode, primaryMansion, availableInt, permanentInt);
            return rows > 0;
        } catch (Exception e) {
            System.err.println("Error while saving staff: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing Staff member's data in the database.
     * @return true if the operation was successful, false otherwise.
     */
    public boolean update() {
        if (serialNumber == 0) return false;

        String query = "UPDATE Staff SET name = ?, email = ?, phone_number = ?, tax_code = ?, " +
                "primary_mansion = ?, available = ?, permanent = ? WHERE serial_number = ?";
        try {
            int availableInt = this.available ? 1 : 0;
            int permanentInt = this.permanent ? 1 : 0;

            int rows = PersistenceManager.executeUpdate(query, name, email, phoneNumber, taxCode,
                    primaryMansion, availableInt, permanentInt, serialNumber);
            return rows > 0;
        } catch (Exception e) {
            System.err.println("Error while updating staff: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes this Staff member from the database.
     * @return true if the operation was successful, false otherwise.
     */
    public boolean delete() {
        if (serialNumber == 0) return false;

        String query = "DELETE FROM Staff WHERE serial_number = ?";
        try {
            int rows = PersistenceManager.executeUpdate(query, serialNumber);
            if (rows > 0) {
                serialNumber = 0; // Resets the internal state to prevent reuse
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error while deleting staff: " + e.getMessage());
            return false;
        }
    }

    // Object Overrides

    /**
     * Checks for equality based on the unique serial number.
     * @param o The object to compare with.
     * @return true if the objects represent the same staff member.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Staff)) return false;
        Staff other = (Staff) o;
        return serialNumber == other.serialNumber;
    }

    /**
     * Generates a hash code based on the unique serial number.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(serialNumber);
    }

    @Override
    public String toString() {
        return name + " (" + email + "), Serial Number: " + serialNumber;
    }
}