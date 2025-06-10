/**
 * * @author Alessandro Demo, Matricola 1049825
 */
package catering.businesslogic.summaryform;

/**
 * An interface for receiving events related to SummaryForm management.
 */
public interface SummaryFormEventReceiver {
    /**
     * Called when a new SummaryForm has been created.
     * @param sf The newly created SummaryForm.
     */
    void updateSummaryFormCreated(SummaryForm sf);

    /**
     * Called when a SummaryForm has been updated (e.g., an event or role was added).
     * @param sf The updated SummaryForm.
     */
    void updateSummaryFormUpdated(SummaryForm sf);

    /**
     * Called when a SummaryForm has been deleted.
     * @param sf The SummaryForm that was deleted.
     */
    void updateSummaryFormDeleted(SummaryForm sf);
}