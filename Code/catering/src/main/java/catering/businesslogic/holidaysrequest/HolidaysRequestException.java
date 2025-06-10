package catering.businesslogic.holidaysrequest;

/**
 * Thrown to indicate a business rule violation during an operation
 * on a {@link HolidaysRequest} object.
 */
public class HolidaysRequestException extends RuntimeException {
    /**
     * Constructs a new HolidaysRequestException with the specified detail message.
     * @param message The detail message.
     */
    public HolidaysRequestException(String message) {
        super(message);
    }
}