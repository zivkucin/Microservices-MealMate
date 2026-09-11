package com.mealmate.mealservice.mapper;

import com.mealmate.mealservice.dto.PlannedMealResponse;
import com.mealmate.mealservice.entity.PlannedMeal;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlannedMealMapper {

    PlannedMealResponse toResponse(PlannedMeal plannedMeal);
}