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

    private void notifyRoleUnassigned(Role rl, Staff s) {
        for (StaffEventReceiver r : eventReceivers) r.updateRoleUnassigned(rl, s);
    }
    
    private void notifyRoleReassigned(Role rl, Staff s) {
        for (StaffEventReceiver r : eventReceivers) r.updateRoleReassigned(rl, s);
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
     * La logica è interamente delegata a StaffDataList, che già gestisce
     * il salvataggio e le notifiche per la persistenza delle associazioni.
     */
    public boolean addStaff(int serialNumber, String name, String email, String phoneNumber,
                            String taxCode, String primaryMansion, boolean permanent) {
        // La firma del tuo costruttore Staff accetta un solo booleano (permanent)
        // e imposta 'available' a true di default. Rispettiamo questo.
        boolean added = staffDataList.insertStaffData(serialNumber, name, email, phoneNumber, taxCode, primaryMansion, true, permanent);
        return added;
    }

    /**
     * Rimuove un membro dello staff.
     * La logica è interamente delegata a StaffDataList.
     */
    public boolean removeStaff(Staff s) {
        boolean removed = staffDataList.removeStaffData(s);
        return removed;
    }

    public Role createRole(Staff worker, String name, String description, Date date, boolean isAssigned) {
        // Usa il factory method di Role per coerenza.
        Role rl = Role.create(name, description, date, isAssigned);
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
        if (!worker.isAvailable() || rl.isAssigned()) {
            throw new RoleException("Worker not available or role already assigned.");
        }
        worker.setAvailability(false);
        rl.setWorker(worker); // Questo imposta anche isAssigned a true
        notifyRoleAssigned(rl);
    }

    /**
     * Corrisponde letteralmente a UC5b: eliminaAssegnazioneRuoloPersonaleDisponibile
     */
    public void deleteRoleAssignment(Staff worker, Role role) {
        // Pre-condizioni
        if (role == null || worker == null || !worker.equals(role.getStaff()) || worker.isAvailable() || !role.isAssigned()) {
            throw new RoleException("Pre-condizioni per eliminare l'assegnazione non soddisfatte.");
        }
        Staff staffToUpdate = role.getStaff();

        // Post-condizioni
        role.setWorker(null);           // -> r.assegnato = no
        staffToUpdate.setAvailability(true); // -> p.disponibile = si
        
        notifyRoleUnassigned(role, staffToUpdate);
    }

    /**
     * Corrisponde a UC5a, Scenario 1: "cambiare lavoratore"
     * Per farlo, il metodo necessita del nuovo lavoratore come parametro esplicito.
     */
    public void modifyAssignmentByChangingWorker(Staff newWorker, Role role) {
        // Pre-condizioni
        if (role == null || !role.isAssigned() || newWorker == null || !newWorker.isAvailable()) {
            throw new RoleException("Pre-condizioni per cambiare lavoratore non soddisfatte.");
        }
        Staff oldWorker = role.getStaff();
        
        // Post-condizioni
        oldWorker.setAvailability(true); // -> p.disponibile = si
        newWorker.setAvailability(false);
        role.setWorker(newWorker);      // -> r.assegnato rimane si, ma con nuovo worker
        
        notifyRoleReassigned(role, oldWorker);
    }

    /**
     * Corrisponde a UC5a, Scenario 2: "cambiare ruolo"
     * Per farlo, il metodo necessita del nuovo ruolo come parametro esplicito.
     */
    public void modifyAssignmentByChangingRole(Staff worker, Role oldRole, Role newRole) {
        // Pre-condizioni
        if (worker == null || oldRole == null || !oldRole.getStaff().equals(worker) || newRole == null || newRole.isAssigned()) {
            throw new RoleException("Pre-condizioni per cambiare ruolo non soddisfatte.");
        }
        
        // Post-condizioni
        oldRole.setWorker(null);      // -> r(vecchio).assegnato = no
        newRole.setWorker(worker);    // -> p.disponibile rimane no, assegnato a nuovo ruolo
        
        notifyRoleUnassigned(oldRole, worker);
        notifyRoleAssigned(newRole);
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

    public void setPermanentStatus(Staff worker, boolean isPermanent) {
        if (worker == null) {
            throw new IllegalArgumentException("Worker cannot be null.");
        }
        
        // 1. Modifica lo stato dell'oggetto in memoria
        worker.setPermanent(isPermanent);
        
        // 2. Salva direttamente le modifiche nel database
        boolean updated = worker.update();
        if (!updated) {
            throw new RuntimeException("Failed to update staff permanent status in database");
        }
        
        // 3. Notifica il sistema che i dati sono cambiati (per la UI, ecc.)
        staffDataList.notifyStaffDataUpdated(worker);
    }
}