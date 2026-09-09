package com.mealmate.recipe.mapper;

import com.mealmate.recipe.dto.RecipeRequest;
import com.mealmate.recipe.dto.RecipeResponse;
import com.mealmate.recipe.entity.Recipe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = RecipeIngredientMapper.class
)
public interface RecipeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "recipeIngredients", ignore = true)
    Recipe toEntity(RecipeRequest request);

    @Mapping(source = "recipeIngredients", target = "ingredients")
    RecipeResponse toResponse(Recipe recipe);
}