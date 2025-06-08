package catering.businesslogic.staff;

import java.sql.Date;
import java.sql.SQLException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

/**
 * Test di validazione per il caso d'uso "Gestire il Personale".
 * Ogni test simula uno scenario specifico e verifica le pre-condizioni e post-condizioni
 * definite nei contratti dell'Allegato Tecnico.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StaffTest {

    private static final Logger LOGGER = LogManager.getLogger(StaffTest.class);
    private static CatERing app;
    private static User organizer;
    private static StaffManager staffManager;

    // Variabili statiche per mantenere lo stato tra i test ordinati,
    // simulando una sessione di lavoro continua.
    private static Staff testStaffMember;
    private static Role testRole;

    @BeforeAll
    static void init() {
        // Inizializza il DB una sola volta per tutti i test
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
    }

    @BeforeEach
    void setup() {
        try {
            // Esegue il login come organizzatore prima di ogni test
            organizer = User.load("Giovanni");
            app.getUserManager().fakeLogin(organizer.getUserName());
            staffManager = app.getStaffManager(); // Il manager viene creato/recuperato qui
            assertNotNull(staffManager, "Lo StaffManager non dovrebbe essere null per un organizzatore loggato.");
        } catch (UseCaseLogicException e) {
            LOGGER.severe("Errore nel setup del test: " + e.getMessage());
            fail("Setup fallito: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    @DisplayName("UC 4: inserisciDatiPersonale - Verifica Contratto")
    void test_UC4_inserisciDatiPersonale() {
        LOGGER.info("TEST: Validazione UC4 - Inserimento dati personale");
        
        // Dati del nuovo membro dello staff
        int serialNumber = 101;
        String name = "Mario Rossi";
        
        // 1. Azione: Esegue l'aggiunta e verifica che l'operazione abbia successo
        boolean added = staffManager.addStaff(serialNumber, name, "m.rossi@test.it", "333",
                "RSSMRA", "Cuoco", true, false);
        assertTrue(added, "Il metodo addStaff dovrebbe ritornare true in caso di successo.");

        // 2. Recupero: Carica lo staff appena inserito per poterlo ispezionare
        testStaffMember = staffManager.getStaff(serialNumber);

        // 3. Verifica: Controlla che le post-condizioni del contratto 4 siano rispettate
        assertNotNull(testStaffMember, "Post-condizione fallita: Deve essere creata un'istanza p di Personale.");
        assertEquals(serialNumber, testStaffMember.getSerialNumber(), "Post-condizione fallita: p.matricola deve essere corretta.");
        assertEquals(name, testStaffMember.getName(), "Post-condizione fallita: p.nome deve essere corretto.");
        assertTrue(testStaffMember.isAvailable(), "Post-condizione fallita: p.disponibile deve essere 'si'.");
    }

    @Test
    @Order(2)
    @DisplayName("UC 2: scriveRuolo - Verifica Contratto")
    void test_UC2_scriveRuolo() throws SQLException {
        LOGGER.info("TEST: Validazione UC2 - Scrittura di un ruolo");
        
        // Azione: 2. Scrive un ruolo utile all'evento
        testRole = staffManager.createRole(null, "Responsabile Vini", "Gestione cantina per evento", Date.valueOf("2025-09-15"));
        Role.create(testRole); // Salva nel DB e recupera l'ID

        // Verifica Post-condizioni del contratto 2
        assertTrue(testRole.getId() > 0, "Post-condizione fallita: Deve essere creata un'istanza r di Ruolo (con ID > 0).");
        assertFalse(testRole.isAssigned(), "Post-condizione fallita: r.assegnato deve essere 'no'.");
    }

    @Test
    @Order(3)
    @DisplayName("UC 5: assegnaRuoloPersonaleDisponibile - Verifica Contratto")
    void test_UC5_assegnaRuolo() {
        LOGGER.info("TEST: Validazione UC5 - Assegnazione ruolo a personale");
        
        // Verifica Pre-condizioni del contratto 5
        assertNotNull(testStaffMember, "Pre-condizione fallita: Deve esistere un'istanza p di Personale.");
        assertTrue(testStaffMember.isAvailable(), "Pre-condizione fallita: p.disponibile deve essere 'si'.");
        assertNotNull(testRole, "Pre-condizione fallita: Deve esistere un'istanza r di Ruolo.");
        assertFalse(testRole.isAssigned(), "Pre-condizione fallita: r.assegnato deve essere 'no'.");

        // Azione: 5. Assegna un ruolo al personale disponibile
        staffManager.assignRole(testStaffMember, testRole);
        
        // Verifica Post-condizioni del contratto 5
        assertTrue(testRole.isAssigned(), "Post-condizione fallita: r.assegnato deve essere 'si'.");
        assertFalse(testStaffMember.isAvailable(), "Post-condizione fallita: p.disponibile deve essere 'no'.");
        assertEquals(testStaffMember, testRole.getStaff(), "Il ruolo deve essere assegnato al corretto membro dello staff.");
    }
    
    @Test
    @Order(4)
    @DisplayName("UC 2a / Eccezione 2a.1a: eliminaRuolo in uso - Verifica Contratto")
    void test_UC2a_eliminaRuolo_Fallimento_InUso() {
        LOGGER.info("TEST: Validazione Eccezione 2a.1a - Eliminazione ruolo in uso");

        // Pre-condizione: il ruolo è in uso (assegnato nel test precedente)
        assertNotNull(testRole, "Il ruolo di test deve esistere.");
        assertTrue(testRole.isAssigned(), "Pre-condizione fallita: il ruolo deve essere in uso.");

        // Azione e Verifica Eccezione 2a.1a
        // Il tentativo di eliminare un ruolo in uso deve lanciare un'eccezione
        assertThrows(RoleException.class, () -> {
            testRole.delete();
        }, "L'eliminazione di un ruolo in uso deve lanciare una RoleException.");
    }
}