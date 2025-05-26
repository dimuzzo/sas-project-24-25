package catering.businesslogic.summaryform;

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

    public SummaryForm createSummaryForm(User owner, String description) {
        SummaryForm sf = SummaryForm.create(owner, description);
        notifySummaryFormCreated(sf);
        return sf;
    }
}
