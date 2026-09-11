package com.mealmate.mealservice.dto;

public record PlannedMealResponse(
        Long plannedMealId,
        Integer mealSlotId
) {
}