package com.example.dglozano.escale.model;

public class Measurement {

    // TODO: Change
    private int iconResource;
    private float value;
    private String unit;
    // TODO: Change to enum
    private String type;

    public Measurement(int iconResource, float value, String unit, String type) {
        this.iconResource = iconResource;
        this.value = value;
        this.unit = unit;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIconResource() {
        return iconResource;
    }

    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
