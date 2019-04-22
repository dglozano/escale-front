package com.dglozano.escale.db.entity;

import java.util.Date;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class AppUser {

    @PrimaryKey
    protected Long id;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected Date lastUpdate;
    private boolean changedDefaultPassword;

    public AppUser() {
    }

    @Ignore
    public AppUser(Long id, Date lastUpdate) {
        this.id = id;
        this.lastUpdate = lastUpdate;
        this.changedDefaultPassword = true;
    }

    @Ignore
    public AppUser(Long id, String firstName, String lastName, String email, Date lastUpdate, Boolean hasChangedDefaultPassword) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.lastUpdate = lastUpdate;
        this.changedDefaultPassword = hasChangedDefaultPassword;
    }

    @Ignore
    public AppUser(AppUser appUser) {
        this.id = appUser.getId();
        this.firstName = appUser.getFirstName();
        this.lastName = appUser.getLastName();
        this.email = appUser.getEmail();
        this.lastUpdate = appUser.getLastUpdate();
        this.changedDefaultPassword = appUser.hasChangedDefaultPassword();
    }

    public boolean hasChangedDefaultPassword() {
        return changedDefaultPassword;
    }

    public void setChangedDefaultPassword(boolean changedDefaultPassword) {
        this.changedDefaultPassword = changedDefaultPassword;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
