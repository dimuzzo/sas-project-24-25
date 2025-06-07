package catering.businesslogic;

import catering.businesslogic.event.EventManager;
import catering.businesslogic.holidaysrequest.HolidaysRequestManager;
import catering.businesslogic.kitchen.KitchenTaskManager;
import catering.businesslogic.menu.MenuManager;
import catering.businesslogic.recipe.RecipeManager;
import catering.businesslogic.shift.ShiftManager;
import catering.businesslogic.staff.StaffManager;
import catering.businesslogic.staffnote.StaffNoteManager;
import catering.businesslogic.summaryform.SummaryFormManager;
import catering.businesslogic.user.UserManager;
import catering.persistence.KitchenTaskPersistence;
import catering.persistence.MenuPersistence;
import catering.persistence.StaffPersistence;

public class CatERing {
    private static CatERing singleInstance;

    public static CatERing getInstance() {
        if (singleInstance == null) {
            singleInstance = new CatERing();
        }
        return singleInstance;
    }

    // Manager
    private MenuManager menuMgr;
    private RecipeManager recipeMgr;
    private UserManager userMgr;
    private EventManager eventMgr;
    private KitchenTaskManager kitchenTaskMgr;
    private ShiftManager shiftMgr;
    private SummaryFormManager summaryFormMgr;

    // Manager di sessione (inizializzati "pigramente")
    private StaffManager staffMgr;
    private StaffNoteManager staffNoteMgr;
    private HolidaysRequestManager holidaysRequestMgr;

    // Persistence
    private MenuPersistence menuPersistence;
    private KitchenTaskPersistence kitchenTaskPersistence;
    private StaffPersistence staffPersistence;

    /**
     * Il costruttore non crea più i manager dipendenti dalla sessione.
     * Verranno creati al primo accesso tramite i loro metodi getter.
     */
    private CatERing() {
        menuMgr = new MenuManager();
        recipeMgr = new RecipeManager();
        userMgr = new UserManager();
        eventMgr = new EventManager();
        kitchenTaskMgr = new KitchenTaskManager();
        shiftMgr = new ShiftManager();
        summaryFormMgr = new SummaryFormManager();
        
        // staffMgr = new StaffManager(); // <-- RIMOSSO
        // staffNoteMgr = new StaffNoteManager(staffMgr); // <-- RIMOSSO
        // holidaysRequestMgr = new HolidaysRequestManager(staffMgr); // <-- RIMOSSO

        menuPersistence = new MenuPersistence();
        kitchenTaskPersistence = new KitchenTaskPersistence();
        staffPersistence = new StaffPersistence();

        menuMgr.addEventReceiver(menuPersistence);
        kitchenTaskMgr.addEventReceiver(kitchenTaskPersistence);
        // staffMgr.addEventReceiver(staffPersistence); // <-- RIMOSSO (verrà aggiunto dopo la creazione)
    }

