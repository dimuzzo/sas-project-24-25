package catering.businesslogic.summaryform;

import java.util.ArrayList;
import java.util.List;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.Event;
import catering.businesslogic.user.User;

public class SummaryFormManager {
    private List<SummaryFormEventReceiver> eventReceivers = new ArrayList<>();

    public void addEventReceiver(SummaryFormEventReceiver er) {
        eventReceivers.add(er);
    }

    public void removeEventReceiver(SummaryFormEventReceiver er) {
        eventReceivers.remove(er);
    }

    private void notifySummaryFormCreated(SummaryForm sf) {
        for (SummaryFormEventReceiver r : eventReceivers) r.updateSummaryFormCreated(sf);
    }

    private void notifySummaryFormDeleted(SummaryForm sf) {
        for (SummaryFormEventReceiver r : eventReceivers) r.updateSummaryFormDeleted(sf);
    }

    public SummaryForm createSummaryForm(Event event, String description) throws UseCaseLogicException {
        User owner = CatERing.getInstance().getUserManager().getCurrentUser();

        if (!owner.isOrganizer())
            throw new UseCaseLogicException("User is not an organizer");

        if (event == null)
            throw new UseCaseLogicException("Event not specified");

        SummaryForm sf = SummaryForm.create(owner, description);
        notifySummaryFormCreated(sf);
        return sf;
    }
}
