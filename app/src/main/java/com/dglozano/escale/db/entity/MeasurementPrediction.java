package com.dglozano.escale.db.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import lombok.NonNull;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = MeasurementForecast.class,
        parentColumns = "id",
        childColumns = "forecastId",
        onDelete = CASCADE))
public class MeasurementPrediction {

    @PrimaryKey
    @NonNull
    private Long id;
    private Long forecastId;
    private int daysOffset;
    private double weight;
    private double water;
    private double bmi;
    private double fat;
    private double muscles;

    public MeasurementPrediction() {
    }

    @Ignore
    public MeasurementPrediction(@NonNull Long id,
                                 Long forecastId,
                                 int daysOffset,
                                 double weight,
                                 double water,
                                 double bmi,
                                 double fat,
                                 double muscles) {
        this.id = id;
        this.forecastId = forecastId;
        this.daysOffset = daysOffset;
        this.weight = weight;
        this.water = water;
        this.bmi = bmi;
        this.fat = fat;
        this.muscles = muscles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getForecastId() {
        return forecastId;
    }

    public void setForecastId(Long forecastId) {
        this.forecastId = forecastId;
    }

    public int getDaysOffset() {
        return daysOffset;
    }

    public void setDaysOffset(int daysOffset) {
        this.daysOffset = daysOffset;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWater() {
        return water;
    }

    public void setWater(double water) {
        this.water = water;
    }

    public double getBmi() {
        return bmi;
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getMuscles() {
        return muscles;
    }

    public void setMuscles(double muscles) {
        this.muscles = muscles;
    }
}
