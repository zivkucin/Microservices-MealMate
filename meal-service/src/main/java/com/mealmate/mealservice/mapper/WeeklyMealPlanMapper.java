package com.mealmate.mealservice.mapper;

import com.mealmate.mealservice.dto.WeeklyMealPlanResponse;
import com.mealmate.mealservice.entity.WeeklyMealPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = PlannedMealMapper.class
)
public interface WeeklyMealPlanMapper {

    @Mapping(
            target = "plannedMeals",
            source = "plannedMeals"
    )
    WeeklyMealPlanResponse toResponse(WeeklyMealPlan weeklyMealPlan);
}
