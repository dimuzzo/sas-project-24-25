package catering.businesslogic.staff;

import java.sql.Date;
import java.util.Random;
import java.util.logging.Logger;

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

@TestMethodOrder(OrderAnnotation.class)
public class StaffTest {

    private static final Logger LOGGER = LogManager.getLogger(StaffTest.class);
    private static CatERing app;
    private StaffManager staffManager;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
    }

    @BeforeEach
    void setup() {
        try {
            User organizer = User.load("Giovanni");
            app.getUserManager().fakeLogin(organizer.getUserName());
            staffManager = app.getStaffManager();
            assertNotNull(staffManager, "Lo StaffManager non dovrebbe essere null per un organizzatore loggato.");
        } catch (UseCaseLogicException e) {
            fail("Setup fallito: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    void test_UC2_writeRole() {
        LOGGER.info("TEST: UC2 - Scrittura di un ruolo");
        Role newRole = null;
        String roleName = "Test Role " + System.currentTimeMillis();
        try {
            newRole = staffManager.createRole(null, roleName, "Descrizione di test", Date.valueOf("2025-10-10"), false);
            assertNotNull(newRole, "Il ruolo creato dal manager non deve essere null.");
            
            Role.create(newRole);
            assertTrue(newRole.getId() > 0, "Un ruolo salvato deve avere un ID > 0.");

            Role loadedRole = Role.loadRole(roleName);
            assertNotNull(loadedRole, "Il ruolo dovrebbe essere caricabile dal DB dopo il salvataggio.");
            assertEquals(roleName, loadedRole.getName());
            assertFalse(loadedRole.isAssigned());

        } catch (Exception e) {
            fail("Il test UC2 non doveva lanciare un'eccezione: " + e.getMessage(), e);
        } finally {
            if (newRole != null && newRole.getId() > 0) {
                try {
                    newRole.delete();
                } catch (RoleException e) { /* Ignora */ }
            }
        }
    }

    @Test
    @Order(2)
    void test_UC2a_deleteRole_AssignedException() {
        LOGGER.info("TEST: UC2a - Eccezione eliminazione ruolo in uso");
        Staff tempStaff = null;
        Role tempRole = null;
        try {
            int sNum = new Random().nextInt(90000) + 10000;
            tempStaff = new Staff(sNum, "Staff Temp", "temp@staff.com", "111", "TMPSFF", "Tester", false);
            assertTrue(tempStaff.save(), "Setup fallito: salvataggio staff temporaneo.");
            
            String roleName = "Ruolo Temp " + sNum;
            tempRole = Role.create(roleName, "Descrizione", Date.valueOf("2025-10-11"), false);
            Role.create(tempRole);
            assertTrue(tempRole.getId() > 0, "Setup fallito: salvataggio ruolo temporaneo.");

            staffManager.assignRole(tempStaff, tempRole);
            
            final Role roleToDelete = tempRole;
            assertThrows(RoleException.class, roleToDelete::delete, "Deve essere lanciata una RoleException quando si cancella un ruolo in uso.");
        
        } catch (Exception e) {
            fail("Il test UC2a (fallimento) non doveva lanciare un'eccezione nel setup: " + e.getMessage(), e);
        } finally {
            if (tempRole != null) {
                tempRole.setWorker(null);
                tempRole.update();
                try { tempRole.delete(); } catch(RoleException e) {}
            }
            if (tempStaff != null) {
                tempStaff.delete();
            }
        }
    }

    @Test
    @Order(3)
    void test_UC2a_deleteRole_Success() {
        LOGGER.info("TEST NUOVO: UC2a - Eliminazione ruolo non in uso");
        Role newRole = null;
        String roleName = "Ruolo Cancellabile " + System.currentTimeMillis();
        try {
            newRole = Role.create(roleName, "Descrizione", Date.valueOf("2025-11-11"), false);
            Role.create(newRole);
            assertTrue(newRole.getId() > 0, "Setup fallito: salvataggio ruolo.");
            assertFalse(newRole.isAssigned(), "Pre-condizione: il ruolo non deve essere assegnato.");

            boolean deleted = newRole.delete();
            assertTrue(deleted, "La cancellazione di un ruolo non assegnato dovrebbe ritornare true.");

            Role loadedRole = Role.loadRole(roleName);
            assertNull(loadedRole, "Il ruolo cancellato non dovrebbe più essere presente nel DB.");
            
        } catch (Exception e) {
            fail("Il test UC2a (successo) non doveva lanciare un'eccezione: " + e.getMessage(), e);
        }
    }

    @Test
    @Order(4)
    void test_UC4_insertStaffData() {
        LOGGER.info("TEST: UC4 - Inserimento dati personale tramite StaffDataList");
        Staff createdStaff = null;
        int serialNumber = new Random().nextInt(90000) + 10000;
        try {
            String name = "Nuovo Staff " + serialNumber;
            
            StaffDataList sdl = staffManager.getStaffDataList();
            assertNotNull(sdl, "StaffDataList non deve essere null.");

            boolean added = sdl.insertStaffData(serialNumber, name, "nuovo@test.it", "555", "NVSTFF", "Extra", true, false);
            assertTrue(added, "insertStaffData dovrebbe ritornare true.");

            createdStaff = Staff.loadStaff(serialNumber);
            assertNotNull(createdStaff, "Il nuovo staff dovrebbe essere recuperabile dal DB.");
            assertEquals(name, createdStaff.getName());
        } catch (Exception e) {
            fail("Il test UC4 non doveva lanciare un'eccezione: " + e.getMessage(), e);
        } finally {
            if (createdStaff != null) {
                staffManager.removeStaff(createdStaff);
            }
        }
    }

    @Test
    @Order(5)
    void test_UC4a_updateStaffData() {
        LOGGER.info("TEST NUOVO: UC4a - Modifica dati personale tramite StaffDataList");
        Staff staff = null;
        int serialNumber = new Random().nextInt(90000) + 10000;
        try {
            // SETUP: Crea uno staff
            StaffDataList sdl = staffManager.getStaffDataList();
            assertNotNull(sdl, "StaffDataList non deve essere null.");
            sdl.insertStaffData(serialNumber, "Nome Originale", "original@email.com", "123", "ORIG", "Original", true, false);

            staff = Staff.loadStaff(serialNumber);
            assertNotNull(staff, "Setup fallito: impossibile creare lo staff da modificare.");

            // AZIONE: Modifica i dati usando il metodo di StaffDataList
            String newName = "Nome Modificato";
            String newEmail = "modified@email.com";
            boolean updated = sdl.updateStaffData(staff, newName, newEmail, staff.getPhoneNumber(),
                    staff.getTaxCode(), staff.getPrimaryMansion(), staff.isAvailable(), staff.isPermanent());
            assertTrue(updated, "updateStaffData della lista dovrebbe ritornare true.");

            // VERIFICA: Ricarica e controlla i nuovi dati
            Staff updatedStaff = Staff.loadStaff(serialNumber);
            assertNotNull(updatedStaff, "Lo staff modificato dovrebbe essere caricabile.");
            assertEquals(newName, updatedStaff.getName(), "Il nome non è stato aggiornato correttamente.");
            assertEquals(newEmail, updatedStaff.getEmail(), "L'email non è stata aggiornata correttamente.");

        } catch (Exception e) {
            fail("Il test UC4a non doveva lanciare un'eccezione: " + e.getMessage(), e);
        } finally {
            if (staff != null) {
                staffManager.removeStaff(staff);
            }
        }
    }

    @Test
    @Order(6)
    void test_UC5_assignRole() {
        LOGGER.info("TEST: UC5 - Assegnazione ruolo a personale");
        Staff tempStaff = null;
        Role tempRole = null;
        try {
            int sNum = new Random().nextInt(90000) + 10000;
            tempStaff = new Staff(sNum, "Staff Assegnabile", "assign@test.com", "222", "ASGSFF", "Tester", true);
            assertTrue(tempStaff.save(), "Setup fallito: salvataggio staff.");
            
            String roleName = "Ruolo Assegnabile " + sNum;
            tempRole = Role.create(roleName, "Descrizione", Date.valueOf("2025-10-12"), false);
            Role.create(tempRole);
            assertTrue(tempRole.getId() > 0, "Setup fallito: salvataggio ruolo.");
            
            assertTrue(tempStaff.isAvailable(), "Pre-condizione: Lo staff deve essere disponibile.");
            assertFalse(tempRole.isAssigned(), "Pre-condizione: Il ruolo non deve essere assegnato.");

            staffManager.assignRole(tempStaff, tempRole);

            Role loadedRole = Role.loadRole(roleName);
            assertNotNull(loadedRole, "Il ruolo dovrebbe esistere nel DB.");
            assertTrue(loadedRole.isAssigned(), "Post-condizione: Il ruolo deve risultare assegnato.");
            assertEquals(tempStaff.getSerialNumber(), loadedRole.getStaff().getSerialNumber(), "Il ruolo deve essere assegnato allo staff corretto.");
            
        } catch (Exception e) {
            fail("Il test UC5 non doveva lanciare un'eccezione: " + e.getMessage(), e);
        } finally {
            if (tempRole != null) {
                tempRole.setWorker(null);
                tempRole.update();
                try { tempRole.delete(); } catch(RoleException e) {}
            }
            if (tempStaff != null) {
                tempStaff.delete();
            }
        }
    }

    @Test
    @Order(7) 
    void test_UC5b_deleteRoleAssignment() {
        LOGGER.info("TEST NUOVO: UC5b - Eliminazione assegnazione");
        Staff tempStaff = null;
        Role tempRole = null;
        try {
            // SETUP
            int sNum = new Random().nextInt(900000) + 100000;
            // Usa la firma corretta di addStaff
            assertTrue(staffManager.addStaff(sNum, "Staff con Assegnazione", "del-assign@test.com", "333", "DELASG", "Cameriere", false));
            tempStaff = Staff.loadStaff(sNum);
            assertNotNull(tempStaff, "Setup fallito: creazione staff.");
            
            // Usa la firma corretta di createRole
            String roleName = "Ruolo da Rimuovere " + sNum;
            tempRole = staffManager.createRole(null, roleName, "Descrizione", Date.valueOf("2025-11-20"), false);
            assertNotNull(tempRole, "Setup fallito: creazione ruolo.");

            staffManager.assignRole(tempStaff, tempRole);

            // Pre-condizioni
            Staff loadedStaff = Staff.loadStaff(tempStaff.getSerialNumber());
            assertFalse(loadedStaff.isAvailable(), "Pre-condizione: lo staff non deve essere disponibile.");
            assertTrue(tempRole.isAssigned(), "Pre-condizione: il ruolo deve essere assegnato.");

            // AZIONE
            staffManager.deleteRoleAssignment(tempStaff, tempRole);

            // VERIFICA post-condizioni
            Role loadedRole = Role.loadRole(tempRole.getName());
            loadedStaff = Staff.loadStaff(tempStaff.getSerialNumber());
            assertTrue(loadedStaff.isAvailable(), "Post-condizione: lo staff deve tornare disponibile.");
            assertFalse(loadedRole.isAssigned(), "Post-condizione: il ruolo non deve più essere assegnato.");

        } finally {
            if (tempRole != null) try { tempRole.delete(); } catch (RoleException e) {}
            if (tempStaff != null) staffManager.removeStaff(tempStaff);
        }
    }

    @Test
    @Order(8) 
    void test_UC7a_setPermanentStatus() {
        LOGGER.info("TEST NUOVO: UC7a - Modifica tipologia assunzione");
        Staff tempStaff = null;
        try {
            // SETUP
            int sNum = new Random().nextInt(900000) + 100000;
            // Usa la firma corretta di addStaff
            assertTrue(staffManager.addStaff(sNum, "Staff Occasionale", "occasional@test.com", "444", "OCCSFF", "Barista", false));
            tempStaff = Staff.loadStaff(sNum);
            assertNotNull(tempStaff, "Setup fallito: creazione staff.");
            assertFalse(tempStaff.isPermanent(), "Pre-condizione: lo staff deve essere occasionale.");

            // AZIONE
            staffManager.setPermanentStatus(tempStaff, true);
            
            // VERIFICA
            Staff loadedStaff = Staff.loadStaff(tempStaff.getSerialNumber());
            assertTrue(loadedStaff.isPermanent(), "Post-condizione: lo staff deve essere diventato permanente.");

            // Azione inversa per completezza
            staffManager.setPermanentStatus(loadedStaff, false);
            loadedStaff = Staff.loadStaff(tempStaff.getSerialNumber());
            assertFalse(loadedStaff.isPermanent(), "Post-condizione: lo staff deve tornare occasionale.");
            
        } finally {
            if (tempStaff != null) {
                staffManager.removeStaff(tempStaff);
            }
        }
    }
}