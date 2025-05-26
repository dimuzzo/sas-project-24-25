package catering.businesslogic.holidays;

import catering.businesslogic.user.User;
import catering.businesslogic.staff.Staff;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import catering.util.LogManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Holidays {

    // Attributi basati sul DCD [1] e contratti [5]
    private Staff worker;
    private Date period; // DCD specifica Date [1], contratto gestisciRichiestaFeriePersonale specifica testo [5]
    private User owner;  // Definito nel metodo create nel DCD [1]

    // Metodo di creazione/costruttore come indicato nel DCD [1]
    // "È stata creata un’istanza f di ferie" [5] - contratto gestisciRichiestaFeriePersonale
    public static Holidays create(User owner, Staff worker, Date period) {
        Holidays holidays = new Holidays();
        holidays.owner = owner;
        holidays.worker = worker;
        holidays.period = period; // f.period = periodo [5]
        // f appartiene a p [5] - questa relazione sarà gestita altrove
        return holidays;
    }

    // Getter basati sul DCD [1]
    public Staff getStaff() {
        return worker;
    }

    public Date getPeriod() {
        return period;
    }

    // Setter basati sul DCD [1]
    public void setWorker(Staff worker) {
        this.worker = worker;
    }

    public void setPeriod(Date period) {
        this.period = period;
    }

    // Metodo isOwner() basato sul DCD [1]
    public boolean isOwner(User user) {
        // Logica per verificare se l'utente fornito è l'owner della richiesta ferie
        return this.owner != null && this.owner.equals(user);
    }

    // Nota: I metodi assegnaRichiestaFeriePersonale [6] ed eliminaRichiestaFeriePersonale [6]
    // sarebbero chiamati su istanze di Ferie tramite un gestore (Manager),
    // e potrebbero modificare lo stato di disponibilità del lavoratore (p.disponibile = no) [6].
}
