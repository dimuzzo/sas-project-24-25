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

    public static StaffDataList create(User owner) {
        StaffDataList dataList = new StaffDataList();
        dataList.staffDataList = new ArrayList<>();
        dataList.owner = Objects.requireNonNull(owner, "Owner cannot be null");
        return dataList;
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

    public boolean insertStaffData(Staff s) {
        if (s != null && getStaffBySerialNumber(s.getSerialNumber()) == null) {
            return staffDataList.add(s);
        }
        return false;
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
            return true;
        }
        return false;
    }

    public boolean removeStaffData(Staff s) {
        return staffDataList.removeIf(existing -> existing.getSerialNumber() == s.getSerialNumber());
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
