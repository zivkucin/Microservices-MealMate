package com.mealmate.mealservice.dto;

import com.mealmate.mealservice.enums.MeasurementUnit;

public record PlannedSnackResponse(
        Long plannedSnackId,
        Long plannedMealId,
        Long snackId,
        Double quantity,
        MeasurementUnit unit
) {}