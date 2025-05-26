package catering.businesslogic.summaryform;

import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import catering.util.LogManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    public SummaryForm createSummaryForm(String description) {
        SummaryForm sf = new SummaryForm();
        sf.setDescription(description);
        notifySummaryFormCreated(sf);
        return sf;
    }
}

