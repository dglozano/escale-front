package com.example.dglozano.escale.data;

import com.example.dglozano.escale.data.entities.BodyMeasurement;

import java.util.ArrayList;
import java.util.List;

public class MeasurementItem {

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
        KG(" kg"),
        LB(" lb"),
        NO_UNIT(""),
        PERCENTAGE(" %");

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

    private int iconResource;
    private float value;
    private Unit unit;
    private MeasurementName name;

    public MeasurementItem(int iconResource, float value, Unit unit, MeasurementName name) {
        this.iconResource = iconResource;
        this.value = value;
        this.unit = unit;
        this.name = name;
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

    public static List<MeasurementItem> getMeasurementList(BodyMeasurement bodyMeasurement) {
        List<MeasurementItem> measurementItemList = new ArrayList<>();
        measurementItemList.add(new MeasurementItem(
                1,
                bodyMeasurement.getWeight(),
                MeasurementItem.Unit.KG,
                MeasurementItem.MeasurementName.WEIGHT));
        measurementItemList.add(new MeasurementItem(
                2,
                bodyMeasurement.getWater(),
                MeasurementItem.Unit.PERCENTAGE,
                MeasurementItem.MeasurementName.WATER));
        measurementItemList.add(new MeasurementItem(
                3,
                bodyMeasurement.getFat(),
                MeasurementItem.Unit.PERCENTAGE,
                MeasurementItem.MeasurementName.FAT));
        measurementItemList.add(new MeasurementItem(
                4,
                bodyMeasurement.getBones(),
                MeasurementItem.Unit.KG,
                MeasurementItem.MeasurementName.BONES));
        measurementItemList.add(new MeasurementItem(
                5,
                bodyMeasurement.getBmi(),
                MeasurementItem.Unit.NO_UNIT,
                MeasurementItem.MeasurementName.BMI));
        measurementItemList.add(new MeasurementItem(
                6,
                bodyMeasurement.getMuscles(),
                MeasurementItem.Unit.PERCENTAGE,
                MeasurementItem.MeasurementName.MUSCLES));
        return measurementItemList;
    }
}