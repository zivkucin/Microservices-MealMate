package com.mealmate.recipe.dto;

import com.mealmate.recipe.enums.RecipeCategory;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecipeResponse {

    private Long id;

    private String name;

    private String description;

    private String instructions;

    private Integer preparationTime;

    private RecipeCategory category;

    private Long userId;

    private String imageUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<RecipeIngredientResponse> ingredients;
}