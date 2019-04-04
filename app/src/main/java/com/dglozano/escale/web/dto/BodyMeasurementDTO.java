package com.dglozano.escale.web.dto;

import java.util.Date;

public class BodyMeasurementDTO extends AddBodyMeasurementDTO {

    private Long id;

    public BodyMeasurementDTO() {
    }

    public BodyMeasurementDTO(Long id, Long patientId, float weight, float water, float fat,
                                 float bmi, float bones, float muscles, Date date, boolean isManual) {
        super(patientId, weight, water, bmi, fat, bones, muscles, date, isManual);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
