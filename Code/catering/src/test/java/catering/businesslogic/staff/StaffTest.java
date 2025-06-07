package catering.businesslogic.staff;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.sql.Date;
import java.sql.Time;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.Event;
import catering.businesslogic.event.Service;
import catering.businesslogic.shift.Shift;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

@TestMethodOrder(OrderAnnotation.class)
public class StaffTest {

    private static final Logger LOGGER = LogManager.getLogger(StaffTest.class);
    private static CatERing app;
    private static User organizer;

    @BeforeAll
    static void init() {
        // Inizializza il database usando il file SQL fornito
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
    }

    @BeforeEach
    void setup() {
        try {
            // Carica l'utente 'Giovanni' che ha il ruolo di ORGANIZZATORE
            organizer = User.load("Giovanni");
            assertNotNull(organizer, "L'utente organizzatore non dovrebbe essere null");
            assertTrue(organizer.isOrganizer(), "L'utente dovrebbe avere il ruolo di organizzatore");

            // Esegui il login come organizzatore prima di ogni test
            app.getUserManager().fakeLogin(organizer.getUserName());
            assertEquals(organizer, app.getUserManager().getCurrentUser(), "L'utente corrente dovrebbe essere l'organizzatore");

        } catch (UseCaseLogicException e) {
            LOGGER.severe("Errore nel setup del test: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    void test01_StaffManagerInitialization() {
        LOGGER.info("TEST: Inizializzazione dello StaffManager");

        // Ottiene lo StaffManager, che viene creato pigramente dopo il login
        StaffManager staffManager = app.getStaffManager();

        assertNotNull(staffManager, "Lo StaffManager non dovrebbe essere null dopo il login di un utente autorizzato");
        assertNotNull(staffManager.getStaffDataList(), "La lista dati dello staff non dovrebbe essere null");

        // Verifica che il proprietario della lista sia l'utente che ha eseguito il login
        assertEquals(organizer, staffManager.getStaffDataList().getOwner(), "Il proprietario della lista dati dovrebbe essere l'organizzatore");
        LOGGER.info("StaffManager inizializzato correttamente per l'utente: " + organizer.getUserName());
    }

    @Test
    @Order(2)
    void test02_AddStaff() {
        LOGGER.info("TEST: Aggiunta di un nuovo membro dello staff");
        StaffManager staffManager = app.getStaffManager();
        assertNotNull(staffManager);

        int initialSize = staffManager.getStaffDataList().getStaffDataList().size();
        int serialNumber = 101;
        
        // Aggiunge un nuovo membro dello staff
        boolean added = staffManager.addStaff(serialNumber, "Mario Rossi", "mario.rossi@email.com", "3331234567",
                "RSSMRA80A01H501U", "Cuoco", false, true);
        
        assertTrue(added, "L'aggiunta dello staff dovrebbe ritornare true");
        
        // Verifica che il nuovo membro sia stato aggiunto
        int newSize = staffManager.getStaffDataList().getStaffDataList().size();
        assertEquals(initialSize + 1, newSize, "La dimensione della lista dello staff dovrebbe essere aumentata di 1");
        
        Staff addedStaff = staffManager.getStaff(serialNumber);
        assertNotNull(addedStaff, "Il membro dello staff aggiunto dovrebbe essere trovabile tramite matricola");
        assertEquals("Mario Rossi", addedStaff.getName(), "Il nome dello staff aggiunto non è corretto");
        LOGGER.info("Membro dello staff aggiunto con successo: " + addedStaff);
    }

    @Test
    @Order(3)
    void test03_UpdateStaff() {
        LOGGER.info("TEST: Modifica di un membro dello staff esistente");
        StaffManager staffManager = app.getStaffManager();
        assertNotNull(staffManager);

        int serialNumber = 102;
        staffManager.addStaff(serialNumber, "Giulia Bianchi", "giulia.bianchi@email.com", "3477654321",
                "BNCGLI85M41H501Z", "Cameriera", true, false);

        Staff staffToUpdate = staffManager.getStaff(serialNumber);
        assertNotNull(staffToUpdate, "Lo staff da modificare deve esistere");

        // Modifica i dati
        String newEmail = "giulia.b@newemail.com";
        staffToUpdate.setEmail(newEmail);
        boolean updated = staffToUpdate.update(); // Il metodo update è in Staff.java

        assertTrue(updated, "La modifica dovrebbe ritornare true");
        
        // Ricarica lo staff dal DB per verificare la modifica
        Staff updatedStaff = Staff.loadStaff(serialNumber);
        assertEquals(newEmail, updatedStaff.getEmail(), "L'email dello staff dovrebbe essere stata aggiornata");
        LOGGER.info("Membro dello staff modificato con successo: " + updatedStaff);
    }

    @Test
    @Order(4)
    void test04_RemoveStaff() {
        LOGGER.info("TEST: Rimozione di un membro dello staff");
        StaffManager staffManager = app.getStaffManager();
        assertNotNull(staffManager);

        int serialNumber = 103;
        staffManager.addStaff(serialNumber, "Luca Verdi", "luca.verdi@email.com", "3391122334",
                "VRDLCU90A01H501A", "Barista", true, true);
        
        Staff staffToRemove = staffManager.getStaff(serialNumber);
        assertNotNull(staffToRemove, "Lo staff da rimuovere deve esistere");

        boolean removed = staffManager.removeStaff(staffToRemove);
        assertTrue(removed, "La rimozione dovrebbe ritornare true");

        // Verifica che lo staff sia stato rimosso
        Staff reloadedStaff = staffManager.getStaff(serialNumber);
        assertNull(reloadedStaff, "Lo staff rimosso non dovrebbe più essere presente nella lista");
        LOGGER.info("Membro dello staff rimosso con successo.");
    }

    @Test
    @Order(5)
    void test05_CreateAndAssignRole() {
        LOGGER.info("TEST: Creazione e assegnazione di un ruolo");
        StaffManager staffManager = app.getStaffManager();
        assertNotNull(staffManager);

        // 1. Aggiungi un membro dello staff a cui assegnare il ruolo
        int serialNumber = 104;
        staffManager.addStaff(serialNumber, "Chiara Neri", "chiara.neri@email.com", "3385566778",
                "NRECHI92A41H501B", "Sommelier", true, false);
        Staff staffMember = staffManager.getStaff(serialNumber);
        assertNotNull(staffMember, "Il membro dello staff deve esistere");

        // 2. Crea un nuovo ruolo
        Role newRole = staffManager.createRole(staffMember,"Responsabile Vini", "Responsabile della cantina per l'evento", Date.valueOf("2025-06-15"));
        assertNotNull(newRole, "Il ruolo creato non dovrebbe essere null");
        assertFalse(newRole.isAssigned(), "Un nuovo ruolo non dovrebbe essere già assegnato");

        // 3. Assegna il ruolo
        staffManager.assignRole(staffMember, newRole);
        assertTrue(newRole.isAssigned(), "Il ruolo dovrebbe risultare assegnato");
        assertEquals(staffMember, newRole.getStaff(), "Il ruolo dovrebbe essere assegnato al corretto membro dello staff");

        LOGGER.info("Ruolo '" + newRole.getName() + "' creato e assegnato con successo a: " + staffMember.getName());
    }
}