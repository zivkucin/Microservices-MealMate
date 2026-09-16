package com.mealmate.reviewservice.dto;

public record RecipeResponse(
        Long id,
        String name,
        Long userId
) {
}