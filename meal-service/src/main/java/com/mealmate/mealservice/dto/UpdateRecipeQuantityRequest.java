package com.mealmate.mealservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateRecipeQuantityRequest(

        @NotNull
        @Min(1)
        @Max(3)
        Integer quantity

) {}