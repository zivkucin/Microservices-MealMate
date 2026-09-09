package com.mealmate.recipe.dto;

import com.mealmate.recipe.enums.RecipeCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class RecipeRequest {

    @NotBlank(message = "Recipe name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Instructions are required")
    private String instructions;

    @NotNull(message = "Preparation time is required")
    @Positive(message = "Preparation time must be positive")
    private Integer preparationTime;

    @NotNull(message = "Category is required")
    private RecipeCategory category;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String imageUrl;

    @NotEmpty(message = "At least one ingredient is required")
    private List<@Valid RecipeIngredientRequest> ingredients;
}