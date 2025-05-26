package catering.businesslogic.summaryform;

import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import catering.util.LogManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import catering.businesslogic.event.Event;
import catering.businesslogic.staff.Role;

public class SummaryForm {
    private String description;
    private boolean inUse;
    private User owner;

    private List<Event> associatedEvents;
    private List<Role> associatedRoles;

    public SummaryForm(User owner, String description) {
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
