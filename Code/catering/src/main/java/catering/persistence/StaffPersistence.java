package catering.persistence;

import java.sql.SQLException;

import catering.businesslogic.staff.Role;
import catering.businesslogic.staff.RoleException;
import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffDataList;
import catering.businesslogic.staff.StaffEventReceiver;
import catering.businesslogic.user.User;

public class StaffPersistence implements StaffEventReceiver {

    // METODI GIÀ CORRETTI

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
        // Quando un ruolo viene assegnato o modificato, aggiorna il record nel DB.
        rl.update();
    }


    // METODI AGGIUNTI E COMPLETATI

    @Override
    public void updateStaffDataListCreated(StaffDataList sdl) {
        // Questo evento non è strettamente necessario se le singole aggiunte
        // vengono gestite, ma per completezza potrebbe salvare l'intera lista di associazioni.
        // Lasciato vuoto perché le operazioni atomiche sono gestite sotto.
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
        User owner = sdl.getOwner();
        if (owner != null) {
            String query = "INSERT INTO StaffDataList (owner_id, staff_serial_number) VALUES (?, ?)";
            PersistenceManager.executeUpdate(query, owner.getId(), s.getSerialNumber());
        }
    }

    @Override
    public void updateStaffDataUpdated(Staff s, StaffDataList sdl) {
        // L'aggiornamento dei dati di un membro dello staff (es. cambio email)
        // è gestito dal metodo s.update(). La tabella di associazione StaffDataList
        // non cambia, quindi questo metodo può rimanere vuoto.
    }

    @Override
    public void updateStaffDataDeleted(Staff s, StaffDataList sdl) {
        // Quando un membro dello staff (s) viene rimosso da una lista (sdl),
        // cancella la singola riga di associazione.
        User owner = sdl.getOwner();
        if (owner != null) {
            String query = "DELETE FROM StaffDataList WHERE owner_id = ? AND staff_serial_number = ?";
            PersistenceManager.executeUpdate(query, owner.getId(), s.getSerialNumber());
        }
    }
}