package com.mealmate.mealservice.controller;

import com.mealmate.mealservice.dto.CreateWeeklyMealPlanRequest;
import com.mealmate.mealservice.dto.WeeklyMealPlanResponse;
import com.mealmate.mealservice.service.WeeklyMealPlanService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meal-plans")
public class WeeklyMealPlanController {

    private final WeeklyMealPlanService weeklyMealPlanService;

    public WeeklyMealPlanController(
            WeeklyMealPlanService weeklyMealPlanService
    ) {
        this.weeklyMealPlanService = weeklyMealPlanService;
    }

    @Operation(
            summary = "Create a weekly meal plan",
            description = "Creates a weekly meal plan with 35 empty meal slots."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WeeklyMealPlanResponse createWeeklyMealPlan(
            @Valid @RequestBody CreateWeeklyMealPlanRequest request
    ) {
        return weeklyMealPlanService.createWeeklyMealPlan(
                request.userId(),
                request.weekStartDate()
        );
    }

    @Operation(
            summary = "Get a weekly meal plan",
            description = "Returns the weekly meal plan with its meal slots."
    )
    @GetMapping("/{weeklyMealPlanId}")
    public WeeklyMealPlanResponse getWeeklyMealPlan(
            @PathVariable Long weeklyMealPlanId
    ) {
        return weeklyMealPlanService.getWeeklyMealPlan(
                weeklyMealPlanId
        );
    }
}