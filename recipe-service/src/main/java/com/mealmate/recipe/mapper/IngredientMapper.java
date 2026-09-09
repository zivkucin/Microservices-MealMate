package com.mealmate.recipe.mapper;

import com.mealmate.recipe.dto.IngredientRequest;
import com.mealmate.recipe.dto.IngredientResponse;
import com.mealmate.recipe.entity.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IngredientMapper {

    @Mapping(target = "id", ignore = true)
    Ingredient toEntity(IngredientRequest request);

    IngredientResponse toResponse(Ingredient ingredient);
}