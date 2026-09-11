package com.mealmate.mealservice.dto;

public record PlannedRecipeResponse(
        Long plannedRecipeId,
        Long plannedMealId,
        Long recipeId,
        Integer quantity
) {
}
