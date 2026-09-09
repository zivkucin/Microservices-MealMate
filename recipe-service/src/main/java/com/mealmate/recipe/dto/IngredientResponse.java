package com.mealmate.recipe.dto;

import lombok.Data;

@Data
public class IngredientResponse {

    private Long id;

    private String name;

    private String unit;
}