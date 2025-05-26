package catering.businesslogic.summaryform;

public interface SummaryFormEventReceiver {
    void updateSummaryFormCreated(SummaryForm sf);
    void updateSummaryFormDeleted(SummaryForm sf);
}
