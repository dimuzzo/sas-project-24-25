package catering.businesslogic.staff;

import catering.businesslogic.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StaffDataList {

    private List<StaffEventReceiver> receivers;
    private List<Staff> staffDataList;
    private User owner;

    // ========================
    // Creazione
    // ========================

    public static StaffDataList create(User owner) {
        StaffDataList dataList = new StaffDataList();
        dataList.staffDataList = new ArrayList<>();
        dataList.receivers = new ArrayList<>();
        dataList.owner = Objects.requireNonNull(owner, "Owner cannot be null");
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

    public List<Staff> getStaff() {
        return staffDataList;
    }

    // ========================
    // Operazioni CRUD
    // ========================

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
        if (getStaffBySerialNumber(serialNumber) != null) {
            return false; // già presente
        }

        Staff s = new Staff(serialNumber, name, email, phoneNumber, taxCode, primaryMansion, permanent);
        s.setAvailability(available);

        if (s.save()) {  // salva nel DB
            boolean added = staffDataList.add(s);
            if (added) {
                notifyStaffDataAdded(s);
            }
            return added;
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
        Staff existing = getStaffBySerialNumber(s.getSerialNumber());
        if (existing != null) {
            existing.setName(name);
            existing.setEmail(newEmail);
            existing.setPhoneNumber(newPhoneNumber);
            existing.setTaxCode(taxCode);
            existing.setPrimaryMansion(newPrimaryMansion);
            existing.setAvailability(availability);
            existing.setPermanent(permanent);
            boolean updated = existing.update(); // aggiorna il DB
            if (updated) {
                notifyStaffDataUpdated(existing);
            }
            return updated;
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

    private Staff getStaffBySerialNumber(int serialNumber) {
        for (Staff s : staffDataList) {
            if (s.getSerialNumber() == serialNumber) {
                return s;
            }
        }
        return null;
    }
}
