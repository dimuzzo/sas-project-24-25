package catering.businesslogic.staff;

import java.util.Objects;

public class Staff {

    // Attributi principali
    private final int serialNumber;  // identificatore unico e immutabile
    private String name;
    private String email;
    private String phoneNumber;
    private String taxCode;
    private String primaryMansion;
    private boolean available;
    private boolean permanent;

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

    // ========================
    // Override di utilità
    // ========================

    @Override
    public String toString() {
        return name + " (" + email + "), SN: " + serialNumber;
    }

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
}
