package catering.businesslogic;

import catering.businesslogic.event.EventManager;
import catering.businesslogic.holidays.HolidaysManager;
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

    private MenuManager menuMgr;
    private RecipeManager recipeMgr;
    private UserManager userMgr;
    private EventManager eventMgr;
    private KitchenTaskManager kitchenTaskMgr;
    private ShiftManager shiftMgr;
    private StaffManager staffMgr;
    private StaffNoteManager staffNoteMgr;
    private HolidaysManager holidaysMgr;
    private SummaryFormManager summaryFormMgr;

    private MenuPersistence menuPersistence;
    private KitchenTaskPersistence kitchenTaskPersistence;
    private StaffPersistence staffPersistence;

    private CatERing() {
        menuMgr = new MenuManager();
        recipeMgr = new RecipeManager();
        userMgr = new UserManager();
        eventMgr = new EventManager();
        kitchenTaskMgr = new KitchenTaskManager();
        shiftMgr = new ShiftManager(); // Add this line to initialize ShiftManager
        staffMgr = new StaffManager();
        staffNoteMgr = new StaffNoteManager(staffMgr);
        holidaysMgr = new HolidaysManager(staffMgr);
        summaryFormMgr = new SummaryFormManager();

        menuPersistence = new MenuPersistence();
        kitchenTaskPersistence = new KitchenTaskPersistence();
        staffPersistence = new StaffPersistence();

        menuMgr.addEventReceiver(menuPersistence);
        kitchenTaskMgr.addEventReceiver(kitchenTaskPersistence);
        staffMgr.addEventReceiver(staffPersistence);
    }

    public static void main(String[] args) {
        // Get the singleton instance which initializes all managers
        CatERing app = CatERing.getInstance();

        System.out.println("CatERing application initialized successfully.");

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
        System.out.println("- Holidays Manager: " + (app.getHolidaysManager() != null ? "OK" : "NOT AVAILABLE"));
        System.out.println("- SummaryForm Manager: " + (app.getSummaryFormManager() != null ? "OK" : "NOT AVAILABLE"));
    }

    public KitchenTaskManager getKitchenTaskManager() {
        return kitchenTaskMgr; // Return the field that was properly initialized
    }

    public ShiftManager getShiftManager() {
        return shiftMgr;
    }

    public void setShiftManager(ShiftManager shiftMgr) {
        this.shiftMgr = shiftMgr;
    }

    public MenuManager getMenuManager() {
        return menuMgr;
    }

    public void setMenuManager(MenuManager menuMgr) {
        this.menuMgr = menuMgr;
    }

    public RecipeManager getRecipeManager() {
        return recipeMgr;
    }

    public void setRecipeManager(RecipeManager recipeMgr) {
        this.recipeMgr = recipeMgr;
    }

    public UserManager getUserManager() {
        return userMgr;
    }

    public void setUserManager(UserManager userMgr) {
        this.userMgr = userMgr;
    }

    public EventManager getEventManager() {
        return eventMgr;
    }

    public void setEventManager(EventManager eventMgr) {
        this.eventMgr = eventMgr;
    }

    public void setKitchenTaskManager(KitchenTaskManager kitchenTaskMgr) {
        this.kitchenTaskMgr = kitchenTaskMgr;
    }

    public StaffManager getStaffManager() { return staffMgr; }

    public void setStaffManager(StaffManager staffMgr) { this.staffMgr = staffMgr; }

    public StaffNoteManager getStaffNoteManager() { return staffNoteMgr; }

    public void setStaffNoteManager(StaffNoteManager staffNoteMgr) {
        this.staffNoteMgr = staffNoteMgr;
    }

    public HolidaysManager getHolidaysManager() { return holidaysMgr; }

    public void setHolidaysManager(HolidaysManager holidaysMgr) { this.holidaysMgr = holidaysMgr; }

    public SummaryFormManager getSummaryFormManager() { return summaryFormMgr; }

    public void setSummaryFormManager(SummaryFormManager summaryFormMgr) { this.summaryFormMgr = summaryFormMgr; }

}
