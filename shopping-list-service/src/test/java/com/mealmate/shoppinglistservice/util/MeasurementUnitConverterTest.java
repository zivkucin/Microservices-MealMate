package com.mealmate.shoppinglistservice.util;

import com.mealmate.shoppinglistservice.entity.MeasurementUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MeasurementUnitConverterTest {

    @Test
    void shouldConvertKilogramsToGrams() {
        double result = MeasurementUnitConverter.toBaseUnit(
                2,
                MeasurementUnit.KILOGRAM
        );

        assertEquals(2000, result);
    }

    @Test
    void shouldConvertLitersToMilliliters() {
        double result = MeasurementUnitConverter.toBaseUnit(
                1.5,
                MeasurementUnit.LITER
        );

        assertEquals(1500, result);
    }

    @Test
    void shouldKeepGramsAsBaseUnit() {
        double result = MeasurementUnitConverter.toBaseUnit(
                500,
                MeasurementUnit.GRAM
        );

        assertEquals(500, result);
    }

    @Test
    void shouldReturnKilogramFor1000Grams() {
        MeasurementUnit result =
                MeasurementUnitConverter.getDisplayUnit(
                        1000,
                        MeasurementUnit.GRAM
                );

        assertEquals(MeasurementUnit.KILOGRAM, result);
    }

    @Test
    void shouldConvertBaseQuantityToKilograms() {
        double result = MeasurementUnitConverter.fromBaseUnit(
                2500,
                MeasurementUnit.KILOGRAM
        );

        assertEquals(2.5, result);
    }

    @Test
    void shouldRecognizeCompatibleUnits() {
        assertTrue(
                MeasurementUnitConverter.areCompatible(
                        MeasurementUnit.GRAM,
                        MeasurementUnit.KILOGRAM
                )
        );

        assertTrue(
                MeasurementUnitConverter.areCompatible(
                        MeasurementUnit.MILLILITER,
                        MeasurementUnit.LITER
                )
        );

        assertTrue(
                MeasurementUnitConverter.areCompatible(
                        MeasurementUnit.PIECE,
                        MeasurementUnit.PIECE
                )
        );
    }

    @Test
    void shouldRecognizeIncompatibleUnits() {
        assertFalse(
                MeasurementUnitConverter.areCompatible(
                        MeasurementUnit.GRAM,
                        MeasurementUnit.LITER
                )
        );

        assertFalse(
                MeasurementUnitConverter.areCompatible(
                        MeasurementUnit.PIECE,
                        MeasurementUnit.GRAM
                )
        );
    }
}