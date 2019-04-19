package com.dglozano.escale.web.dto;

import java.util.Date;

public class AddWeightGoalDTO {

    private Float goalInKg;
    private Date dueDate;

    public AddWeightGoalDTO() {
    }

    public AddWeightGoalDTO(Float goalInKg, Date dueDate) {
        this.goalInKg = goalInKg;
        this.dueDate = dueDate;
    }

    public Float getGoalInKg() {
        return goalInKg;
    }

    public void setGoalInKg(Float goalInKg) {
        this.goalInKg = goalInKg;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
}
