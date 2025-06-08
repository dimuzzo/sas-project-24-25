package catering.businesslogic.staff;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import catering.businesslogic.holidaysrequest.HolidaysRequest;
import catering.businesslogic.staffnote.StaffNote;
import catering.businesslogic.summaryform.SummaryForm;
import catering.businesslogic.user.User;

/**
 * Gestisce tutte le operazioni relative al personale (staff).
 * Agisce come controller principale per i casi d'uso legati allo staff.
 * Questa versione è stata corretta per garantire un'inizializzazione e una gestione
 * della sessione utente corrette.
 */
public class StaffManager {
    // Lista di "ascoltatori" esterni (es. UI) da notificare quando i dati cambiano.
    private final List<StaffEventReceiver> eventReceivers = new ArrayList<>();

    // Riferimento all'utente che sta eseguendo le operazioni. È finale perché non deve cambiare.
    private final User currentUser;

    // La lista dei dati del personale. Viene inizializzata nel costruttore.
    private final StaffDataList staffDataList;
    
    // Altri attributi come da versione originale
    private SummaryForm summaryForm;
    private final Map<Staff, List<StaffNote>> staffNotesMap = new HashMap<>();
    private final Map<Staff, List<HolidaysRequest>> staffHolidaysRequestMap = new HashMap<>();

    /**
     * Costruttore di StaffManager.
     * È l'unico modo per creare un'istanza, garantendo che sia sempre associata a un utente valido.
     * @param currentUser L'utente che sta utilizzando il sistema. Non può essere null.
     */
    public StaffManager(User currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("CurrentUser cannot be null for StaffManager");
        }

        if (!currentUser.isOrganizer()) {
             throw new RoleException("Only organizers can manage staff");
        }

        this.currentUser = currentUser;
        
        // CHIAVE: Inizializza la lista del personale usando il factory method corretto.
        // Questo carica i dati dal DB e imposta l'utente come proprietario.
        this.staffDataList = StaffDataList.create(this.currentUser);
    }
    
    // METODI PER LA GESTIONE DEGLI EVENTI
    
    public void addEventReceiver(StaffEventReceiver er) {
        eventReceivers.add(er);
    }

    public void removeEventReceiver(StaffEventReceiver er) {
        eventReceivers.remove(er);
    }
    
    // METODI DI NOTIFICA

    public void notifyStaffAdded(Staff s) {
        for (StaffEventReceiver r : eventReceivers) r.updateStaffAdded(s);
    }

    public void notifyStaffRemoved(Staff s) {
        for (StaffEventReceiver r : eventReceivers) r.updateStaffRemoved(s);
    }

    public void notifyRoleCreated(Role rl) {
        for (StaffEventReceiver r : eventReceivers) r.updateRoleCreated(rl);
    }

    public void notifyRoleDeleted(Role rl) {
        for (StaffEventReceiver r : eventReceivers) r.updateRoleDeleted(rl);
    }

    public void notifyRoleAssigned(Role rl) {
        for (StaffEventReceiver r : eventReceivers) r.updateRoleAssigned(rl);
    }

    public void notifyStaffDataListCreated(StaffDataList sdl) {
        for (StaffEventReceiver r : eventReceivers) r.updateStaffDataListCreated(sdl);
    }

    public void notifyStaffDataListDeleted(StaffDataList sdl) {
        for (StaffEventReceiver r : eventReceivers) r.updateStaffDataListDeleted(sdl);
    }

    public void notifyStaffDataAdded(Staff s, StaffDataList sdl) {
        for (StaffEventReceiver r : eventReceivers) r.updateStaffDataAdded(s, sdl);
    }

    public void notifyStaffDataUpdated(Staff s, StaffDataList sdl) {
        for (StaffEventReceiver r : eventReceivers) r.updateStaffDataUpdated(s, sdl);
    }

    public void notifyStaffDataDeleted(Staff s, StaffDataList sdl) {
        for (StaffEventReceiver r : eventReceivers) r.updateStaffDataDeleted(s, sdl);
    }
    
    // METODI DI BUSINESS LOGIC
    
    /**
     * Cerca uno staff nella lista tramite serialNumber.
     * Ora funziona correttamente perché `staffDataList` è inizializzato.
     */
    public Staff getStaff(int serialNumber) {
        return staffDataList.getStaffDataList().stream()
                .filter(s -> s.getSerialNumber() == serialNumber)
                .findFirst()
                .orElse(null);
    }

    public StaffDataList getStaffDataList() {
        return staffDataList;
    }

    public SummaryForm getSummaryForm() {
        return summaryForm;
    }

    /**
     * Aggiunge un nuovo membro dello staff.
     * Non richiede più il parametro `currentUser`, ma usa quello interno.
     */
    public boolean addStaff(int serialNumber, String name, String email, String phoneNumber,
                            String taxCode, String primaryMansion, boolean available, boolean permanent) {
        // Usa `this.currentUser` per il controllo di autorizzazione.
        boolean added = staffDataList.tryInsertStaff(this.currentUser, serialNumber, name, email, phoneNumber, taxCode, primaryMansion, available, permanent);
        if (added) {
            notifyStaffAdded(getStaff(serialNumber));
        }
        return added;
    }

    /**
     * Rimuove un membro dello staff.
     * Non richiede più il parametro `currentUser`.
     */
    public boolean removeStaff(Staff s) {
        // Usa `this.currentUser` per il controllo di autorizzazione.
        boolean removed = staffDataList.tryRemoveStaff(this.currentUser, s);
        if (removed) {
            notifyStaffRemoved(s);
        }
        return removed;
    }

    public Role createRole(Staff worker, String name, String description, Date date) {
        // Usa il factory method di Role per coerenza.
        Role rl = Role.create(name, description, date);
        if (worker != null) {
            rl.setWorker(worker);
        }
        notifyRoleCreated(rl);
        return rl;
    }

    public void assignRole(Staff worker, Role rl) {
        if (worker == null || rl == null) {
            throw new RoleException("Worker and Role cannot be null for assignment.");
        }
        rl.setWorker(worker);
        rl.setAssigned(true);
        // È buona pratica salvare l'aggiornamento nel DB
        rl.update(); 
        notifyRoleAssigned(rl);
    }

    public void addStaffNote(StaffNote n) {
        Staff staff = getStaff(n.getStaff().getSerialNumber());
        if (staff == null) {
            System.out.println("Staff not found for note.");
            return;
        }
        staffNotesMap.computeIfAbsent(staff, k -> new ArrayList<>()).add(n);
    }

    public void addHolidaysRequest(HolidaysRequest hr) {
        Staff staff = getStaff(hr.getWorker().getSerialNumber());
        if (staff == null) {
            System.out.println("Staff not found for holidays.");
            return;
        }
        staffHolidaysRequestMap.computeIfAbsent(staff, k -> new ArrayList<>()).add(hr);
    }
}