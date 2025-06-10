package catering.businesslogic.holidaysrequest;

/**
 * An interface for receiving events related to {@link HolidaysRequest} management.
 */
public interface HolidaysRequestEventReceiver {
    /**
     * Called when a new holiday request has been created.
     * @param hr The newly created {@link HolidaysRequest}.
     */
    void updateHolidaysRequestCreated(HolidaysRequest hr);

    /**
     * Called when a holiday request has been assigned (approved).
     * @param hr The {@link HolidaysRequest} that was assigned.
     */
    void updateHolidaysRequestAssigned(HolidaysRequest hr);

    /**
     * Called when a holiday request has been deleted.
     * @param hr The {@link HolidaysRequest} that was deleted.
     */
    void updateHolidaysRequestDeleted(HolidaysRequest hr);
}