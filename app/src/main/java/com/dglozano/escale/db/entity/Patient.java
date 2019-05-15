package com.dglozano.escale.db.entity;

import com.dglozano.escale.web.dto.PatientDTO;
import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import lombok.ToString;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = Doctor.class,
        parentColumns = "id",
        childColumns = "doctorId",
        onDelete = CASCADE))
@ToString
public class Patient extends AppUser {

    private Gender gender;
    private int heightInCm;
    private int physicalActivity;
    private Date birthday;
    private Long doctorId;
    private Float goalInKg;
    private Date goalDueDate;
    private Date goalStartDate;
    private boolean hasToUpdateDataInScale = true;
    private boolean isLoseGoal = true;
    @Ignore
    @ToString.Exclude
    private MeasurementForecast measurementForecast;

    public Patient() {
    }

    @Ignore
    public Patient(Long id, Long doctorId, Date timestamp) {
        this.id = id;
        this.doctorId = doctorId;
        this.lastUpdate = timestamp;
    }

    @Ignore
    public Patient(PatientDTO patientDTO, Date timestamp) {
        super(patientDTO.getId(),
                patientDTO.getFirstName(),
                patientDTO.getLastName(),
                patientDTO.getEmail(),
                timestamp,
                patientDTO.hasChangedDefaultPassword());
        this.gender = patientDTO.getGender();
        this.heightInCm = patientDTO.getHeightInCm();
        this.physicalActivity = patientDTO.getPhysicalActivity();
        this.birthday = patientDTO.getBirthday();
        this.doctorId = patientDTO.getDoctorDTO().getId();
        if (patientDTO.getCurrentWeightGoal() != null) {
            this.goalDueDate = patientDTO.getCurrentWeightGoal().getDueDate();
            this.goalInKg = patientDTO.getCurrentWeightGoal().getGoalInKg();
            this.goalStartDate = patientDTO.getCurrentWeightGoal().getStartDate();
            this.isLoseGoal = patientDTO.getCurrentWeightGoal().isLoseGoal();
        } else {
            this.goalDueDate = null;
            this.goalInKg = null;
            this.goalStartDate = null;
            this.isLoseGoal = true;
        }
    }

    public boolean isLoseGoal() {
        return isLoseGoal;
    }

    public void setLoseGoal(boolean loseGoal) {
        isLoseGoal = loseGoal;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
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

    public boolean isHasToUpdateDataInScale() {
        return hasToUpdateDataInScale;
    }

    public void setHasToUpdateDataInScale(boolean hasToUpdateDataInScale) {
        this.hasToUpdateDataInScale = hasToUpdateDataInScale;
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

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Patient)) return false;
        Patient otherPatient = (Patient) other;

        return otherPatient.id == this.id
                && otherPatient.firstName.equals(this.firstName)
                && otherPatient.lastName.equals(this.lastName)
                && otherPatient.email.equals(this.email);
    }

    public Float getGoalInKg() {
        return goalInKg;
    }

    public void setGoalInKg(Float goalInKg) {
        this.goalInKg = goalInKg;
    }

    public Date getGoalDueDate() {
        return goalDueDate;
    }

    public void setGoalDueDate(Date goalDueDate) {
        this.goalDueDate = goalDueDate;
    }

    public MeasurementForecast getMeasurementForecast() {
        return measurementForecast;
    }

    public void setMeasurementForecast(MeasurementForecast measurementForecast) {
        this.measurementForecast = measurementForecast;
    }

    public int getAge() {
        Date currentDate = Calendar.getInstance().getTime();
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        int d1 = Integer.parseInt(formatter.format(birthday));
        int d2 = Integer.parseInt(formatter.format(currentDate));
        return (d2 - d1) / 10000;
    }

    public String getActivityString() {
        switch (physicalActivity) {
            case 1:
                return "Ninguna";
            case 2:
                return "Baja";
            case 3:
                return "Media";
            case 4:
                return "Alta";
            case 5:
                return "Muy Alta";
            default:
                return "Media";
        }
    }

    public Date getGoalStartDate() {
        return goalStartDate;
    }

    public void setGoalStartDate(Date goalStartDate) {
        this.goalStartDate = goalStartDate;
    }

    public boolean hasActiveGoal(Date today) {
        return goalInKg != null && goalDueDate != null && today.before(goalDueDate);
    }

    public boolean isFullyLoaded() {
        return birthday != null && firstName != null && lastName != null && email != null;
    }

    public enum Gender {
        @SerializedName("1")
        MALE,
        @SerializedName("2")
        FEMALE,
        @SerializedName("3")
        OTHER
    }
}
