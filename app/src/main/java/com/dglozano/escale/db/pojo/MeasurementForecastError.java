package com.dglozano.escale.db.pojo;

import androidx.room.Embedded;

public class MeasurementForecastError {

    @Embedded(prefix = "weight_")
    private AttributeForecastError weightError;
    @Embedded(prefix = "fat_")
    private AttributeForecastError fatError;
    @Embedded(prefix = "muscle_")
    private AttributeForecastError muscleError;
    @Embedded(prefix = "water_")
    private AttributeForecastError waterError;
    @Embedded(prefix = "bmi_")
    private AttributeForecastError bmiError;

    public MeasurementForecastError() {
    }

    public MeasurementForecastError(AttributeForecastError weightError,
                                    AttributeForecastError fatError,
                                    AttributeForecastError muscleError,
                                    AttributeForecastError waterError,
                                    AttributeForecastError bmiError) {
        this.weightError = weightError;
        this.fatError = fatError;
        this.muscleError = muscleError;
        this.waterError = waterError;
        this.bmiError = bmiError;
    }

    public AttributeForecastError getWeightError() {
        return weightError;
    }

    public void setWeightError(AttributeForecastError weightError) {
        this.weightError = weightError;
    }

    public AttributeForecastError getFatError() {
        return fatError;
    }

    public void setFatError(AttributeForecastError fatError) {
        this.fatError = fatError;
    }

    public AttributeForecastError getMuscleError() {
        return muscleError;
    }

    public void setMuscleError(AttributeForecastError muscleError) {
        this.muscleError = muscleError;
    }

    public AttributeForecastError getWaterError() {
        return waterError;
    }

    public void setWaterError(AttributeForecastError waterError) {
        this.waterError = waterError;
    }

    public AttributeForecastError getBmiError() {
        return bmiError;
    }

    public void setBmiError(AttributeForecastError bmiError) {
        this.bmiError = bmiError;
    }


}