package com.dglozano.escale.db.pojo;

import androidx.room.Ignore;

public class AttributeForecastError {
    private double mad;
    private double mapd;

    public AttributeForecastError() {
    }

    @Ignore
    public AttributeForecastError(double mad, double mapd) {
        this.mad = mad;
        this.mapd = mapd;
    }

    public double getMad() {
        return mad;
    }

    public void setMad(double mad) {
        this.mad = mad;
    }

    public double getMapd() {
        return mapd;
    }

    public void setMapd(double mapd) {
        this.mapd = mapd;
    }
}