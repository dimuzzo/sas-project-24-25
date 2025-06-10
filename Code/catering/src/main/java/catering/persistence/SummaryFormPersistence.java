/**
 * * @author Alessandro Demo, Matricola 1049825
 */
package catering.persistence;

import catering.businesslogic.summaryform.SummaryForm;
import catering.businesslogic.summaryform.SummaryFormEventReceiver;

/**
 * Handles database persistence for SummaryForm objects by listening to events.
 */
public class SummaryFormPersistence implements SummaryFormEventReceiver {
    
    @Override
    public void updateSummaryFormCreated(SummaryForm sf) {
        sf.save();
    }
    
    @Override
    public void updateSummaryFormUpdated(SummaryForm sf) {
        // The current logic only associates events/roles in memory.
        // If these associations were to be persisted, sf.update() would be called here.
        // For now, only the description and in_use fields are updatable.
        sf.update();
    }

    @Override
    public void updateSummaryFormDeleted(SummaryForm sf) {
        sf.delete();
    }
}