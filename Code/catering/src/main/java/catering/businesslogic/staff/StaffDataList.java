package catering.businesslogic.staff;

import catering.businesslogic.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StaffDataList {

    private List<Staff> staffDataList;
    private User owner;

    // ========================
    // Creazione
    // ========================

    /**
     * Factory method per creare una nuova StaffDataList per un determinato owner.
     */
    public static StaffDataList create(User owner) {
        StaffDataList dataList = new StaffDataList();
        dataList.staffDataList = new ArrayList<>();
        dataList.owner = Objects.requireNonNull(owner, "Owner cannot be null");
        return dataList;
    }

    // ========================
    // Accessor e controllo ownership
    // ========================

    /**
     * Verifica se l'utente specificato è l'owner di questa lista.
     */
    public boolean isOwner(User user) {
        return this.owner != null && this.owner.equals(user);
    }

    /**
     * Restituisce la lista completa del personale.
     */
    public List<Staff> getStaff() {
        return staffDataList;
    }

    // ========================
    // Operazioni CRUD
    // ========================

    /**
     * Inserisce un nuovo membro del personale, se non già presente.
     */
    public boolean insertStaffData(Staff s) {
        if (s != null && !staffDataList.contains(s)) {
            return staffDataList.add(s);
        }
        return false;
    }

    /**
     * Aggiorna i dati del personale esistente nella lista.
     */
    public boolean updateStaffData(Staff s, String name, String newEmail, int newPhoneNumber,
                                   String taxCode, String newPrimaryMansion,
                                   boolean availability, boolean permanent) {
        if (staffDataList.contains(s)) {
            s.setName(name);
            s.setEmail(newEmail);
            s.setPhoneNumber(newPhoneNumber);
            s.setTaxCode(taxCode);
            s.setPrimaryMansion(newPrimaryMansion);
            s.setAvailability(availability);
            s.setPermanent(permanent);
            return true;
        }
        return false;
    }

    /**
     * Rimuove un membro del personale dalla lista.
     */
    public boolean removeStaffData(Staff s) {
        return staffDataList.remove(s);
    }

    // ========================
    // Filtri
    // ========================

    /**
     * Restituisce una lista di personale in base alla disponibilità.
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
     * Restituisce una lista di personale in base al tipo di contratto (permanent o temporaneo).
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

    // ========================
    // Eliminazione dell'intera lista (opzionale)
    // ========================

    /*
    public void delete() {
        staffDataList.clear();
        owner = null;
    }
    */
}
