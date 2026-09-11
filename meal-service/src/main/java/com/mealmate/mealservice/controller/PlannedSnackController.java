package com.mealmate.mealservice.controller;

import com.mealmate.mealservice.dto.AddSnackToMealRequest;
import com.mealmate.mealservice.dto.PlannedSnackResponse;
import com.mealmate.mealservice.service.PlannedSnackService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meal-plans")
public class PlannedSnackController {

    private final PlannedSnackService plannedSnackService;

    public PlannedSnackController(
            PlannedSnackService plannedSnackService
    ) {
        this.plannedSnackService = plannedSnackService;
    }

    @Operation(
            summary = "Add a snack to a meal slot",
            description = "Adds a snack with the selected quantity and unit to the specified meal slot."
    )
    @PostMapping("/{weeklyMealPlanId}/meals/{plannedMealId}/snacks")
    @ResponseStatus(HttpStatus.CREATED)
    public PlannedSnackResponse addSnackToMeal(
            @PathVariable Long weeklyMealPlanId,
            @PathVariable Long plannedMealId,
            @Valid @RequestBody AddSnackToMealRequest request
    ) {
        return plannedSnackService.addSnackToMeal(
                weeklyMealPlanId,
                plannedMealId,
                request
        );
    }

    @Operation(
            summary = "Remove a snack from a meal slot",
            description = "Removes the selected snack from the specified meal slot."
    )
    @DeleteMapping(
            "/{weeklyMealPlanId}/meals/{plannedMealId}/snacks/{plannedSnackId}"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSnackFromMeal(
            @PathVariable Long weeklyMealPlanId,
            @PathVariable Long plannedMealId,
            @PathVariable Long plannedSnackId
    ) {
        plannedSnackService.removeSnackFromMeal(
                weeklyMealPlanId,
                plannedMealId,
                plannedSnackId
        );
    }
}