package com.mealmate.recipe.dto;

import lombok.Data;

@Data
public class RecipeIngredientResponse {

    private Long ingredientId;

    private String ingredientName;

    private Double quantity;

    private String unit;
}