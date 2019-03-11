package com.dglozano.escale.ui.main.home;

import com.dglozano.escale.db.entity.BodyMeasurement;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MeasurementItem {

    static final int ICON_RESOURCE_WEIGHT = 1;
    static final int ICON_RESOURCE_WATER = 2;
    static final int ICON_RESOURCE_FAT = 3;
    static final int ICON_RESOURCE_BONES = 4;
    static final int ICON_RESOURCE_BMI = 5;
    static final int ICON_RESOURCE_MUSCLES = 6;

    private int iconResource;
    private String formattedValue;
    private MeasurementName name;

    private MeasurementItem(int iconResource, String formattedValue, MeasurementName name) {
        this.iconResource = iconResource;
        this.formattedValue = formattedValue;
        this.name = name;
    }

    static List<MeasurementItem> getMeasurementList(Optional<BodyMeasurement> bodyMeasurement) {
        DecimalFormat df = new DecimalFormat("###,###.##");
        List<MeasurementItem> measurementItemList = new ArrayList<>();
        String weight = bodyMeasurement.isPresent() ? String.format("%s %s",
                df.format(bodyMeasurement.get().getWeight()), Unit.KG) : "-";
        String water = bodyMeasurement.isPresent() ? String.format("%s %s",
                df.format(bodyMeasurement.get().getWater()), Unit.PERCENTAGE) : "-";
        String fat = bodyMeasurement.isPresent() ? String.format("%s %s",
                df.format(bodyMeasurement.get().getFat()), Unit.PERCENTAGE) : "-";
        String bones = bodyMeasurement.isPresent() ? String.format("%s %s",
                df.format(bodyMeasurement.get().getBones()), Unit.KG) : "-";
        String bmi = bodyMeasurement.isPresent() ? String.format("%s %s",
                df.format(bodyMeasurement.get().getBmi()), Unit.NO_UNIT) : "-";
        String muscles = bodyMeasurement.isPresent() ? String.format("%s %s",
                df.format(bodyMeasurement.get().getMuscles()), Unit.PERCENTAGE) : "-";

        measurementItemList.add(new MeasurementItem(
                ICON_RESOURCE_WEIGHT,
                weight,
                MeasurementItem.MeasurementName.WEIGHT));
        measurementItemList.add(new MeasurementItem(
                ICON_RESOURCE_WATER,
                water,
                MeasurementItem.MeasurementName.WATER));
        measurementItemList.add(new MeasurementItem(
                ICON_RESOURCE_FAT,
                fat,
                MeasurementItem.MeasurementName.FAT));
        measurementItemList.add(new MeasurementItem(
                ICON_RESOURCE_BONES,
                bones,
                MeasurementItem.MeasurementName.BONES));
        measurementItemList.add(new MeasurementItem(
                ICON_RESOURCE_BMI,
                bmi,
                MeasurementItem.MeasurementName.BMI));
        measurementItemList.add(new MeasurementItem(
                ICON_RESOURCE_MUSCLES,
                muscles,
                MeasurementItem.MeasurementName.MUSCLES));
        return measurementItemList;
    }

    int getIconResource() {
        return iconResource;
    }

    public String getFormattedValue() {
        return formattedValue;
    }

    public void setFormattedValue(String value) {
        this.formattedValue = value;
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
}