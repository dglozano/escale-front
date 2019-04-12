package com.dglozano.escale.web.dto;

import java.util.Date;

public class CreatePatientDTO {

    private String firstName;
    private String lastName;
    private String email;
    private int heightInCm;
    private int physicalActivity;
    private int gender;
    private Date birthday;
    private Long doctorId;

    public CreatePatientDTO() {
    }

    public CreatePatientDTO(String firstName,
                            String lastName,
                            String email,
                            Date birthday,
                            int heightInCm,
                            int gender,
                            int physicalActivity,
                            Long doctorId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.heightInCm = heightInCm;
        this.physicalActivity = physicalActivity;
        this.gender = gender;
        this.birthday = birthday;
        this.doctorId = doctorId;
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

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
}
