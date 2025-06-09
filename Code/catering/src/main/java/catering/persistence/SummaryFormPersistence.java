package catering.persistence;

import catering.businesslogic.summaryform.SummaryForm;
import catering.businesslogic.summaryform.SummaryFormEventReceiver;

public class SummaryFormPersistence implements SummaryFormEventReceiver {
    
    @Override
    public void updateSummaryFormCreated(SummaryForm sf) {
        sf.save();
    }

    @Override
    public void updateSummaryFormDeleted(SummaryForm sf) {
        sf.delete();
    }
}