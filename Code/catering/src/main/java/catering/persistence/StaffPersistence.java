package catering.persistence;

import java.sql.SQLException;

import catering.businesslogic.staff.Role;
import catering.businesslogic.staff.RoleException;
import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffDataList;
import catering.businesslogic.staff.StaffEventReceiver;

public class StaffPersistence implements StaffEventReceiver {

    @Override
    public void updateStaffAdded(Staff s) {
        // Quando un nuovo staff viene creato nella logica,
        // questo metodo lo salva permanentemente nel DB.
        s.save();
    }

    @Override
    public void updateStaffRemoved(Staff s) {
        // Quando uno staff viene rimosso, lo cancella dal DB.
        s.delete();
    }

    @Override
    public void updateRoleCreated(Role rl) {
        // Quando un nuovo ruolo viene creato, lo salva nel DB.
        // Usa il metodo statico create che abbiamo definito.
        try {
            Role.create(rl);
        } catch (SQLException e) {
            // In un'applicazione reale, qui si gestirebbe l'eccezione
            e.printStackTrace();
        }
    }

    @Override
    public void updateRoleDeleted(Role rl) {
        // Quando un ruolo viene cancellato...
        try {
            rl.delete();
        } catch (RoleException e) {
            // ...gestisce l'eccezione se il ruolo è in uso.
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateRoleAssigned(Role rl) {
        // Salva lo stato del ruolo (assegnato) e del lavoratore (non disponibile)
        rl.getStaff().update();
        rl.update();
    }

    @Override
    public void updateRoleUnassigned(Role rl, Staff s) {
        // Salva lo stato del ruolo (non assegnato) e del lavoratore (disponibile)
        s.update();
        rl.update();
    }

    @Override
    public void updateRoleReassigned(Role rl, Staff s) {
        // Salva lo stato del vecchio lavoratore, del nuovo e del ruolo
        s.update();
        rl.getStaff().update(); // rl.getStaff() ora ritorna il *nuovo* lavoratore
        rl.update();
    }

    @Override
    public void updateStaffDataListCreated(StaffDataList sdl) {
        // Questo evento non è strettamente necessario se le singole aggiunte
        // vengono gestite, ma per completezza potrebbe salvare l'intera lista di associazioni.
        sdl.save();
    }

    @Override
    public void updateStaffDataListDeleted(StaffDataList sdl) {
        // Se l'intera lista di un utente venisse cancellata, questo metodo
        // rimuoverebbe tutte le associazioni per quell'owner.
        sdl.delete();
    }

    @Override
    public void updateStaffDataAdded(Staff s, StaffDataList sdl) {
        // Quando un membro dello staff (s) viene aggiunto a una lista (sdl),
        // crea la singola riga di associazione nella tabella StaffDataList.
        // Un membro è stato aggiunto alla lista, quindi salviamo l'associazione.
        sdl.addStaffAssociation(s);
    }

    @Override
    public void updateStaffDataUpdated(Staff s, StaffDataList sdl) {
        // Quando i dati di uno Staff vengono aggiornati,
        // questo metodo si assicura che le modifiche vengano salvate nel DB.
        s.update();
    }

    @Override
    public void updateStaffDataDeleted(Staff s, StaffDataList sdl) {
        // Quando un membro dello staff (s) viene rimosso da una lista (sdl),
        // cancella la singola riga di associazione.
        sdl.removeStaffAssociation(s);
    }
}