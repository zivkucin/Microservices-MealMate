package com.mealmate.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IngredientRequest {

    @NotBlank(message = "Ingredient name is required")
    private String name;

    @NotBlank(message = "Ingredient unit is required")
    private String unit;
}