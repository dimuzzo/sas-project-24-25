package catering.businesslogic.holidaysrequest;

import java.sql.Date;
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
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

@TestMethodOrder(OrderAnnotation.class)
public class HolidaysRequestTest {

    private static final Logger LOGGER = LogManager.getLogger(HolidaysRequestTest.class);
    private static CatERing app;
    private static HolidaysRequestManager holidaysRequestManager;
    private static User organizer;

    // Aggiungo una variabile d'istanza per lo staff di test
    private Staff testStaff;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
    }

    @BeforeEach
    void setup() {
        try {
            // Login dell'organizzatore e setup del manager
            organizer = User.load("Giovanni");
            app.getUserManager().fakeLogin(organizer.getUserName());
            holidaysRequestManager = app.getHolidaysRequestManager();
            assertNotNull(holidaysRequestManager, "Lo holidaysManager non dovrebbe essere null per un organizzatore loggato.");
            
            // Creazione di uno staff pulito per ogni test
            int sNum = new Random().nextInt(900000) + 100000;
            testStaff = new Staff(sNum, "Staff Ferie " + sNum, "ferie@test.com", "444", "FERIESFF", "Tester", true);
            assertTrue(testStaff.save(), "Setup fallito: salvataggio dello staff temporaneo.");

        } catch (UseCaseLogicException e) {
            fail("Setup fallito: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        // Pulizia dello staff creato dopo ogni test per garantire l'isolamento
        if (testStaff != null) {
            testStaff.delete();
        }
    }

    @Test
    @Order(1)
    void test_UC8_createHolidaysRequest() {
        LOGGER.info("TEST: UC8 - Creazione richiesta ferie");
        HolidaysRequest hr = null;
        try {
            // AZIONE
            hr = holidaysRequestManager.createHolidaysRequest(organizer, testStaff, Date.valueOf("2025-08-10"));

            // VERIFICA
            assertNotNull(hr, "La richiesta di ferie creata non deve essere null.");
            assertTrue(hr.getId() > 0, "La richiesta salvata deve avere un ID dal DB.");
            assertFalse(hr.isAssigned(), "Una nuova richiesta non deve essere assegnata.");

            HolidaysRequest loadedHr = HolidaysRequest.load(hr.getId());
            assertNotNull(loadedHr, "La richiesta deve essere ricaricabile dal DB.");
            assertEquals(testStaff.getSerialNumber(), loadedHr.getWorker().getSerialNumber());

        } finally {
            if (hr != null) {
                holidaysRequestManager.deleteHolidaysRequest(testStaff, hr);
            }
        }
    }

    @Test
    @Order(2)
    void test_UC8a_assignHolidaysRequest_Success() {
        LOGGER.info("TEST: UC8a - Assegnazione (successo) richiesta ferie");
        HolidaysRequest hr = null;
        try {
            // SETUP
            hr = holidaysRequestManager.createHolidaysRequest(organizer, testStaff, Date.valueOf("2025-09-15"));
            assertNotNull(hr, "Setup fallito: la richiesta di ferie non è stata creata.");
            assertTrue(testStaff.isAvailable(), "Pre-condizione: lo staff deve essere disponibile.");

            // AZIONE
            holidaysRequestManager.assignHolidaysRequest(testStaff, hr);

            // VERIFICA
            HolidaysRequest loadedHr = HolidaysRequest.load(hr.getId());
            Staff updatedStaff = Staff.loadStaff(testStaff.getSerialNumber());

            assertTrue(loadedHr.isAssigned(), "Post-condizione: la richiesta deve risultare assegnata.");
            assertFalse(updatedStaff.isAvailable(), "Post-condizione: lo staff deve risultare non disponibile.");

        } finally {
            if (hr != null) {
                holidaysRequestManager.deleteHolidaysRequest(testStaff, hr);
            }
        }
    }

    @Test
    @Order(3)
    void test_UC8a_assignHolidaysRequest_FailureAlreadyAssigned() {
        LOGGER.info("TEST: UC8a - Assegnazione (fallimento) richiesta già assegnata");
        HolidaysRequest hr = null;
        try {
            // SETUP
            hr = holidaysRequestManager.createHolidaysRequest(organizer, testStaff, Date.valueOf("2025-11-20"));
            assertNotNull(hr, "Setup fallito: impossibile creare la richiesta.");
            
            // Prima assegnazione (corretta)
            holidaysRequestManager.assignHolidaysRequest(testStaff, hr);
            assertTrue(hr.isAssigned(), "Pre-condizione: la richiesta deve essere già assegnata.");

            // CORREZIONE: Crea una variabile finale per la lambda
            final HolidaysRequest requestToFail = hr;
            
            // AZIONE E VERIFICA ECCEZIONE - Usa la nuova variabile nella lambda
            assertThrows(HolidaysRequestException.class, () -> {
                holidaysRequestManager.assignHolidaysRequest(testStaff, requestToFail);
            }, "Deve essere lanciata una HolidaysRequestException per richieste già assegnate.");

        } finally {
            if (hr != null) {
                holidaysRequestManager.deleteHolidaysRequest(testStaff, hr);
            }
        }
    }

    @Test
    @Order(4)
    void test_UC8b_deleteHolidaysRequest() {
        LOGGER.info("TEST: UC8b - Eliminazione richiesta ferie");
        HolidaysRequest hr = null;
        // SETUP
        hr = holidaysRequestManager.createHolidaysRequest(organizer, testStaff, Date.valueOf("2025-12-25"));
        assertNotNull(hr, "Setup fallito: la richiesta da cancellare non è stata creata.");
        int hrId = hr.getId();

        // AZIONE
        holidaysRequestManager.deleteHolidaysRequest(testStaff, hr);

        // VERIFICA
        HolidaysRequest loadedHr = HolidaysRequest.load(hrId);
        assertNull(loadedHr, "La richiesta cancellata non dovrebbe più essere presente nel DB.");
    }
}