    /**
     * Esegue l'inizializzazione pigra dello StaffManager e dei suoi dipendenti.
     * Questo metodo privato viene chiamato dai getter pubblici.
     */
    private void ensureSessionManagersInitialized() {
        // Se lo staffMgr non è stato creato E c'è un utente loggato...
        if (this.staffMgr == null && this.userMgr.getCurrentUser() != null) {
            // ...allora crea tutto lo stack di manager di sessione.
            this.staffMgr = new StaffManager(this.userMgr.getCurrentUser());
            this.staffNoteMgr = new StaffNoteManager(this.staffMgr);
            this.holidaysRequestMgr = new HolidaysRequestManager(this.staffMgr);

            // E collega i listener necessari
            this.staffMgr.addEventReceiver(this.staffPersistence);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("--- CatERing Application Start ---");
        // Get the singleton instance which initializes global managers
        CatERing app = CatERing.getInstance();
        System.out.println("Global managers initialized.");

        // --- PASSAGGIO CHIAVE MANCANTE ---
        // 1. Simula il login di un utente prima di controllare i manager di sessione
        System.out.println("\nStep 1: Simulating user login...");
        try {
            // Usa un nome utente che sai esistere nel tuo database
            app.getUserManager().fakeLogin("chef_mario"); 
            System.out.println("User logged in successfully: " + app.getUserManager().getCurrentUser().getUserName());
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            // Se il login fallisce, i manager di sessione rimarranno giustamente "NOT AVAILABLE"
        }
        // ------------------------------------

        // 2. Adesso che l'utente è (potenzialmente) loggato, controlla di nuovo la disponibilità
        System.out.println("\nStep 2: Checking manager availability post-login...");
        
        // Log which managers are available
        System.out.println("Available managers:");
        System.out.println("- Menu Manager: " + (app.getMenuManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Recipe Manager: " + (app.getRecipeManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- User Manager: " + (app.getUserManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Event Manager: " + (app.getEventManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Kitchen Task Manager: " + (app.getKitchenTaskManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Shift Manager: " + (app.getShiftManager() != null ? "OK" : "NOT AVAILABLE"));
        // Questa volta, la chiamata a getStaffManager() troverà un utente e creerà l'istanza
        System.out.println("- Staff Manager: " + (app.getStaffManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- StaffNote Manager: " + (app.getStaffNoteManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- HolidaysRequest Manager: " + (app.getHolidaysRequestManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- SummaryForm Manager: " + (app.getSummaryFormManager() != null ? "OK" : "NOT AVAILABLE"));
    }

    // I GETTER DEI MANAGER DI SESSIONE ORA CONTROLLANO L'INIZIALIZZAZIONE

    public StaffManager getStaffManager() {
        ensureSessionManagersInitialized();
        return this.staffMgr;
    }

    public StaffNoteManager getStaffNoteManager() {
        ensureSessionManagersInitialized();
        return this.staffNoteMgr;
    }

    public HolidaysRequestManager getHolidaysRequestManager() {
        ensureSessionManagersInitialized();
        return this.holidaysRequestMgr;
    }

    // GETTER E SETTER PER GLI ALTRI MANAGER (invariati)

    public KitchenTaskManager getKitchenTaskManager() { return kitchenTaskMgr; }
    public ShiftManager getShiftManager() { return shiftMgr; }
    public MenuManager getMenuManager() { return menuMgr; }
    public RecipeManager getRecipeManager() { return recipeMgr; }
    public UserManager getUserManager() { return userMgr; }
    public EventManager getEventManager() { return eventMgr; }
    public SummaryFormManager getSummaryFormManager() { return summaryFormMgr; }
    
    // I setter potrebbero non essere necessari se la gestione è solo interna.
    public void setShiftManager(ShiftManager shiftMgr) { this.shiftMgr = shiftMgr; }
    public void setMenuManager(MenuManager menuMgr) { this.menuMgr = menuMgr; }
    public void setRecipeManager(RecipeManager recipeMgr) { this.recipeMgr = recipeMgr; }
    public void setUserManager(UserManager userMgr) { this.userMgr = userMgr; }
    public void setEventManager(EventManager eventMgr) { this.eventMgr = eventMgr; }
    public void setKitchenTaskManager(KitchenTaskManager kitchenTaskMgr) { this.kitchenTaskMgr = kitchenTaskMgr; }
    public void setStaffManager(StaffManager staffMgr) { this.staffMgr = staffMgr; }
    public void setStaffNoteManager(StaffNoteManager staffNoteMgr) { this.staffNoteMgr = staffNoteMgr; }
    public void setHolidaysRequestManager(HolidaysRequestManager holidaysRequestMgr) { this.holidaysRequestMgr = holidaysRequestMgr; }
    public void setSummaryFormManager(SummaryFormManager summaryFormMgr) { this.summaryFormMgr = summaryFormMgr; }
}