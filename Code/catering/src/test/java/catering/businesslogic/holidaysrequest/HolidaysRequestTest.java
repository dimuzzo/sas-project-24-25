package catering.businesslogic.holidaysrequest;

import java.sql.Date;
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
import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffManager;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

/**
 * Validation tests for the "Manage Holidays Request" use case.
 * This class tests the business logic within the HolidaysRequestManager and its interaction
 * with the persistence layer. Each test is designed to be independent.
 */
@TestMethodOrder(OrderAnnotation.class)
public class HolidaysRequestTest {

    private static final Logger LOGGER = LogManager.getLogger(HolidaysRequestTest.class);
    private static CatERing app;
    private static HolidaysRequestManager holidaysRequestManager;
    private static User organizer;

    // Lists to track temporary entities for automated cleanup
    private final List<Staff> tempStaffList = new ArrayList<>();
    private final List<HolidaysRequest> tempRequestList = new ArrayList<>();

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
            organizer = User.load("Giovanni");
            app.getUserManager().fakeLogin(organizer.getUserName());
            holidaysRequestManager = app.getHolidaysRequestManager();
            assertNotNull(holidaysRequestManager, "HolidaysRequestManager should not be null for a logged-in organizer.");
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
        // Delete requests first
        for (HolidaysRequest hr : tempRequestList) {
            if (HolidaysRequest.load(hr.getId()) != null) {
                hr.delete();
            }
        }
        // Then delete the staff
        StaffManager staffManager = app.getStaffManager(); // Get staff manager for cleanup
        for (Staff staff : tempStaffList) {
            staffManager.removeStaff(staff);
        }
        tempRequestList.clear();
        tempStaffList.clear();
    }

    // --- HELPER METHODS ---

    /**
     * Helper to create and save a temporary staff member via the StaffManager.
     * @return The created Staff object.
     */
    private Staff createTestStaff() {
        StaffManager staffManager = app.getStaffManager();
        int sNum = new Random().nextInt(900000) + 100000;
        String name = "Holiday Staff " + sNum;
        boolean added = staffManager.addStaff(sNum, name, name.replace(" ", ".") + "@test.com", "555-HOLIDAY", "HLY" + sNum, "Tester", true);
        assertTrue(added, "Helper method failed: could not add staff.");

        Staff staff = Staff.loadStaff(sNum);
        assertNotNull(staff, "Helper method failed: could not load created staff.");
        tempStaffList.add(staff); // Track for cleanup
        return staff;
    }

    /**
     * Helper to create a temporary holidays request via the HolidaysRequestManager.
     * @return The created HolidaysRequest object.
     */
    private HolidaysRequest createTestHolidaysRequest(Staff staff) {
        HolidaysRequest hr = holidaysRequestManager.createHolidaysRequest(organizer, staff, Date.valueOf("2025-08-15"));
        assertNotNull(hr, "Helper method failed to create a holiday request.");
        tempRequestList.add(hr); // Track for cleanup
        return hr;
    }

    // --- TESTS ---

    /**
     * Corresponds to Contract 8: gestisciRichiestaFeriePersonale.
     */
    @Test
    @Order(1)
    void test_manageHolidaysRequest() {
        LOGGER.info("TEST: Contract 8 - Managing a holiday request");
        // ARRANGE
        Staff staff = createTestStaff();

        // ACT
        HolidaysRequest hr = holidaysRequestManager.createHolidaysRequest(organizer, staff, Date.valueOf("2025-08-10"));
        tempRequestList.add(hr); // Add to cleanup list

        // ASSERT
        assertNotNull(hr, "The created holiday request should not be null.");
        assertTrue(hr.getId() > 0, "A saved request must have a DB-generated ID.");
        assertFalse(hr.isAssigned(), "A new request should not be assigned (approved).");

        HolidaysRequest loadedHr = HolidaysRequest.load(hr.getId());
        assertNotNull(loadedHr, "The request should be reloadable from the DB.");
        assertEquals(staff.getSerialNumber(), loadedHr.getWorker().getSerialNumber());
    }

    /**
     * Corresponds to Contract 8a (Extension scenario): assegnaRichiestaFeriePersonale.
     */
    @Test
    @Order(2)
    void test_assignHolidaysRequest_Success() {
        LOGGER.info("TEST: Contract 8a (Extension scenario) - Assigning (approving) a holiday request successfully");
        // ARRANGE
        Staff staff = createTestStaff();
        HolidaysRequest hr = createTestHolidaysRequest(staff);
        assertTrue(staff.isAvailable(), "Pre-condition: the staff member must be available.");

        // ACT
        holidaysRequestManager.assignHolidaysRequest(staff, hr);

        // ASSERT
        HolidaysRequest loadedHr = HolidaysRequest.load(hr.getId());
        Staff updatedStaff = Staff.loadStaff(staff.getSerialNumber());

        assertTrue(loadedHr.isAssigned(), "Post-condition: the request should be marked as assigned.");
        assertFalse(updatedStaff.isAvailable(), "Post-condition: the staff member should be marked as unavailable.");
    }

    /**
     * Corresponds to Contract 8a (Exception scenario): assegnaRichiestaFeriePersonale.
     */
    @Test
    @Order(3)
    void test_assignHolidaysRequest_AlreadyAssignedException() {
        LOGGER.info("TEST: Contract 8a (Exception scenario) - Assigning an already assigned request should fail");
        // ARRANGE
        Staff staff = createTestStaff();
        HolidaysRequest hr = createTestHolidaysRequest(staff);

        // First assignment (should succeed)
        holidaysRequestManager.assignHolidaysRequest(staff, hr);
        assertTrue(hr.isAssigned(), "Pre-condition: the request must already be assigned.");
        
        // ACT & ASSERT
        assertThrows(HolidaysRequestException.class, () -> {
            holidaysRequestManager.assignHolidaysRequest(staff, hr);
        }, "A HolidaysRequestException should be thrown for already assigned requests.");
    }

    /**
     * Corresponds to Contract 8b (Extension scenario): eliminaRichiestaFeriePersonale.
     */
    @Test
    @Order(4)
    void test_UC8b_deleteHolidaysRequest() {
        LOGGER.info("TEST: Contract 8b (Extension scenario) - Deleting a holiday request");
        // ARRANGE
        Staff staff = createTestStaff();
        HolidaysRequest hr = createTestHolidaysRequest(staff);
        int hrId = hr.getId(); // Save ID before deletion

        // ACT
        holidaysRequestManager.deleteHolidaysRequest(staff, hr);

        // ASSERT
        HolidaysRequest loadedHr = HolidaysRequest.load(hrId);
        assertNull(loadedHr, "The deleted request should no longer be present in the DB.");
    }
}