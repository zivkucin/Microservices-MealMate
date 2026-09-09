package com.mealmate.recipe.repository;

import com.mealmate.recipe.entity.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIngredientRepository
        extends JpaRepository<RecipeIngredient, Long> {

    boolean existsByIngredientId(Long ingredientId);
    long countByRecipeId(Long recipeId);
}