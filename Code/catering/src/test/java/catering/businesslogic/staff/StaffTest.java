/**
 * * @author Alessandro Demo, Matricola 1049825
 */
package catering.businesslogic.staff;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

/**
 * Validation tests for the "Manage Staff" use case.
 * This class tests the business logic within the StaffManager and its interaction
 * with the persistence layer. Each test is designed to be independent.
 */
@TestMethodOrder(OrderAnnotation.class)
public class StaffTest {

    private static final Logger LOGGER = LogManager.getLogger(StaffTest.class);
    private static CatERing app;
    private StaffManager staffManager;

    // Lists to track temporary entities created during tests for automated cleanup
    private final List<Staff> tempStaffList = new ArrayList<>();
    private final List<Role> tempRoleList = new ArrayList<>();

    /**
     * Initializes the database once for the entire test class.
     */
    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
    }

    /**
     * Runs before each test to ensure a clean state.
     * It logs in an organizer user and initializes a fresh StaffManager instance.
     */
    @BeforeEach
    void setup() {
        try {
            User organizer = User.load("Giovanni");
            app.getUserManager().fakeLogin(organizer.getUserName());
            staffManager = app.getStaffManager();
            assertNotNull(staffManager, "The StaffManager should not be null for a logged-in organizer.");
        } catch (UseCaseLogicException e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }

    /**
     * Cleans up all temporary entities created during a test.
     * This method runs after each test to ensure a clean state for the next one.
     */
    @AfterEach
    void cleanup() {
        // Unassign roles before deleting to avoid constraint violations
        for (Role r : tempRoleList) {
            if (r.isAssigned()) {
                try {
                    staffManager.deleteRoleAssignment(r.getStaff(), r);
                } catch (Exception e) {
                    // This might fail if the entity was already manipulated; proceed with cleanup.
                }
            }
        }

        // Delete roles using the model's delete method, as StaffManager doesn't provide one
        tempRoleList.forEach(role -> {
            try { role.delete(); } catch (RoleException e) { /* ignore if already deleted */ }
        });

        // Delete staff using the manager's remove method
        tempStaffList.forEach(staff -> staffManager.removeStaff(staff));

        tempRoleList.clear();
        tempStaffList.clear();
    }

    // --- HELPER METHODS ---

    /**
     * Helper to create and save a temporary staff member via the StaffManager.
     * @return The created Staff object.
     */
    private Staff createTestStaff() {
        int sNum = new Random().nextInt(900000) + 100000;
        String name = "Test Staff " + sNum;
        boolean added = staffManager.addStaff(sNum, name, name.replace(" ", ".") + "@test.com", "555-1234", "TST" + sNum, "Tester", false);
        assertTrue(added, "Helper method failed: could not add staff.");

        Staff staff = Staff.loadStaff(sNum);
        assertNotNull(staff, "Helper method failed: could not load created staff.");
        tempStaffList.add(staff); // Track for automated cleanup
        return staff;
    }

    /**
     * Helper to create a temporary role via the StaffManager.
     * @return The created Role object.
     */
    private Role createTestRole() {
        String roleName = "Test Role " + System.currentTimeMillis();
        Role role = staffManager.createRole(null, roleName, "Test Description", Date.valueOf("2025-10-10"));
        assertNotNull(role, "Helper method failed: staffManager.createRole returned null.");

        try {
            // ACTION: Persist the role. In a real scenario, an event receiver would do this.
            Role.create(role);
        } catch (SQLException e) {
            // If persistence fails, the setup is broken. Fail the test with a clear message.
            fail("Helper method 'createTestRole' failed due to a database exception.", e);
        }
        
        // VERIFY: Check the side effect of persistence, which is getting an ID.
        assertTrue(role.getId() > 0, "Helper method failed: persisted role should have an ID.");
        
        tempRoleList.add(role); // Track for automated cleanup
        return role;
    }

    // --- TESTS ---

    /**
     * Corresponds to Contract 2: scriveRuolo.
     */
    @Test
    @Order(1)
    void test_writeRole() {
        LOGGER.info("TEST: Contract 2 - Role writing");
        // ARRANGE & ACT
        Role newRole = createTestRole(); // The helper method handles arrangement and action

        // ASSERT
        assertTrue(newRole.getId() > 0, "A saved role must have an ID > 0.");
        Role loadedRole = Role.loadRole(newRole.getName());
        assertNotNull(loadedRole, "The role should be loadable from the DB after saving.");
        assertEquals(newRole.getName(), loadedRole.getName());
        assertFalse(loadedRole.isAssigned());
    }

    /**
     * Corresponds to Contract 2a (Exception scenario): eliminaRuolo.
     */
    @Test
    @Order(2)
    void test_deleteRole_AssignedException() {
        LOGGER.info("TEST: Contract 2a (Exception) - Deleting an assigned role");
        // ARRANGE
        Staff staff = createTestStaff();
        Role role = createTestRole();
        staffManager.assignRole(staff, role);
        assertTrue(role.isAssigned(), "Pre-condition failed: role should be assigned.");

        // ACT & ASSERT
        assertThrows(RoleException.class, role::delete, "A RoleException must be thrown when deleting a role in use.");
    }

    /**
     * Corresponds to Contract 2a (Extension scenario): eliminaRuolo.
     */
    @Test
    @Order(3)
    void test_deleteRole_Success() {
        LOGGER.info("TEST: Contract 2a (Extension) - Deleting an unassigned role");
        // ARRANGE
        Role role = createTestRole();
        assertFalse(role.isAssigned(), "Pre-condition: the role must be unassigned");

        // ACT
        boolean deleted = false;
        try {
            deleted = role.delete();
        } catch (RoleException e) {
            fail("Deleting an unassigned role should not throw an exception.", e);
        }

        // ASSERT
        assertTrue(deleted, "Deleting an unassigned role should return true.");
        Role loadedRole = Role.loadRole(role.getName());
        assertNull(loadedRole, "The deleted role should no longer be present in the DB.");
    }

    /**
     * Corresponds to Contract 4: inserisciDatiPersonale.
     */
    @Test
    @Order(4)
    void test_insertStaffData() {
        LOGGER.info("TEST: Contract 4 - Staff data insertion via StaffDataList");
        // ARRANGE (is empty)

        // ACT
        int serialNumber = new Random().nextInt(90000) + 10000;
        boolean added = staffManager.addStaff(serialNumber, "Staff " + serialNumber, "new@test.it", "555", "NVSTFF", "Extra", false);
        tempStaffList.add(Staff.loadStaff(serialNumber)); // Track for cleanup

        // ASSERT
        assertTrue(added, "addStaff should return true.");
        assertNotNull(Staff.loadStaff(serialNumber), "The new staff member should be retrievable from the DB.");
    }

    /**
     * Corresponds to Contract 4a: modificaDatiPersonale.
     */
    @Test
    @Order(5)
    void test_updateStaffData() {
        LOGGER.info("TEST: Contract 4a - Updating staff data via StaffDataList");
        // ARRANGE
        Staff staff = createTestStaff();
        StaffDataList sdl = staffManager.getStaffDataList();

        // ACT
        String newName = "Modified Name";
        String newEmail = "modified@email.com";
        boolean updated = sdl.updateStaffData(staff, newName, newEmail, staff.getPhoneNumber(),
                staff.getTaxCode(), staff.getPrimaryMansion(), staff.isAvailable(), staff.isPermanent());
        
        // ASSERT
        assertTrue(updated, "The list's updateStaffData method should return true.");
        Staff updatedStaff = Staff.loadStaff(staff.getSerialNumber());
        assertNotNull(updatedStaff, "The modified staff member should be loadable.");
        assertEquals(newName, updatedStaff.getName(), "The name was not updated correctly.");
        assertEquals(newEmail, updatedStaff.getEmail(), "The email was not updated correctly.");
    }

    /**
     * Corresponds to Contract 5: assegnaRuoloPersonaleDisponibile.
     */
    @Test
    @Order(6)
    void test_assignRole() {
        LOGGER.info("TEST: Contract 5 - Assigning a role to a staff member");
        // ARRANGE
        Staff staff = createTestStaff();
        Role role = createTestRole();
        assertTrue(staff.isAvailable(), "Pre-condition: The staff member must be available.");
        assertFalse(role.isAssigned(), "Pre-condition: The role must not be assigned.");

        // ACT
        staffManager.assignRole(staff, role);

        // ASSERT
        Role loadedRole = Role.loadRole(role.getName());
        assertNotNull(loadedRole, "The role should exist in the DB.");
        assertTrue(loadedRole.isAssigned(), "Post-condition: The role should now be assigned.");
        assertEquals(staff.getSerialNumber(), loadedRole.getStaff().getSerialNumber(), "The role must be assigned to the correct staff member.");
    }

    /**
     * Corresponds to Contract 5b: eliminaAssegnazioneRuoloPersonaleDisponibile.
     */
    @Test
    @Order(7) 
    void test_deleteRoleAssignment() {
        LOGGER.info("TEST: Contract 5b - Deleting a role assignment");
        // ARRANGE
        Staff staff = createTestStaff();
        Role role = createTestRole();
        staffManager.assignRole(staff, role);
        assertFalse(Staff.loadStaff(staff.getSerialNumber()).isAvailable(), "Pre-condition: the staff should not be available.");
        assertTrue(Role.loadRole(role.getName()).isAssigned(), "Pre-condition: the role should be assigned.");

        // ACT
        staffManager.deleteRoleAssignment(staff, role);

        // ASSERT
        Staff loadedStaff = Staff.loadStaff(staff.getSerialNumber());
        Role loadedRole = Role.loadRole(role.getName());
        assertTrue(loadedStaff.isAvailable(), "Post-condition: the staff should be available again.");
        assertFalse(loadedRole.isAssigned(), "Post-condition: the role should no longer be assigned.");
    }
    
    /**
     * Corresponds to Contract 7a: modificaTipologiaAssunzione.
     */
    @Test
    @Order(8) 
    void test_setPermanentStatus() {
        LOGGER.info("TEST: Contract 7a - Changing employment type");
        // ARRANGE
        Staff staff = createTestStaff(); // Starts as occasional by default in helper
        assertFalse(staff.isPermanent(), "Pre-condition: the staff must be occasional.");

        // ACT 1
        staffManager.setPermanentStatus(staff, true);
        
        // ASSERT 1
        Staff loadedStaff = Staff.loadStaff(staff.getSerialNumber());
        assertTrue(loadedStaff.isPermanent(), "Post-condition: the staff should have become permanent.");

        // ACT 2: Reverse action for completeness
        staffManager.setPermanentStatus(loadedStaff, false);
        
        // ASSERT 2
        loadedStaff = Staff.loadStaff(staff.getSerialNumber());
        assertFalse(loadedStaff.isPermanent(), "Post-condition: the staff should be occasional again.");
    }
}