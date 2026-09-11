package com.mealmate.mealservice.dto;

import com.mealmate.mealservice.enums.MeasurementUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSnackRequest(

        @NotBlank
        String name,

        @NotNull
        MeasurementUnit defaultUnit

) {}