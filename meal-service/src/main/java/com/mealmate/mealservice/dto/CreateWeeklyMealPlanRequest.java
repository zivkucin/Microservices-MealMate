package com.mealmate.mealservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateWeeklyMealPlanRequest(

        @NotNull
        Long userId,

        @NotNull
        LocalDate weekStartDate
) {
}