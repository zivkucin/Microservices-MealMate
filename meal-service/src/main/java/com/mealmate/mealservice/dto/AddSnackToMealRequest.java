package com.mealmate.mealservice.dto;

import com.mealmate.mealservice.enums.MeasurementUnit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddSnackToMealRequest(

        @NotNull
        Long snackId,

        @NotNull
        @Positive
        Double quantity,

        @NotNull
        MeasurementUnit unit

) {}