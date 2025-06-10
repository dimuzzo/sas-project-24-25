package catering.businesslogic.staff;

/**
 * Thrown to indicate that an operation on a Role object has violated a business rule.
 * For example, this exception is thrown when an attempt is made to delete a Role
 * that is currently assigned to a Staff member.
 * As it extends {@link RuntimeException}, it is an unchecked exception.
 */
public class RoleException extends RuntimeException {

    /**
     * Constructs a new RoleException with the specified detail message.
     * @param message the detail message. The detail message is saved for
     * later retrieval by the {@link #getMessage()} method.
     */
    public RoleException(String message) {
        super(message);
    }
}