package com.mealmate.mealservice.controller;

import com.mealmate.mealservice.dto.PlannedMealContentResponse;
import com.mealmate.mealservice.service.PlannedMealService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meal-plans")
public class PlannedMealController {

    private final PlannedMealService plannedMealService;

    public PlannedMealController(
            PlannedMealService plannedMealService
    ) {
        this.plannedMealService = plannedMealService;
    }

    @Operation(
            summary = "Get meal slot content",
            description = "Returns the recipe or snacks assigned to the selected meal slot."
    )
    @GetMapping("/{weeklyMealPlanId}/meals/{plannedMealId}")
    public PlannedMealContentResponse getMealContent(
            @PathVariable Long weeklyMealPlanId,
            @PathVariable Long plannedMealId
    ) {
        return plannedMealService.getMealContent(
                weeklyMealPlanId,
                plannedMealId
        );
    }

    @Operation(
            summary = "Clear meal slot",
            description = "Removes all content from the selected meal slot."
    )
    @DeleteMapping("/{weeklyMealPlanId}/meals/{plannedMealId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearMeal(
            @PathVariable Long weeklyMealPlanId,
            @PathVariable Long plannedMealId
    ) {
        plannedMealService.clearMeal(
                weeklyMealPlanId,
                plannedMealId
        );
    }
}