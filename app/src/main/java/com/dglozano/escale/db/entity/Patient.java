package com.dglozano.escale.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.dglozano.escale.web.dto.PatientDTO;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = Doctor.class,
        parentColumns = "id",
        childColumns = "doctorId",
        onDelete = CASCADE))
public class Patient extends AppUser {

    private Gender gender;
    private int scaleUserPin;
    private int scaleUserIndex;
    private int heightInCm;
    private int physicalActivity;
    private Date birthday;
    private boolean changedDefaultPassword;
    private Long doctorId;

    public Patient() {
    }

    @Ignore
    public Patient(PatientDTO patientDTO, Date timestamp) {
        super(patientDTO.getId(),
                patientDTO.getFirstName(),
                patientDTO.getLastName(),
                patientDTO.getEmail(),
                timestamp);
        this.gender = patientDTO.getGender();
        this.scaleUserIndex = patientDTO.getScaleUserIndex();
        this.scaleUserPin = patientDTO.getScaleUserPin();
        this.heightInCm = patientDTO.getHeightInCm();
        this.physicalActivity = patientDTO.getPhysicalActivity();
        this.birthday = patientDTO.getBirthday();
        this.doctorId = patientDTO.getDoctorDTO().getId();
        this.changedDefaultPassword = patientDTO.hasChangedDefaultPassword();
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

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
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

    public boolean hasChangedDefaultPassword() {
        return changedDefaultPassword;
    }

    public void setChangedDefaultPassword(boolean changedDefaultPassword) {
        this.changedDefaultPassword = changedDefaultPassword;
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
                "   height: %s \n" +
                "   physicalactivity: %s \n" +
                "}", id, firstName, lastName, email,scaleUserIndex,gender, heightInCm, physicalActivity);
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
