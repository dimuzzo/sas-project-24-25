package catering.businesslogic.staff;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Random;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

@TestMethodOrder(OrderAnnotation.class)
public class StaffTest {

    private static final Logger LOGGER = LogManager.getLogger(StaffTest.class);

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        CatERing.getInstance();
    }

    @BeforeEach
    void setup() {
        LOGGER.info("Executing new test...");
    }

    @Test
    @Order(1)
    void testCreateAndLoadStaff() {
        LOGGER.info("TEST: Creazione, salvataggio e caricamento di un membro dello staff");
        Staff staff = null;
        int serialNumber = new Random().nextInt(90000) + 10000;
        String name = "Test Staff " + serialNumber;

        try {
            staff = new Staff(serialNumber, name, "test@staff.it", "12345", "TSTSTF", "Tester", true);
            
            // 1. Verifica immediata del salvataggio
            boolean saved = staff.save();
            assertTrue(saved, "staff.save() dovrebbe ritornare true. Se fallisce qui, il problema è nel PersistenceManager o nello schema DB.");

            // 2. Verifica che l'oggetto sia ora recuperabile
            Staff loadedStaff = Staff.loadStaff(serialNumber);
            assertNotNull(loadedStaff, "Lo staff caricato non dovrebbe essere null. Se lo è, la scrittura su DB non è avvenuta.");
            assertEquals(name, loadedStaff.getName());

        } catch (Exception e) {
            fail("Test fallito a causa di un'eccezione inaspettata.", e);
        } finally {
            if (staff != null) {
                staff.delete();
            }
        }
    }

    @Test
    @Order(2)
    void testCreateAndLoadRole() {
        LOGGER.info("TEST: Creazione, salvataggio e caricamento di un ruolo");
        Role role = null;
        String roleName = "Test Role " + System.currentTimeMillis();

        try {
            role = Role.create(roleName, "Descrizione di test", Date.valueOf("2025-11-15"), false);
            
            // 1. Esegui il salvataggio
            Role.create(role);

            // 2. Verifica l'effetto del salvataggio provando a ricaricare subito l'oggetto
            Role loadedRole = Role.loadRole(roleName);
            assertNotNull(loadedRole, "Il ruolo non è stato trovato nel DB subito dopo la sua creazione. Controlla PersistenceManager.");
            
            // 3. Solo se il caricamento ha successo, verifica l'ID
            assertTrue(loadedRole.getId() > 0, "Il ruolo caricato dal DB deve avere un ID valido (> 0).");
            assertEquals(roleName, loadedRole.getName());

        } catch (SQLException e) {
            fail("Test fallito a causa di un'eccezione SQL.", e);
        } finally {
            if (role != null) {
                // Per la pulizia, ricarichiamo il ruolo per essere sicuri di avere l'ID corretto
                Role roleToDelete = Role.loadRole(roleName);
                if (roleToDelete != null) {
                    try {
                        roleToDelete.delete();
                    } catch (RoleException ex) {
                        // ignora
                    }
                }
            }
        }
    }

    @Test
    @Order(3)
    void testAssignRoleToStaff() {
        LOGGER.info("TEST: Assegnazione di un ruolo a un membro dello staff");
        Staff staff = null;
        Role role = null;
        int serialNumber = new Random().nextInt(90000) + 10000;
        String roleName = "Assignable Role " + serialNumber;

        try {
            // Setup
            staff = new Staff(serialNumber, "Staff Assegnabile", "assign@test.it", "555", "ASSGN", "Cook", false);
            assertTrue(staff.save(), "Setup fallito: impossibile salvare lo staff.");
            
            role = Role.create(roleName, "Ruolo da assegnare", Date.valueOf("2025-12-01"), false);
            Role.create(role);
            
            Role roleToAssign = Role.loadRole(roleName);
            assertNotNull(roleToAssign, "Setup fallito: impossibile caricare il ruolo appena creato.");

            // Azione
            roleToAssign.setWorker(staff);
            boolean updated = roleToAssign.update();
            assertTrue(updated, "role.update() dovrebbe ritornare true dopo un'assegnazione.");

            // Verifica
            Role loadedRole = Role.loadRole(roleName);
            assertNotNull(loadedRole, "Il ruolo assegnato non è stato trovato nel DB.");
            assertTrue(loadedRole.isAssigned(), "Il ruolo dovrebbe risultare assegnato.");
            assertEquals(staff.getSerialNumber(), loadedRole.getStaff().getSerialNumber());

        } catch (Exception e) {
            fail("Test fallito a causa di un'eccezione inaspettata.", e);
        } finally {
            // Pulizia
            if (role != null) {
                 Role roleToClean = Role.loadRole(roleName);
                 if(roleToClean != null) {
                    roleToClean.setWorker(null);
                    roleToClean.update();
                    try { roleToClean.delete(); } catch (RoleException e) {}
                 }
            }
            if (staff != null) {
                staff.delete();
            }
        }
    }

    @Test
    @Order(4)
    void testDeleteAssignedRoleThrowsException() {
        LOGGER.info("TEST: L'eliminazione di un ruolo assegnato deve lanciare un'eccezione");
        Staff staff = null;
        Role role = null;
        int serialNumber = new Random().nextInt(90000) + 10000;
        String roleName = "Protected Role " + serialNumber;
        
        try {
            // Setup
            staff = new Staff(serialNumber, "Staff con Ruolo", "protect@test.it", "555", "PRTCT", "Guard", true);
            assertTrue(staff.save(), "Setup fallito: impossibile salvare lo staff per il test.");

            role = Role.create(roleName, "Ruolo protetto", Date.valueOf("2026-01-01"), false);
            Role.create(role);

            Role assignedRole = Role.loadRole(roleName);
            assertNotNull(assignedRole, "Setup fallito: impossibile caricare il ruolo appena creato.");
            
            assignedRole.setWorker(staff);
            assertTrue(assignedRole.update(), "Setup fallito: impossibile assegnare il ruolo.");

            // Azione e Verifica
            assertThrows(RoleException.class, assignedRole::delete, "Deve essere lanciata una RoleException.");
        } catch (Exception e) {
            fail("Test fallito a causa di un'eccezione inaspettata durante il setup.", e);
        } finally {
             // Pulizia
            if (role != null) {
                Role roleToClean = Role.loadRole(roleName);
                if (roleToClean != null) {
                    roleToClean.setWorker(null);
                    roleToClean.update();
                    try { roleToClean.delete(); } catch (RoleException e) {}
                }
            }
            if (staff != null) {
                staff.delete();
            }
        }
    }
}