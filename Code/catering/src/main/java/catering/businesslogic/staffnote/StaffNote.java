package catering.businesslogic.staffnote;

import catering.businesslogic.user.User;
import catering.businesslogic.staff.Staff;

import java.sql.Date;

public class StaffNote {

    private Staff worker;
    private String description;
    private Date date;
    private User owner;

    public static StaffNote create(User owner, Staff worker, String description, Date date) {
        StaffNote note = new StaffNote();
        note.owner = owner;
        note.worker = worker;
        note.description = description;
        note.date = date;
        return note;
    }

    public Staff getStaff() {
        return worker;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    public void setWorker(Staff worker) {
        this.worker = worker;
    }

    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isOwner(User user) {
        return this.owner != null && this.owner.equals(user);
    }
}
