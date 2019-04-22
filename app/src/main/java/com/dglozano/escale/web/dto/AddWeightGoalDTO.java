package com.dglozano.escale.web.dto;

import java.util.Date;

public class AddWeightGoalDTO {

    private Float goalInKg;
    private Date dueDate;
    private boolean loseGoal;

    public AddWeightGoalDTO() {
    }

    public AddWeightGoalDTO(Float goalInKg, Date dueDate, boolean isLoseGoal) {
        this.goalInKg = goalInKg;
        this.dueDate = dueDate;
        this.loseGoal = isLoseGoal;
    }

    public boolean isLoseGoal() {
        return loseGoal;
    }

    public void setLoseGoal(boolean loseGoal) {
        this.loseGoal = loseGoal;
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
