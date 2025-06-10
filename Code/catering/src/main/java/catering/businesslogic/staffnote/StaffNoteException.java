/**
 * * @author Alessandro Demo, Matricola 1049825
 */
package catering.businesslogic.staffnote;

/**
 * Thrown to indicate a business rule violation during an operation
 * on a {@link StaffNote} object (e.g., editing a note that does not belong to a specific worker).
 */
public class StaffNoteException extends RuntimeException {
    /**
     * Constructs a new StaffNoteException with the specified detail message.
     * @param message The detail message.
     */
    public StaffNoteException(String message) {
        super(message);
    }
}