package com.dglozano.escale.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.dglozano.escale.web.dto.PatientDTO;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

@Entity
public class Patient {

    @PrimaryKey
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Gender gender;
    private int scaleUserPin;
    private int scaleUserIndex;
    private int heightInCm;
    private int physicalActivity;
    private Date birthday;
    private boolean changedDefaultPassword;
    private int doctorId;
    private Date lastUpdate;

    public Patient() {
    }

    @Ignore
    public Patient(Long id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    @Ignore
    public Patient(PatientDTO patientDTO, Date timestamp) {
        this.id = patientDTO.getId();
        this.firstName = patientDTO.getFirstName();
        this.lastName = patientDTO.getLastName();
        this.email = patientDTO.getEmail();
        this.gender = patientDTO.getGender();
        this.scaleUserIndex = patientDTO.getScaleUserIndex();
        this.scaleUserPin = patientDTO.getScaleUserPin();
        this.heightInCm = patientDTO.getHeightInCm();
        this.physicalActivity = patientDTO.getPhysicalActivity();
        this.birthday = patientDTO.getBirthday();
        this.changedDefaultPassword = patientDTO.hasChangedDefaultPassword();
        this.lastUpdate = timestamp;
    }

    public boolean hasChangedDefaultPassword() {
        return changedDefaultPassword;
    }

    public void setChangedDefaultPassword(boolean changedDefaultPassword) {
        this.changedDefaultPassword = changedDefaultPassword;
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

    public void setId(Long id) {
        this.id = id;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getScaleUserPin() {
        return scaleUserPin;
    }

    public void setScaleUserPin(int scaleUserPin) {
        this.scaleUserPin = scaleUserPin;
    }

    public int getScaleUserIndex() {
        return scaleUserIndex;
    }

    public void setScaleUserIndex(int scaleUserIndex) {
        this.scaleUserIndex = scaleUserIndex;
    }

    public int getHeightInCm() {
        return heightInCm;
    }

    public void setHeightInCm(int heightInCm) {
        this.heightInCm = heightInCm;
    }

    public int getPhysicalActivity() {
        return physicalActivity;
    }

    public void setPhysicalActivity(int physicalActivity) {
        this.physicalActivity = physicalActivity;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Patient)) return false;
        Patient otherPatient = (Patient) other;

        return otherPatient.id == this.id
                && otherPatient.firstName.equals(this.firstName)
                && otherPatient.lastName.equals(this.lastName)
                && otherPatient.email.equals(this.email);
    }

    public Long getId() {
        return id;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return String.format("{\n " +
                "   id: %s \n" +
                "   firstName: %s \n" +
                "   lastName: %s \n" +
                "   email: %s \n" +
                "   userindex: %s \n" +
                "   gender: %s \n" +
                "   changedpass: %s \n" +
                "   height: %s \n" +
                "   physicalactivity: %s \n" +
                "}", id, firstName, lastName, email,scaleUserIndex,gender,changedDefaultPassword, heightInCm, physicalActivity);
    }

    public enum Gender {
        @SerializedName("1")
        MALE,
        @SerializedName("2")
        FEMALE,
        @SerializedName("3")
        OTHER
    }
}
