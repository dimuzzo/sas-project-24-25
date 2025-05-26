package catering.businesslogic.summaryform;

import catering.businesslogic.user.User;
import catering.businesslogic.event.Event;
import catering.businesslogic.staff.Role;

import java.util.ArrayList;
import java.util.List;

public class SummaryForm {
    private String description;
    private boolean inUse;
    private User owner;

    private List<Event> associatedEvents;
    private List<Role> associatedRoles;

    // Factory method consigliato
    public static SummaryForm create(User owner, String description) {
        return new SummaryForm(owner, description);
    }

    // Costruttore reso private o package-private per forzare uso del factory
    SummaryForm(User owner, String description) {
        this.owner = owner;
        this.description = description;
        this.inUse = false;
        this.associatedEvents = new ArrayList<>();
        this.associatedRoles = new ArrayList<>();
    }

    public boolean isOwner(User user) {
        return this.owner != null && this.owner.equals(user);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public boolean containsEvent(Event ev) {
        return associatedEvents.contains(ev);
    }

    public boolean containsRole(Role r) {
        return associatedRoles.contains(r);
    }

    public void addEvent(Event ev) {
        if (!associatedEvents.contains(ev)) {
            associatedEvents.add(ev);
        }
    }

    public void addRole(Role r) {
        if (!associatedRoles.contains(r)) {
            associatedRoles.add(r);
        }
    }

    public List<Event> getAssociatedEvents() {
        return associatedEvents;
    }

    public List<Role> getAssociatedRoles() {
        return associatedRoles;
    }

    public User getOwner() {
        return owner;
    }
}
