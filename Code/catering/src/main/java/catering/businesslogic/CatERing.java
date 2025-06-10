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
import catering.persistence.HolidaysRequestPersistence;
import catering.persistence.KitchenTaskPersistence;
import catering.persistence.MenuPersistence;
import catering.persistence.StaffNotePersistence;
import catering.persistence.StaffPersistence;
import catering.persistence.SummaryFormPersistence;

/**
 * The main entry point for the CatERing application logic.
 * This class follows the Singleton pattern to provide a single, centralized
 * access point to all application managers.
 */
public class CatERing {
    private static CatERing singleInstance;

    // Manager
    private MenuManager menuMgr;
    private RecipeManager recipeMgr;
    private UserManager userMgr;
    private EventManager eventMgr;
    private KitchenTaskManager kitchenTaskMgr;
    private ShiftManager shiftMgr;
    private SummaryFormManager summaryFormMgr;

    // Session-specific managers (initialized lazily)
    private StaffManager staffMgr;
    private StaffNoteManager staffNoteMgr;
    private HolidaysRequestManager holidaysRequestMgr;

    // Persistence Receivers
    private MenuPersistence menuPersistence;
    private KitchenTaskPersistence kitchenTaskPersistence;
    private StaffPersistence staffPersistence;
    private HolidaysRequestPersistence holidaysRequestPersistence;
    private StaffNotePersistence staffNotePersistence;
    private SummaryFormPersistence summaryFormPersistence;

    /**
     * Private constructor to enforce the Singleton pattern.
     * It initializes all global managers and registers their persistence listeners.
     * Session-dependent managers are not created here; they are initialized on first access.
     */
    private CatERing() {
        // Initialize global managers
        menuMgr = new MenuManager();
        recipeMgr = new RecipeManager();
        userMgr = new UserManager();
        eventMgr = new EventManager();
        kitchenTaskMgr = new KitchenTaskManager();
        shiftMgr = new ShiftManager();
        summaryFormMgr = new SummaryFormManager();

        // Initialize persistence receivers
        menuPersistence = new MenuPersistence();
        kitchenTaskPersistence = new KitchenTaskPersistence();
        staffPersistence = new StaffPersistence();
        holidaysRequestPersistence = new HolidaysRequestPersistence();
        staffNotePersistence = new StaffNotePersistence();
        summaryFormPersistence = new SummaryFormPersistence();

        // Wire up persistence for global managers
        menuMgr.addEventReceiver(menuPersistence);
        kitchenTaskMgr.addEventReceiver(kitchenTaskPersistence);
        summaryFormMgr.addEventReceiver(summaryFormPersistence);
    }

    /**
     * Provides access to the single instance of the CatERing application.
     * @return The singleton instance of CatERing.
     */
    public static CatERing getInstance() {
        if (singleInstance == null) {
            singleInstance = new CatERing();
        }
        return singleInstance;
    }

    /**
     * Ensures that session-specific managers (like StaffManager) are initialized.
     * This private helper method implements a lazy-initialization pattern. It is called
     * by the public getters of session-dependent managers.
     */
    private void ensureSessionManagersInitialized() {
        // If staffMgr has not been created AND a user is currently logged in...
        if (this.staffMgr == null && this.userMgr.getCurrentUser() != null) {
            // ...then create the entire stack of session managers.
            this.staffMgr = new StaffManager(this.userMgr.getCurrentUser());
            this.staffNoteMgr = new StaffNoteManager(this.staffMgr);
            this.holidaysRequestMgr = new HolidaysRequestManager(this.staffMgr);

            // And wire up the necessary persistence listeners for them
            this.staffMgr.addEventReceiver(this.staffPersistence);
            this.holidaysRequestMgr.addEventReceiver(this.holidaysRequestPersistence);
            this.staffNoteMgr.addEventReceiver(this.staffNotePersistence);
        }
    }
    
    /**
     * A demonstration entry point for the application.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        System.out.println("--- CatERing Application Start ---");
        // Get the singleton instance which initializes global managers
        CatERing app = CatERing.getInstance();
        System.out.println("Global managers initialized.");

        // Simulate a user login before checking session managers
        System.out.println("\nStep 1: Simulating user login...");
        try {
            app.getUserManager().fakeLogin("Giovanni"); 
            System.out.println("User logged in successfully: " + app.getUserManager().getCurrentUser().getUserName());
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
        }

        // Now that the user is (potentially) logged in, check manager availability again
        System.out.println("\nStep 2: Checking manager availability post-login...");
        
        // Log which managers are available
        System.out.println("Available managers:");
        System.out.println("- Menu Manager: " + (app.getMenuManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Recipe Manager: " + (app.getRecipeManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- User Manager: " + (app.getUserManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Event Manager: " + (app.getEventManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Kitchen Task Manager: " + (app.getKitchenTaskManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Shift Manager: " + (app.getShiftManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- Staff Manager: " + (app.getStaffManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- StaffNote Manager: " + (app.getStaffNoteManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- HolidaysRequest Manager: " + (app.getHolidaysRequestManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- SummaryForm Manager: " + (app.getSummaryFormManager() != null ? "OK" : "NOT AVAILABLE"));
    }


    // Getters for session managers now ensure initialization before returning
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

    // Getters and Setters

    public KitchenTaskManager getKitchenTaskManager() { 
        return kitchenTaskMgr; 
    }

    public ShiftManager getShiftManager() { 
        return shiftMgr; 
    }

    public MenuManager getMenuManager() { 
        return menuMgr; 
    }

    public RecipeManager getRecipeManager() { 
        return recipeMgr; 
    }

    public UserManager getUserManager() { 
        return userMgr; 
    }

    public EventManager getEventManager() { 
        return eventMgr; 
    }

    public SummaryFormManager getSummaryFormManager() { 
        return summaryFormMgr; 
    }
}