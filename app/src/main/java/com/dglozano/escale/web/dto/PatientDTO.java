package com.dglozano.escale.web.dto;

import com.dglozano.escale.db.entity.Patient;

import java.util.Date;

public class PatientDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Patient.Gender gender;
    private int heightInCm;
    private int physicalActivity;
    private Date birthday;
    private boolean changedDefaultPassword;
    private DoctorDTO doctor;
    private WeightGoalDTO currentWeightGoal;
    private boolean enabled;

    public PatientDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
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

    public Patient.Gender getGender() {
        return gender;
    }

    public void setGender(Patient.Gender gender) {
        this.gender = gender;
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

    public boolean hasChangedDefaultPassword() {
        return changedDefaultPassword;
    }

    public void setChangedDefaultPassword(boolean changedDefaultPassword) {
        this.changedDefaultPassword = changedDefaultPassword;
    }

    public DoctorDTO getDoctorDTO() {
        return doctor;
    }

    public void setDoctorDTO(DoctorDTO doctor) {
        this.doctor = doctor;
    }

    public WeightGoalDTO getCurrentWeightGoal() {
        return currentWeightGoal;
    }

    public void setCurrentWeightGoal(WeightGoalDTO currentWeightGoal) {
        this.currentWeightGoal = currentWeightGoal;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return String.format("{\n " +
                "   id: %s \n" +
                "   firstName: %s \n" +
                "   lastName: %s \n" +
                "   email: %s \n" +
                "   gender: %s \n" +
                "   changedpass: %s \n" +
                "   height: %s \n" +
                "   physicalactivity: %s \n" +
                "}", id, firstName, lastName, email, gender, changedDefaultPassword, heightInCm, physicalActivity);
    }
}
