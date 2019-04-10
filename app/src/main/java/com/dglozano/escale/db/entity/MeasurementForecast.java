package com.dglozano.escale.db.entity;

import com.dglozano.escale.db.pojo.MeasurementForecastError;

import java.util.Date;
import java.util.List;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import lombok.NonNull;
import lombok.ToString;

@ToString
@Entity
public class MeasurementForecast {

    @PrimaryKey
    @NonNull
    private Long id;
    private Long patientId;
    private String precision;
    private Date dateCalculated;
    private Date dateOfLastMeasurementUsed;

    @Embedded
    private MeasurementForecastError error;

    @Ignore
    private List<MeasurementPrediction> nextDaysPredictions;

    public MeasurementForecast() {
    }

    @Ignore
    public MeasurementForecast(Long id, Long patientId,
                               String precision,
                               MeasurementForecastError error,
                               Date dateCalculated,
                               Date dateOfLastMeasurementUsed) {
        this.id = id;
        this.patientId = patientId;
        this.precision = precision;
        this.error = error;
        this.dateCalculated = dateCalculated;
        this.dateOfLastMeasurementUsed = dateOfLastMeasurementUsed;
    }

    public MeasurementForecastError getError() {
        return error;
    }

    public void setError(MeasurementForecastError error) {
        this.error = error;
    }

    public List<MeasurementPrediction> getNextDaysPredictions() {
        return nextDaysPredictions;
    }

    public void setNextDaysPredictions(List<MeasurementPrediction> nextDaysPredictions) {
        this.nextDaysPredictions = nextDaysPredictions;
    }

    public Date getDateOfLastMeasurementUsed() {
        return dateOfLastMeasurementUsed;
    }

    public void setDateOfLastMeasurementUsed(Date dateOfLastMeasurementUsed) {
        this.dateOfLastMeasurementUsed = dateOfLastMeasurementUsed;
    }

    public Date getDateCalculated() {
        return dateCalculated;
    }

    public void setDateCalculated(Date dateCalculated) {
        this.dateCalculated = dateCalculated;
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

    public String getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }
}

