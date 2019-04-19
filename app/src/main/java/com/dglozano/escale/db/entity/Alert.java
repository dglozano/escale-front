package com.dglozano.escale.db.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;
import static com.dglozano.escale.db.entity.Alert.AlertType.FORECAST_PREDICTS_GOAL_WILL_FAIL;
import static com.dglozano.escale.db.entity.Alert.AlertType.GOAL_FAILED;
import static com.dglozano.escale.db.entity.Alert.AlertType.GOAL_SUCCESS;
import static com.dglozano.escale.db.entity.Alert.AlertType.MANUAL_MEASUREMENT;
import static com.dglozano.escale.db.entity.Alert.AlertType.NO_RECENT_MEASUREMENT;

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
    private AlertType alertType;
    private String message;

    public Alert() {
    }

    @Ignore
    public Alert(
            Long id, Long patientId, Long doctorId, AlertType alertType, String alertMsg, Date date) {
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

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public enum AlertType {
        @SerializedName("1")
        FORECAST_PREDICTS_GOAL_WILL_FAIL,
        @SerializedName("2")
        GOAL_FAILED,
        @SerializedName("3")
        MANUAL_MEASUREMENT,
        @SerializedName("4")
        NO_RECENT_MEASUREMENT,
        @SerializedName("5")
        GOAL_SUCCESS
    }

    public static Alert.AlertType intToAlertType(Integer i) {
        switch (i) {
            case 1:
                return FORECAST_PREDICTS_GOAL_WILL_FAIL;
            case 2:
                return GOAL_FAILED;
            case 3:
                return MANUAL_MEASUREMENT;
            case 4:
                return NO_RECENT_MEASUREMENT;
            case 5:
                return GOAL_SUCCESS;
            default:
                return null;
        }
    }

    public static Integer alertTypeToInt(AlertType alertType) {
        if (alertType == null) {
            return -1;
        } else {
            switch (alertType) {
                case FORECAST_PREDICTS_GOAL_WILL_FAIL:
                    return 1;
                case GOAL_FAILED:
                    return 2;
                case MANUAL_MEASUREMENT:
                    return 3;
                case NO_RECENT_MEASUREMENT:
                    return 4;
                case GOAL_SUCCESS:
                    return 5;
                default:
                    return -1;
            }
        }
    }
}
