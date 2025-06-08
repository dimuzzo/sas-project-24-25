package catering.businesslogic.holidaysrequest;

public interface HolidaysRequestEventReceiver {
    void updateHolidaysRequestCreated(HolidaysRequest hr);
    void updateHolidaysRequestAssigned(HolidaysRequest hr);
    void updateHolidaysRequestDeleted(HolidaysRequest hr);
}

