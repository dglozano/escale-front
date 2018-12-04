package com.dglozano.escale.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

@Entity
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String lastName;
    private String email;
    // Token?
//    private Gender gender;
//    private int scaleUserPin;
//    private int scaleUserIndex;
//    private int heightCentimeters;
//    private int physicalActivity;
//    private Date birthday;

    public User() {
    }

    @Ignore
    public User(Integer id, String name, String lastName, String email) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
    }

    public static User createMockUser() {
        String names[] = {"Diego", "Juan", "Mari", "Pepe", "Rebeca"};
        String lastNames[] = {"Garcia Lozano", "Novero", "Pitton", "Macri"};
        String emailProviders[] = {"hotmail.com", "flioh.com", "gmail.com", "frsf.utn.edu.ar"};

        Random r = new Random();
        int year = r.nextInt(51) + 1950;
        int month = r.nextInt(12) + 1;
        int day = r.nextInt(28) + 1;
        int height = r.nextInt(40) + 160;
        int activity = r.nextInt(4);

        User.Gender gender = r.nextInt(2) == 0 ? User.Gender.MALE : User.Gender.FEMALE;
        String name = names[r.nextInt(names.length)];
        String lastName = lastNames[r.nextInt(lastNames.length)];
        String email = String.format(
                "%s%s@%s",
                name.trim().replaceAll("\\s", "").toLowerCase(),
                lastName.trim().replaceAll("\\s", "").toLowerCase(),
                emailProviders[r.nextInt(emailProviders.length)]);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);

        User userTest = new User(0, name, lastName, email);
//        userTest.setBirthday(cal.getTime());
//        userTest.setGender(gender);
//        userTest.setHeightCentimeters(height);
//        userTest.setPhysicalActivity(activity);

        return userTest;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

//    public Gender getGender() {
//        return gender;
//    }
//
//    public void setGender(Gender gender) {
//        this.gender = gender;
//    }
//
//    public int getScaleUserPin() {
//        return scaleUserPin;
//    }
//
//    public void setScaleUserPin(int scaleUserPin) {
//        this.scaleUserPin = scaleUserPin;
//    }
//
//    public int getScaleUserIndex() {
//        return scaleUserIndex;
//    }
//
//    public void setScaleUserIndex(int scaleUserIndex) {
//        this.scaleUserIndex = scaleUserIndex;
//    }
//
//    public int getHeightCentimeters() {
//        return heightCentimeters;
//    }
//
//    public void setHeightCentimeters(int heightCentimeters) {
//        this.heightCentimeters = heightCentimeters;
//    }
//
//    public int getPhysicalActivity() {
//        return physicalActivity;
//    }
//
//    public void setPhysicalActivity(int physicalActivity) {
//        this.physicalActivity = physicalActivity;
//    }
//
//    public Date getBirthday() {
//        return birthday;
//    }
//
//    public void setBirthday(Date birthday) {
//        this.birthday = birthday;
//    }

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof User)) return false;
        User otherUser = (User) other;

        return otherUser.id == this.id
                && otherUser.name.equals(this.name)
                && otherUser.lastName.equals(this.lastName)
                && otherUser.email.equals(this.email);
    }

    @Override
    public String toString() {
        return String.format("{\n " +
                "   id: %s \n" +
                "   name: %s \n" +
                "   lastName: %s \n" +
                "   email: %s \n" +
                "}", id, name, lastName, email);
    }

    public enum Gender {
        MALE, FEMALE
    }
}
