package com.dglozano.escale.ui.main.home;

import com.dglozano.escale.db.entity.BodyMeasurement;

import java.util.ArrayList;
import java.util.List;

public class MeasurementItem {

    static final int ICON_RESOURCE_WEIGHT = 1;
    static final int ICON_RESOURCE_WATER = 2;
    static final int ICON_RESOURCE_FAT = 3;
    static final int ICON_RESOURCE_BONES = 4;
    static final int ICON_RESOURCE_BMI = 5;
    static final int ICON_RESOURCE_MUSCLES = 6;

    private int iconResource;
    private float value;
    private Unit unit;
    private MeasurementName name;

    private MeasurementItem(int iconResource, float value, Unit unit, MeasurementName name) {
        this.iconResource = iconResource;
        this.value = value;
        this.unit = unit;
        this.name = name;
    }

    int getIconResource() {
        return iconResource;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public MeasurementName getName() {
        return name;
    }

    public void setName(MeasurementName name) {
        this.name = name;
    }

    public enum MeasurementName {
        WEIGHT("Peso"),
        BMI("IMC"),
        MUSCLES("Masa Muscular"),
        BONES("Masa Ósea"),
        WATER("Agua Corporal"),
        FAT("Grasa corporal");

        private final String name;

        MeasurementName(String s) {
            this.name = s;
        }

        public boolean equalsName(String otherName) {
            return name.equals(otherName);
        }

        public String toString() {
            return this.name;
        }
    }

    public enum Unit {
        KG("kg"),
        LB("lb"),
        NO_UNIT(""),
        PERCENTAGE("%");

        private final String name;

        Unit(String s) {
            this.name = s;
        }

        public boolean equalsName(String otherName) {
            return name.equals(otherName);
        }

        public String toString() {
            return this.name;
        }
    }

    static List<MeasurementItem> getMeasurementList(BodyMeasurement bodyMeasurement) {
        List<MeasurementItem> measurementItemList = new ArrayList<>();
        measurementItemList.add(new MeasurementItem(
                ICON_RESOURCE_WEIGHT,
                bodyMeasurement.getWeight(),
                MeasurementItem.Unit.KG,
                MeasurementItem.MeasurementName.WEIGHT));
        measurementItemList.add(new MeasurementItem(
                ICON_RESOURCE_WATER,
                bodyMeasurement.getWater(),
                MeasurementItem.Unit.PERCENTAGE,
                MeasurementItem.MeasurementName.WATER));
        measurementItemList.add(new MeasurementItem(
                ICON_RESOURCE_FAT,
                bodyMeasurement.getFat(),
                MeasurementItem.Unit.PERCENTAGE,
                MeasurementItem.MeasurementName.FAT));
        measurementItemList.add(new MeasurementItem(
                ICON_RESOURCE_BONES,
                bodyMeasurement.getBones(),
                MeasurementItem.Unit.KG,
                MeasurementItem.MeasurementName.BONES));
        measurementItemList.add(new MeasurementItem(
                ICON_RESOURCE_BMI,
                bodyMeasurement.getBmi(),
                MeasurementItem.Unit.NO_UNIT,
                MeasurementItem.MeasurementName.BMI));
        measurementItemList.add(new MeasurementItem(
                ICON_RESOURCE_MUSCLES,
                bodyMeasurement.getMuscles(),
                MeasurementItem.Unit.PERCENTAGE,
                MeasurementItem.MeasurementName.MUSCLES));
        return measurementItemList;
    }
}