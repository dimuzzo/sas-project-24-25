package catering.businesslogic.staffnote;

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

public class StaffNote {

    // Attributi basati sul DCD [2] e contratti [3]
    private Staff worker;
    private String description;
    private Date date;
    private User owner; // Definito nel metodo create nel DCD [8] e contratto [3]

    // Metodo di creazione/costruttore come indicato nel DCD [8] e contratto [3]
    // "È stata creata un’istanza np di Nota Personale" [3]
    public static StaffNote create(User owner, Staff worker, String description, Date date) {
        StaffNote note = new StaffNote();
        note.owner = owner;
        note.worker = worker;
        note.description = description; // np.descrizione = descrizione [3]
        note.date = date;             // np.data = data [3]
        // np appartiene a p [3] - questa relazione sarà gestita altrove, ad esempio dal Manager
        return note;
    }

    // Getter basati sul DCD [8]
    public Staff getStaff() {
        return worker;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    // Setter basati sul DCD [8] e contratti [4]
    // Nota: il setter per worker nel DCD [8] è setWorker, ma il getter è getStaff.
    // Adotto la nomenclatura del setter dal DCD.
    public void setWorker(Staff worker) {
        this.worker = worker; // np appartiene a p (nuovo) - logica da gestire altrove
    }

    public void setDescription(String newDescription) {
        this.description = newDescription; // np.descrizione = nuovaDescrizione [4]
    }

    public void setDate(Date date) {
        this.date = date; // Questo setter non è esplicitamente nel contratto modificaNotaPersonale [4] ma è nel DCD [8]
    }

    // Metodo isOwner() basato sul DCD [8]
    public boolean isOwner(User user) {
        // Logica per verificare se l'utente fornito è l'owner della nota
        return this.owner != null && this.owner.equals(user);
    }

    // Nota: I metodi modificaNotaPersonale [4] ed eliminaNotaPersonale [4]
    // sarebbero chiamati su istanze di Nota Personale tramite un gestore (Manager).
}
