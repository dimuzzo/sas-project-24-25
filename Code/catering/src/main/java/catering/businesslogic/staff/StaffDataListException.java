package catering.businesslogic.staff;

/**
 * Thrown to indicate that a business rule has been violated during an operation
 * on a {@link StaffDataList} object.
 * For example, this exception could be thrown when attempting to add a staff member
 * that already exists in the list, or if an unauthorized user tries to modify it.
 * As it extends {@link RuntimeException}, it is an unchecked exception.
 */
public class StaffDataListException extends RuntimeException {

    /**
     * Constructs a new StaffDataListException with the specified detail message.
     * @param message the detail message. The detail message is saved for
     * later retrieval by the {@link #getMessage()} method.
     */
    public StaffDataListException(String message) {
        super(message);
    }
}