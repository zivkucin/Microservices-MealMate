package com.mealmate.shoppinglistservice.util;

import com.mealmate.shoppinglistservice.entity.MeasurementUnit;

public final class MeasurementUnitConverter {

    private MeasurementUnitConverter() {
    }

    public static double toBaseUnit(double quantity, MeasurementUnit unit) {
        return switch (unit) {
            case GRAM -> quantity;
            case KILOGRAM -> quantity * 1000;
            case MILLILITER -> quantity;
            case LITER -> quantity * 1000;
            case PIECE -> quantity;
        };
    }

    public static MeasurementUnit getDisplayUnit(
            double baseQuantity,
            MeasurementUnit unit
    ) {
        return switch (unit) {
            case GRAM, KILOGRAM ->
                    baseQuantity >= 1000
                            ? MeasurementUnit.KILOGRAM
                            : MeasurementUnit.GRAM;

            case MILLILITER, LITER ->
                    baseQuantity >= 1000
                            ? MeasurementUnit.LITER
                            : MeasurementUnit.MILLILITER;

            case PIECE -> MeasurementUnit.PIECE;
        };
    }

    public static double fromBaseUnit(
            double baseQuantity,
            MeasurementUnit displayUnit
    ) {
        return switch (displayUnit) {
            case GRAM, MILLILITER, PIECE -> baseQuantity;
            case KILOGRAM, LITER -> baseQuantity / 1000;
        };
    }

    public static boolean areCompatible(
            MeasurementUnit first,
            MeasurementUnit second
    ) {
        return switch (first) {
            case GRAM, KILOGRAM ->
                    second == MeasurementUnit.GRAM
                            || second == MeasurementUnit.KILOGRAM;

            case MILLILITER, LITER ->
                    second == MeasurementUnit.MILLILITER
                            || second == MeasurementUnit.LITER;

            case PIECE ->
                    second == MeasurementUnit.PIECE;
        };
    }
}