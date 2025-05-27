package catering.businesslogic.summaryform;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.Event;
import catering.businesslogic.user.User;

import java.util.ArrayList;
import java.util.List;

public class SummaryFormManager {
    private List<SummaryFormEventReceiver> receivers = new ArrayList<>();

    public void addReceiver(SummaryFormEventReceiver er) {
        receivers.add(er);
    }

    public void removeReceiver(SummaryFormEventReceiver er) {
        receivers.remove(er);
    }

    private void notifySummaryFormCreated(SummaryForm sf) {
        for (SummaryFormEventReceiver r : receivers) r.updateSummaryFormCreated(sf);
    }

    private void notifySummaryFormDeleted(SummaryForm sf) {
        for (SummaryFormEventReceiver r : receivers) r.updateSummaryFormDeleted(sf);
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
