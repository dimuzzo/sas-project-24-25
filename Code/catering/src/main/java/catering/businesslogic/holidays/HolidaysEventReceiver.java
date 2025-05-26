package catering.businesslogic.holidays;

public interface HolidaysEventReceiver {
    void updateHolidaysCreated(Holidays h);
    void updateHolidaysDeleted(Holidays h);
}

