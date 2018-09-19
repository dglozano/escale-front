package com.example.dglozano.escale.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = User.class,
        parentColumns = "id",
        childColumns = "userId",
        onDelete = CASCADE))
public class BodyMeasurement {

    @Ignore
    public static float NO_VALUE = -1.0f;

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private Date date;
    private float weight;
    private float bmi;
    private float fat;
    private float water;
    private float bones;
    private float muscles;

    public BodyMeasurement(){

    }

    @Ignore
    public BodyMeasurement(int id, int userId, Date date, float weight) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public float getWeight() {
        return weight;
    }

    public float getBmi() {
        return bmi;
    }

    public float getFat() {
        return fat;
    }

    public float getWater() {
        return water;
    }

    public float getBones() {
        return bones;
    }

    public float getMuscles() {
        return muscles;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public void setBmi(float bmi) {
        this.bmi = bmi;
    }

    public void setFat(float fat) {
        this.fat = fat;
    }

    public void setWater(float water) {
        this.water = water;
    }

    public void setBones(float bones) {
        this.bones = bones;
    }

    public void setMuscles(float muscles) {
        this.muscles = muscles;
    }

    @Override
    public boolean equals(Object other) {
        if(other == null) return false;
        if(other == this) return true;
        if(!(other instanceof  BodyMeasurement)) return false;
        BodyMeasurement otherMeasurement = (BodyMeasurement) other;

        return otherMeasurement.id == this.id
                && otherMeasurement.date.equals(this.date)
                && otherMeasurement.weight == this.weight
                && otherMeasurement.userId == this.userId
                && otherMeasurement.muscles == this.muscles
                && otherMeasurement.bones == this.bones
                && otherMeasurement.water == this.water
                && otherMeasurement.fat == this.fat
                && otherMeasurement.bmi == this.bmi;
    }

    @Override
    public String toString() {
        SimpleDateFormat df = new SimpleDateFormat("dd/mm/yyyy");
        return String.format("{\n " +
                "   id: %s \n" +
                "   userId: %s \n" +
                "   date: %s \n" +
                "   weight: %s \n" +
                "   bones: %s \n" +
                "   fat: %s \n" +
                "   water: %s \n" +
                "   muscle: %s \n" +
                "   bmi: %s \n" +
                "}", id, userId, df.format(date), weight, bones, fat, water, muscles, bmi);
    }

    public static BodyMeasurement createMockBodyMeasurementForUser(Integer userId) {
        Random r = new Random();
        Float weight = r.nextInt(51) + 60f;
        Float bmi = r.nextInt(30) + 10f;
        Float fat = r.nextInt(30) + 10f;
        Float muscles = r.nextInt(30) + 10f;
        Float water = r.nextInt(30) + 10f;
        Float bones = r.nextInt(30) + 10f;

        int year = r.nextInt(5) + 2013;
        int month = r.nextInt(12) + 1;
        int day = r.nextInt(28) + 1;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);

        BodyMeasurement bodyMeasurementTest = new BodyMeasurement(0, userId, cal.getTime(), weight);

        bodyMeasurementTest.setBmi(bmi);
        bodyMeasurementTest.setFat(fat);
        bodyMeasurementTest.setMuscles(muscles);
        bodyMeasurementTest.setWater(water);
        bodyMeasurementTest.setBones(bones);

        return bodyMeasurementTest;
    }
}
