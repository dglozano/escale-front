package com.example.dglozano.escale.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class User {

    public enum Gender {
        MALE, FEMALE
    }

    private String name;
    private String lastName;
    private String email;
    private Gender gender;
    private Integer scaleUserPin;
    private Integer scaleUserIndex;
    private Integer heightCentimeters;
    private Integer physicalActivity;
    private Date birthday;
    private final List<BodyMeasurement> bodyMeasurementList;

    private User (UserBuilder builder) {
        name = builder.name;
        lastName = builder.lastName;
        email = builder.email;
        gender = builder.gender;
        scaleUserPin = builder.scaleUserPin;
        scaleUserIndex = builder.scaleUserIndex;
        heightCentimeters = builder.heightCentimeters;
        physicalActivity = builder.physicalActivity;
        birthday = builder.birthday;
        bodyMeasurementList = builder.bodyMeasurementList;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Gender getGender() {
        return gender;
    }

    public Integer getScaleUserPin() {
        return scaleUserPin;
    }

    public Integer getScaleUserIndex() {
        return scaleUserIndex;
    }

    public Integer getHeightCentimeters() {
        return heightCentimeters;
    }

    public Integer getPhysicalActivity() {
        return physicalActivity;
    }

    public Date getBirthday() {
        return birthday;
    }

    public List<BodyMeasurement> getBodyMeasurementList() {
        return bodyMeasurementList;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setScaleUserPin(Integer scaleUserPin) {
        this.scaleUserPin = scaleUserPin;
    }

    public void setScaleUserIndex(Integer scaleUserIndex) {
        this.scaleUserIndex = scaleUserIndex;
    }

    public void setHeightCentimeters(Integer heightCentimeters) {
        this.heightCentimeters = heightCentimeters;
    }

    public void setPhysicalActivity(Integer physicalActivity) {
        this.physicalActivity = physicalActivity;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public static class UserBuilder {
        private final String name;
        private final String lastName;
        private final String email;
        private Gender gender;
        private Integer scaleUserPin;
        private Integer scaleUserIndex;
        private Integer heightCentimeters;
        private Integer physicalActivity;
        private Date birthday;
        private final List<BodyMeasurement> bodyMeasurementList;

        public UserBuilder(String name, String lastName, String email) {
            this.name = name;
            this.lastName = lastName;
            this.email = email;
            this.bodyMeasurementList = new ArrayList<>();
            this.physicalActivity = 1;
            this.scaleUserPin = 0;
            this.scaleUserIndex = -1;
            this.heightCentimeters = 160;
            this.gender = Gender.MALE;
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 1990);
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            this.birthday = cal.getTime();
        }

        public UserBuilder gender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public UserBuilder withScaleUserPin(Integer scaleUserPin) {
            this.scaleUserPin = scaleUserPin;
            return this;
        }

        public UserBuilder withScaleUserIndex(Integer scaleUserIndex) {
            this.scaleUserIndex = scaleUserIndex;
            return this;
        }

        public UserBuilder height(Integer userHeight) {
            this.heightCentimeters = userHeight;
            return this;
        }

        public UserBuilder physicalActivity(Integer physycalActivity) {
            this.physicalActivity = physycalActivity;
            return this;
        }

        public UserBuilder birthday(Date birthday) {
            this.birthday = birthday;
            return this;
        }

        public User build() {
            return new User(this);
        }

    }
}
