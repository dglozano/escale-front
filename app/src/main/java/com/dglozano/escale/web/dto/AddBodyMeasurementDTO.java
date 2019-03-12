package com.dglozano.escale.web.dto;

import java.util.Date;

public class AddBodyMeasurementDTO {

    private Long patientId;
    private float weight;
    private float water;
    private float bmi;
    private float fat;
    private float bones;
    private float muscles;
    private Date date;

    public AddBodyMeasurementDTO() {
    }

    public AddBodyMeasurementDTO(Long patientId, float weight, float water, float fat,
                                 float bmi, float bones, float muscles, Date date) {
        this.patientId = patientId;
        this.weight = weight;
        this.water = water;
        this.bmi = bmi;
        this.fat = fat;
        this.bones = bones;
        this.muscles = muscles;
        this.date = date;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getWater() {
        return water;
    }

    public void setWater(float water) {
        this.water = water;
    }

    public float getBmi() {
        return bmi;
    }

    public void setBmi(float bmi) {
        this.bmi = bmi;
    }

    public float getFat() {
        return fat;
    }

    public void setFat(float fat) {
        this.fat = fat;
    }

    public float getBones() {
        return bones;
    }

    public void setBones(float bones) {
        this.bones = bones;
    }

    public float getMuscles() {
        return muscles;
    }

    public void setMuscles(float muscles) {
        this.muscles = muscles;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
