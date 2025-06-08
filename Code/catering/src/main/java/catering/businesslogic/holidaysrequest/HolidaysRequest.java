package catering.businesslogic.holidaysrequest;

import java.sql.Date;
import java.util.Objects;

import catering.businesslogic.staff.Staff;
import catering.businesslogic.user.User;

public class HolidaysRequest {

    // Attributi principali secondo il DCD
    private Staff worker;
    private Date period;
    private boolean isAssigned;
    private final User owner;

    // Costruttore privato per forzare uso di create()
    public HolidaysRequest(User owner, Staff worker, Date period, boolean isAssigned) {
        this.owner = owner;
        this.worker = worker;
        this.period = period;
        this.isAssigned = isAssigned;
    }

    // Factory method conforme al DCD e contratto use case
    public static HolidaysRequest create(User owner, Staff worker, Date period, boolean isAssigned) {
        return new HolidaysRequest(owner, worker, period, isAssigned);
    }

    // Getter
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

    // equals e hashCode (opzionali, ma utili se gestisci liste o mappe di ferie)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HolidaysRequest)) return false;
        HolidaysRequest holidaysRequest = (HolidaysRequest) o;
        return Objects.equals(worker, holidaysRequest.worker) &&
                Objects.equals(period, holidaysRequest.period) &&
                Objects.equals(owner, holidaysRequest.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worker, period, owner);
    }
}
