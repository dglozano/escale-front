package com.dglozano.escale.web.dto;

public class UpdatePatientDTO {

    private Integer heightInCm;
    private Integer physicalActivity;

    public UpdatePatientDTO() {
    }

    public UpdatePatientDTO(Integer heightInCm, Integer physicalActivity) {
        this.heightInCm = heightInCm;
        this.physicalActivity = physicalActivity;
    }

    public Integer getHeightInCm() {
        return heightInCm;
    }

    public void setHeightInCm(Integer heightInCm) {
        this.heightInCm = heightInCm;
    }

    public Integer getPhysicalActivity() {
        return physicalActivity;
    }

    public void setPhysicalActivity(Integer physicalActivity) {
        this.physicalActivity = physicalActivity;
    }
}
