package com.dglozano.escale.web.dto;

import java.util.Date;

public class BodyMeasurementDTO {

    private Long id;
    private Long patientId;
    private float weight;
    private float water;
    private float bmi;
    private float fat;
    private float bones;
    private float muscles;
    private Date date;

    public BodyMeasurementDTO() {
    }

    public BodyMeasurementDTO(Long id, Long patientId, float weight, float water, float bmi,
                              float fat, float bones, float muscles, Date date) {
        this.id = id;
        this.patientId = patientId;
        this.weight = weight;
        this.water = water;
        this.bmi = bmi;
        this.fat = fat;
        this.bones = bones;
        this.muscles = muscles;
        this.date = date;
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
