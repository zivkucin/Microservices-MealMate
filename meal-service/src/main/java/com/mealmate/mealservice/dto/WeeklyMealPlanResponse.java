package com.mealmate.mealservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record WeeklyMealPlanResponse(
        Long weeklyMealPlanId,
        Long userId,
        LocalDate weekStartDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PlannedMealResponse> plannedMeals
) {
}