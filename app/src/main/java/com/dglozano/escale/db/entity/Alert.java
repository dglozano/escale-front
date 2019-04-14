package com.dglozano.escale.db.entity;

import java.util.Date;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = {
        @ForeignKey(entity = Doctor.class,
                parentColumns = "id",
                childColumns = "doctorId",
                onDelete = CASCADE),
        @ForeignKey(entity = Patient.class,
                parentColumns = "id",
                childColumns = "patientId",
                onDelete = CASCADE)
})
public class Alert {

    @PrimaryKey
    private Long id;
    private Long patientId;
    private Long doctorId;
    private Date dateCreated;
    private boolean seenByDoctor;
    private boolean seenByPatient;
    private int alertType;
    private String message;

    public Alert() {
    }

    @Ignore
    public Alert(
            Long id, Long patientId, Long doctorId, int alertType, String alertMsg, Date date) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.alertType = alertType;
        this.message = alertMsg;
        this.dateCreated = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isSeenByDoctor() {
        return seenByDoctor;
    }

    public void setSeenByDoctor(boolean seenByDoctor) {
        this.seenByDoctor = seenByDoctor;
    }

    public boolean isSeenByPatient() {
        return seenByPatient;
    }

    public void setSeenByPatient(boolean seenByPatient) {
        this.seenByPatient = seenByPatient;
    }

    public int getAlertType() {
        return alertType;
    }

    public void setAlertType(int alertType) {
        this.alertType = alertType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
