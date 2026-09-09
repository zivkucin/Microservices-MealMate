package com.mealmate.recipe.mapper;

import com.mealmate.recipe.dto.RecipeIngredientResponse;
import com.mealmate.recipe.entity.RecipeIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecipeIngredientMapper {

    @Mapping(source = "ingredient.id", target = "ingredientId")
    @Mapping(source = "ingredient.name", target = "ingredientName")
    @Mapping(source = "ingredient.unit", target = "unit")
    RecipeIngredientResponse toResponse(RecipeIngredient recipeIngredient);
}