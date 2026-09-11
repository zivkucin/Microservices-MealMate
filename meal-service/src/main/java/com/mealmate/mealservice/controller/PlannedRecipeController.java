package com.mealmate.mealservice.controller;

import com.mealmate.mealservice.dto.AddRecipeToMealRequest;
import com.mealmate.mealservice.dto.PlannedRecipeResponse;
import com.mealmate.mealservice.dto.UpdateRecipeQuantityRequest;
import com.mealmate.mealservice.service.PlannedRecipeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meal-plans")
public class PlannedRecipeController {

    private final PlannedRecipeService plannedRecipeService;

    public PlannedRecipeController(
            PlannedRecipeService plannedRecipeService
    ) {
        this.plannedRecipeService = plannedRecipeService;
    }

    @Operation(
            summary = "Add a recipe to a meal slot",
            description = "Adds a recipe with the selected quantity to the specified meal slot."
    )
    @PostMapping("/{weeklyMealPlanId}/meals/{plannedMealId}/recipe")
    @ResponseStatus(HttpStatus.CREATED)
    public PlannedRecipeResponse addRecipeToMeal(
            @PathVariable Long weeklyMealPlanId,
            @PathVariable Long plannedMealId,
            @Valid @RequestBody AddRecipeToMealRequest request
    ) {
        return plannedRecipeService.addRecipeToMeal(
                weeklyMealPlanId,
                plannedMealId,
                request
        );
    }

    @Operation(
            summary = "Remove a recipe from a meal slot",
            description = "Removes the recipe assigned to the specified meal slot."
    )
    @DeleteMapping("/{weeklyMealPlanId}/meals/{plannedMealId}/recipe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRecipeFromMeal(
            @PathVariable Long weeklyMealPlanId,
            @PathVariable Long plannedMealId
    ) {
        plannedRecipeService.removeRecipeFromMeal(
                weeklyMealPlanId,
                plannedMealId
        );
    }

    @Operation(
            summary = "Update recipe quantity",
            description = "Updates the quantity of the recipe assigned to the specified meal slot."
    )
    @PutMapping("/{weeklyMealPlanId}/meals/{plannedMealId}/recipe")
    public PlannedRecipeResponse updateRecipeQuantity(
            @PathVariable Long weeklyMealPlanId,
            @PathVariable Long plannedMealId,
            @Valid @RequestBody UpdateRecipeQuantityRequest request
    ) {
        return plannedRecipeService.updateRecipeQuantity(
                weeklyMealPlanId,
                plannedMealId,
                request
        );
    }
}