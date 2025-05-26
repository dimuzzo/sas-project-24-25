package catering.businesslogic.holidays;

import catering.businesslogic.user.User;
import catering.businesslogic.staff.Staff;

import java.sql.Date;
import java.util.Objects;

public class Holidays {

    // Attributi principali secondo il DCD
    private Staff worker;
    private Date period;
    private final User owner;

    // Costruttore privato per forzare uso di create()
    public Holidays(User owner, Staff worker, Date period) {
        this.owner = owner;
        this.worker = worker;
        this.period = period;
    }

    // Factory method conforme al DCD e contratto use case
    public static Holidays create(User owner, Staff worker, Date period) {
        return new Holidays(owner, worker, period);
    }

    // Getter
    public Staff getWorker() {
        return worker;
    }

    public Date getPeriod() {
        return period;
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

    // Controlla se l'utente è proprietario della richiesta ferie
    public boolean isOwner(User user) {
        return this.owner != null && this.owner.equals(user);
    }

    // equals e hashCode (opzionali, ma utili se gestisci liste o mappe di ferie)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Holidays)) return false;
        Holidays holidays = (Holidays) o;
        return Objects.equals(worker, holidays.worker) &&
                Objects.equals(period, holidays.period) &&
                Objects.equals(owner, holidays.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worker, period, owner);
    }
}
