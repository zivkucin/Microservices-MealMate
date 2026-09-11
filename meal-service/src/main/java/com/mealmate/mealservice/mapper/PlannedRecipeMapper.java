package com.mealmate.mealservice.mapper;

import com.mealmate.mealservice.dto.PlannedRecipeResponse;
import com.mealmate.mealservice.entity.PlannedRecipe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlannedRecipeMapper {

    @Mapping(
            target = "plannedMealId",
            source = "plannedMeal.plannedMealId"
    )
    PlannedRecipeResponse toResponse(PlannedRecipe plannedRecipe);
}