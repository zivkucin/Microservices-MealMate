package com.mealmate.mealservice.mapper;

import com.mealmate.mealservice.dto.PlannedSnackResponse;
import com.mealmate.mealservice.entity.PlannedSnack;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlannedSnackMapper {

    @Mapping(
            target = "plannedMealId",
            source = "plannedMeal.plannedMealId"
    )
    @Mapping(
            target = "snackId",
            source = "snack.snackId"
    )
    PlannedSnackResponse toResponse(PlannedSnack plannedSnack);
}