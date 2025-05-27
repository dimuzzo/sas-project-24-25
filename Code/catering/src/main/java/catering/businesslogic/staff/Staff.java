package catering.businesslogic.staff;

import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class Staff {

    // Attributi principali
    private int serialNumber;  // identificatore unico e immutabile
    private String name;
    private String email;
    private String phoneNumber;
    private String taxCode;
    private String primaryMansion;
    private boolean available;
    private boolean permanent;

    /**
     * Costruttore vuoto per caricamento da DB
     */
    private Staff() {
    }

    /**
     * Costruttore principale.
     * Il serialNumber viene assegnato una volta sola al momento della creazione.
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
        this.available = true;  // un nuovo lavoratore è disponibile di default
    }

    // ========================
    // Getter
    // ========================

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

    // ========================
    // Setter (solo per attributi modificabili)
    // ========================

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

    // =========================
    // Metodi di caricamento/salvataggio DB
    // =========================

    private static Staff fromResultSet(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.serialNumber = rs.getInt("serial_number");
        s.name = rs.getString("name");
        s.email = rs.getString("email");
        s.phoneNumber = rs.getString("phone_number");
        s.taxCode = rs.getString("tax_code");
        s.primaryMansion = rs.getString("primary_mansion");
        s.available = rs.getBoolean("available");
        s.permanent = rs.getBoolean("permanent");
        return s;
    }

    /**
     * Carica tutti gli Staff dal DB
     * @return lista di Staff
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

    public static Staff loadStaff(int serialNumber) {
        final Staff[] resultHolder = new Staff[1];
        String query = "SELECT * FROM Staff WHERE serial_number = ?";

        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    resultHolder[0] = fromResultSet(rs);
                }
            }
        }, serialNumber);

        return resultHolder[0];
    }

    /**
     * Salva un nuovo Staff nel DB
     * @return true se successo, false altrimenti
     */
    public boolean save() {
        String query = "INSERT INTO Staff (serial_number, name, email, phone_number, tax_code, primary_mansion, available, permanent) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            int rows = PersistenceManager.executeUpdate(query, serialNumber, name, email, phoneNumber, taxCode, primaryMansion, available, permanent);
            return rows > 0;
        } catch (Exception e) {
            System.err.println("Error while saving staff: " + e.getMessage());
            return false;
        }
    }

    public boolean update() {
        if (serialNumber == 0) return false;

        String query = "UPDATE Staff SET name = ?, email = ?, phone_number = ?, tax_code = ?, " +
                "primary_mansion = ?, available = ?, permanent = ? WHERE serial_number = ?";
        try {
            int rows = PersistenceManager.executeUpdate(query, name, email, phoneNumber, taxCode,
                    primaryMansion, available, permanent, serialNumber);
            return rows > 0;
        } catch (Exception e) {
            System.err.println("Error while updating staff: " + e.getMessage());
            return false;
        }
    }

    public boolean delete() {
        if (serialNumber == 0) return false;

        String query = "DELETE FROM Staff WHERE serial_number = ?";
        try {
            int rows = PersistenceManager.executeUpdate(query, serialNumber);
            if (rows > 0) {
                serialNumber = 0; // resetto lo stato interno
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error while deleting staff: " + e.getMessage());
            return false;
        }
    }

    // =========================
    // Override equals e hashCode
    // =========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Staff)) return false;
        Staff other = (Staff) o;
        return serialNumber == other.serialNumber;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(serialNumber);
    }

    @Override
    public String toString() {
        return name + " (" + email + "), SN: " + serialNumber;
    }
}
