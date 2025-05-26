package catering.businesslogic.staff;

import java.util.Objects;

public class Staff {

    // Attributi principali, incluso l'id gestito internamente
    private int id;
    private String name;
    private String email;
    private int phoneNumber;
    private String taxCode;
    private String primaryMansion;
    private boolean available;
    private boolean permanent;

    /**
     * Costruttore principale, usato da StaffManager per creare nuovi lavoratori.
     * L'id viene assegnato successivamente dal database al momento della persistenza.
     */
    public Staff(String name, String email, int phoneNumber, String taxCode,
                 String primaryMansion, boolean permanent) {
        this.id = 0;  // 0 = non ancora salvato nel database
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

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getPhoneNumber() {
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
    // Setter
    // ========================

    /**
     * Setter dell'id usato solo dal livello di persistenza.
     */
    void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(int phoneNumber) {
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
        return name + " (" + email + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Staff)) return false;
        Staff other = (Staff) o;
        return taxCode.equals(other.taxCode);
    }

    @Override
    public int hashCode() {
        return taxCode.hashCode();
    }
}
