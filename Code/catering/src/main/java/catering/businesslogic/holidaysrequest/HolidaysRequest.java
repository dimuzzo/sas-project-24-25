package catering.businesslogic.holidaysrequest;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import catering.businesslogic.staff.Staff;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;

public class HolidaysRequest {

    private int id;
    private Staff worker;
    private Date period;
    private boolean isAssigned;
    private final User owner;

    private HolidaysRequest(User owner, Staff worker, Date period, boolean isAssigned) {
        this.id = 0; // Una richiesta non salvata ha id 0
        this.owner = owner;
        this.worker = worker;
        this.period = period;
        this.isAssigned = isAssigned;
    }

    public static HolidaysRequest create(User owner, Staff worker, Date period, boolean isAssigned) {
        return new HolidaysRequest(owner, worker, period, isAssigned);
    }

    // Getter
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

    // Setter solo per worker e period, NON per owner
    public void setWorker(Staff worker) {
        this.worker = worker;
    }

    public void setPeriod(Date period) {
        this.period = period;
    }

    public void setAssigned(boolean assigned) {
        this.isAssigned = assigned;
    }

    // Controlla se l'utente è proprietario della richiesta ferie
    public boolean isOwner(User user) {
        return this.owner != null && this.owner.equals(user);
    }

    // METODI DI PERSISTENZA
    public boolean save() {
        if (this.id != 0) return false; // Già salvata
        String query = "INSERT INTO HolidaysRequest (worker_id, period, owner_id) VALUES (?, ?, ?)";
        int rows = PersistenceManager.executeUpdate(query, this.worker.getSerialNumber(), this.period, this.owner.getId());
        if (rows > 0) {
            this.id = PersistenceManager.getLastId();
            return true;
        }
        return false;
    }

    public boolean update() {
        if (this.id == 0) return false; // Mai salvata
        String query = "UPDATE HolidaysRequest SET worker_id = ?, period = ?, is_assigned = ? WHERE id = ?";
        int assignedInt = this.isAssigned ? 1 : 0;
        int rows = PersistenceManager.executeUpdate(query, this.worker.getSerialNumber(), this.period, assignedInt, this.id);
        return rows > 0;
    }

    public boolean delete() {
        if (this.id == 0) return false;
        String query = "DELETE FROM HolidaysRequest WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, this.id);
        return rows > 0;
    }

    public static HolidaysRequest load(int requestId) {
        final HolidaysRequest[] result = new HolidaysRequest[1];
        String query = "SELECT * FROM HolidaysRequest WHERE id = ?";
        
        PersistenceManager.executeQuery(query, rs -> {
            result[0] = fromResultSet(rs);
        }, requestId);

        return result[0];
    }

    private static HolidaysRequest fromResultSet(ResultSet rs) throws SQLException {
        User owner = User.load(rs.getInt("owner_id"));
        Staff worker = Staff.loadStaff(rs.getInt("worker_id"));
        
        HolidaysRequest hr = new HolidaysRequest(owner, worker, rs.getDate("period"), rs.getBoolean("is_assigned"));
        hr.id = rs.getInt("id");
        return hr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HolidaysRequest that = (HolidaysRequest) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
