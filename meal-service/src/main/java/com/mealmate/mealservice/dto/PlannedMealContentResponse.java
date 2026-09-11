package com.mealmate.mealservice.dto;

import java.util.List;

public record PlannedMealContentResponse(
        Long plannedMealId,
        Integer mealSlotId,
        PlannedRecipeResponse recipe,
        List<PlannedSnackResponse> snacks
) {}