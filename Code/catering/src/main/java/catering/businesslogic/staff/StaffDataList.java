package catering.businesslogic.staff;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;

public class StaffDataList {

    private List<StaffEventReceiver> receivers;
    private List<Staff> staffDataList;
    private User owner;

    // ========================
    // Creazione e caricamento da DB
    // ========================

    public static StaffDataList create(User owner) {
        StaffDataList dataList = new StaffDataList();
        dataList.owner = Objects.requireNonNull(owner, "Owner cannot be null");
        dataList.receivers = new ArrayList<>();
        // Carica i dati dal DB
        dataList.staffDataList = Staff.loadAllStaff();
        return dataList;
    }

    // ========================
    // Gestione receivers
    // ========================

    public void addReceiver(StaffEventReceiver receiver) {
        if (this.receivers == null) {
            this.receivers = new ArrayList<>();
        }
        this.receivers.add(receiver);
    }

    private void notifyStaffDataAdded(Staff s) {
        if (receivers != null) {
            for (StaffEventReceiver r : receivers) {
                r.updateStaffDataAdded(s, this);
            }
        }
    }

    private void notifyStaffDataUpdated(Staff s) {
        if (receivers != null) {
            for (StaffEventReceiver r : receivers) {
                r.updateStaffDataUpdated(s, this);
            }
        }
    }

    private void notifyStaffDataDeleted(Staff s) {
        if (receivers != null) {
            for (StaffEventReceiver r : receivers) {
                r.updateStaffDataDeleted(s, this);
            }
        }
    }

    // ========================
    // Accessor e controllo ownership
    // ========================

    public boolean isOwner(User user) {
        return this.owner != null && this.owner.equals(user);
    }

    public List<Staff> getStaffDataList() {
        return new ArrayList<>(staffDataList);
    }

    /**
     * Restituisce l'utente proprietario di questa lista.
     * Questo metodo è necessario per la classe StaffPersistence.
     * @return L'utente proprietario.
     */
    public User getOwner() {
        return this.owner;
    }

    // ========================
    // Operazioni CRUD
    // ========================

    public boolean save() {
        if (owner == null || staffDataList == null) {
            return false;
        }
        // Ipotizziamo di voler salvare l'associazione per ogni membro dello staff nella lista
        String query = "INSERT INTO StaffDataList (owner_id, staff_serial_number) VALUES (?, ?)";
        try {
            for (Staff staff : this.staffDataList) {
                // Esegui un insert per ogni riga di associazione
                PersistenceManager.executeUpdate(query, owner.getId(), staff.getSerialNumber());
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error while saving staff data list: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(){
        if(!isOwner(owner)) return false;
        String query = "DELETE FROM StaffDataList WHERE owner_id = ?";
        try {
            int rows = PersistenceManager.executeUpdate(query, owner.getId());
            return true;
        } catch (Exception e) {
            System.err.println("Error while deleting staff: " + e.getMessage());
            return false;
        }
    }

    public boolean tryInsertStaff(User currentUser, int serialNumber, String name, String email, String phoneNumber,
                                  String taxCode, String primaryMansion, boolean available, boolean permanent) {
        if (!isOwner(currentUser)) {
            System.out.println("Error: the user is not authorized, he's not the owner of the list.");
            return false;
        }
        return insertStaffData(serialNumber, name, email, phoneNumber, taxCode, primaryMansion, available, permanent);
    }

    public boolean insertStaffData(int serialNumber, String name, String email, String phoneNumber,
                                   String taxCode, String primaryMansion,
                                   boolean available, boolean permanent) {
        if (getStaff(serialNumber) != null) {
            return false; // già presente
        }

        Staff s = new Staff(serialNumber, name, email, phoneNumber, taxCode, primaryMansion, permanent);
        s.setAvailability(available);

        if (s.save()) {  // salva nel DB
            staffDataList.add(s);
            notifyStaffDataAdded(s);
            return true;
        }
        return false;
    }

    public boolean tryUpdateStaff(User currentUser, Staff s, String name, String newEmail, String newPhoneNumber,
                                  String taxCode, String newPrimaryMansion,
                                  boolean availability, boolean permanent) {
        if (!isOwner(currentUser)) {
            System.out.println("Error: the user is not authorized, he's not the owner of the list.");
            return false;
        }
        return updateStaffData(s, name, newEmail, newPhoneNumber, taxCode, newPrimaryMansion, availability, permanent);
    }

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
            if (existing.update()) { // aggiorna il DB
                notifyStaffDataUpdated(existing);
                return true;
            }
        }
        return false;
    }

    public boolean tryRemoveStaff(User currentUser, Staff s) {
        if (!isOwner(currentUser)) {
            System.out.println("Error: the user is not authorized, he's not the owner of the list.");
            return false;
        }
        return removeStaffData(s);
    }

    public boolean removeStaffData(Staff s) {
        if (s.delete()) {  // prima cancella dal DB
            boolean removed = staffDataList.removeIf(existing -> existing.getSerialNumber() == s.getSerialNumber());
            if (removed) {
                notifyStaffDataDeleted(s);
            }
            return removed;
        }
        return false;
    }

    // METODI DI PERSISTENZA PURA
    // Questi vengono chiamati solo da StaffPersistence per evitare ricorsione.
    
    /**
     * Crea nel database l'associazione tra l'owner di questa lista e un membro dello staff.
     * NON invia notifiche.
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
     * Rimuove dal database l'associazione tra l'owner di questa lista e un membro dello staff.
     * NON invia notifiche.
     */
    public boolean removeStaffAssociation(Staff staff) {
        if (owner != null && staff != null) {
            String query = "DELETE FROM StaffDataList WHERE owner_id = ? AND staff_serial_number = ?";
            int rows = PersistenceManager.executeUpdate(query, owner.getId(), staff.getSerialNumber());
            return rows > 0;
        }
        return false;
    }

    // ========================
    // Filtri
    // ========================

    public List<Staff> getAvailable(boolean availability) {
        List<Staff> result = new ArrayList<>();
        for (Staff s : staffDataList) {
            if (s.isAvailable() == availability) {
                result.add(s);
            }
        }
        return result;
    }

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
    // Utility interna
    // ========================

    private Staff getStaff(int serialNumber) {
        for (Staff s : staffDataList) {
            if (s.getSerialNumber() == serialNumber) {
                return s;
            }
        }
        return null;
    }
}
