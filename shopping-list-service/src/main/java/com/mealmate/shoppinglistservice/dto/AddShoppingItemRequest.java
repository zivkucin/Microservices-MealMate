package com.mealmate.shoppinglistservice.dto;

import com.mealmate.shoppinglistservice.entity.MeasurementUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddShoppingItemRequest(

        @NotBlank
        String name,

        @NotNull
        @Positive
        Double quantity,

        @NotNull
        MeasurementUnit unit

) {
}