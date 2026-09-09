package com.mealmate.recipe.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RecipeIngredientRequest {

    @NotNull(message = "Ingredient ID is required")
    private Long ingredientId;

    @NotNull(message = "Ingredient quantity is required")
    @Positive(message = "Ingredient quantity must be positive")
    private Double quantity;
}