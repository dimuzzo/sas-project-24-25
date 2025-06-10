package catering.businesslogic.summaryform;

import java.util.ArrayList;
import java.util.List;

import catering.businesslogic.CatERing;
import catering.businesslogic.event.Event;
import catering.businesslogic.staff.Role;
import catering.businesslogic.user.User;

/**
 * Manages the business logic for creating and handling SummaryForms.
 */
public class SummaryFormManager {

    private List<SummaryFormEventReceiver> eventReceivers = new ArrayList<>();
    private SummaryForm currentSummaryForm;

    /**
     * Adds an event receiver to be notified of summary form-related changes.
     * @param receiver The event receiver to add.
     */
    public void addEventReceiver(SummaryFormEventReceiver er) {
        eventReceivers.add(er);
    }

    /**
     * Removes an event receiver.
     * @param receiver The event receiver to remove.
     */
    public void removeEventReceiver(SummaryFormEventReceiver er) {
        eventReceivers.remove(er);
    }

    // Notifaction Methods

    private void notifySummaryFormCreated(SummaryForm sf) {
        for (SummaryFormEventReceiver r : eventReceivers) r.updateSummaryFormCreated(sf);
    }

    private void notifySummaryFormUpdated(SummaryForm sf) {
        for (SummaryFormEventReceiver r : eventReceivers) r.updateSummaryFormUpdated(sf);
    }

    private void notifySummaryFormDeleted(SummaryForm sf) {
        for (SummaryFormEventReceiver r : eventReceivers) r.updateSummaryFormDeleted(sf);
    }

    /**
     * Creates a new summary form for an event.
     * @param event The event to which the form is associated.
     * @param description A description for the form.
     * @return The newly created and persisted SummaryForm.
     * @throws SummaryFormException if the user is not an organizer or the event is null.
     */
    public SummaryForm createSummaryForm(Event event, String description) throws SummaryFormException {
        User owner = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!owner.isOrganizer()) {
            throw new SummaryFormException("User is not an organizer.");
        }
        if (event == null) {
            throw new SummaryFormException("Event cannot be null.");
        }

        SummaryForm sf = SummaryForm.create(owner, description);
        sf.addEvent(event); // Associates the event with the form
        
        this.currentSummaryForm = sf;
        notifySummaryFormCreated(sf);
        return sf;
    }

    /**
     * Deletes the currently selected summary form.
     * @throws SummaryFormException if no summary form is currently selected.
     */
    public void deleteSummaryForm() throws SummaryFormException {
        if (currentSummaryForm == null) {
            throw new SummaryFormException("No summary form selected to delete.");
        }
        notifySummaryFormDeleted(currentSummaryForm);
        currentSummaryForm = null;
    }
    
    /**
     * Adds an event to the current summary form.
     * @param event The event to add.
     * @throws SummaryFormException if no summary form is selected.
     */
    public void addEventToForm(Event event) throws SummaryFormException {
        if (currentSummaryForm == null) {
            throw new SummaryFormException("No summary form selected to add an event.");
        }
        currentSummaryForm.addEvent(event);
        notifySummaryFormUpdated(currentSummaryForm);
    }

    /**
     * Adds a role to the current summary form.
     * @param role The role to add.
     * @throws SummaryFormException if no summary form is selected.
     */
    public void addRoleToForm(Role role) throws SummaryFormException {
        if (currentSummaryForm == null) {
            throw new SummaryFormException("No summary form selected to add a role.");
        }
        currentSummaryForm.addRole(role);
        notifySummaryFormUpdated(currentSummaryForm);
    }
    
    public SummaryForm getCurrentSummaryForm() {
        return currentSummaryForm;
    }
}