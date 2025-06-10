package catering.businesslogic.summaryform;

/**
 * Thrown to indicate a business rule violation during an operation
 * on a {@link SummaryForm} object.
 */
public class SummaryFormException extends RuntimeException {
    /**
     * Constructs a new SummaryFormException with the specified detail message.
     * @param message the detail message.
     */
    public SummaryFormException(String message) {
        super(message);
    }
}