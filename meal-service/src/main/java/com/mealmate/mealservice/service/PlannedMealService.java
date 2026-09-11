package com.mealmate.mealservice.service;

import com.mealmate.mealservice.dto.PlannedMealContentResponse;
import com.mealmate.mealservice.dto.PlannedRecipeResponse;
import com.mealmate.mealservice.dto.PlannedSnackResponse;
import com.mealmate.mealservice.entity.PlannedMeal;
import com.mealmate.mealservice.entity.PlannedRecipe;
import com.mealmate.mealservice.exception.InvalidMealPlanException;
import com.mealmate.mealservice.exception.PlannedMealNotFoundException;
import com.mealmate.mealservice.mapper.PlannedRecipeMapper;
import com.mealmate.mealservice.mapper.PlannedSnackMapper;
import com.mealmate.mealservice.repository.PlannedMealRepository;
import com.mealmate.mealservice.repository.PlannedRecipeRepository;
import com.mealmate.mealservice.repository.PlannedSnackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlannedMealService {

    private final PlannedMealRepository plannedMealRepository;
    private final PlannedRecipeRepository plannedRecipeRepository;
    private final PlannedSnackRepository plannedSnackRepository;
    private final PlannedRecipeMapper plannedRecipeMapper;
    private final PlannedSnackMapper plannedSnackMapper;

    public PlannedMealService(
            PlannedMealRepository plannedMealRepository,
            PlannedRecipeRepository plannedRecipeRepository,
            PlannedSnackRepository plannedSnackRepository,
            PlannedRecipeMapper plannedRecipeMapper,
            PlannedSnackMapper plannedSnackMapper
    ) {
        this.plannedMealRepository = plannedMealRepository;
        this.plannedRecipeRepository = plannedRecipeRepository;
        this.plannedSnackRepository = plannedSnackRepository;
        this.plannedRecipeMapper = plannedRecipeMapper;
        this.plannedSnackMapper = plannedSnackMapper;
    }

    @Transactional(readOnly = true)
    public PlannedMealContentResponse getMealContent(
            Long weeklyMealPlanId,
            Long plannedMealId
    ) {
        PlannedMeal plannedMeal =
                findAndValidatePlannedMeal(
                        weeklyMealPlanId,
                        plannedMealId
                );

        PlannedRecipeResponse recipeResponse = null;

        PlannedRecipe plannedRecipe =
                plannedRecipeRepository
                        .findByPlannedMealPlannedMealId(plannedMealId)
                        .orElse(null);

        if (plannedRecipe != null) {
            recipeResponse =
                    plannedRecipeMapper.toResponse(plannedRecipe);
        }

        List<PlannedSnackResponse> snackResponses =
                plannedSnackRepository
                        .findAllByPlannedMealPlannedMealId(plannedMealId)
                        .stream()
                        .map(plannedSnackMapper::toResponse)
                        .toList();

        return new PlannedMealContentResponse(
                plannedMeal.getPlannedMealId(),
                plannedMeal.getMealSlotId(),
                recipeResponse,
                snackResponses
        );
    }

    @Transactional
    public void clearMeal(
            Long weeklyMealPlanId,
            Long plannedMealId
    ) {
        findAndValidatePlannedMeal(
                weeklyMealPlanId,
                plannedMealId
        );

        plannedRecipeRepository
                .findByPlannedMealPlannedMealId(plannedMealId)
                .ifPresent(plannedRecipeRepository::delete);

        plannedSnackRepository
                .deleteAllByPlannedMealPlannedMealId(plannedMealId);
    }

    private PlannedMeal findAndValidatePlannedMeal(
            Long weeklyMealPlanId,
            Long plannedMealId
    ) {
        PlannedMeal plannedMeal =
                plannedMealRepository.findById(plannedMealId)
                        .orElseThrow(() ->
                                new PlannedMealNotFoundException(
                                        "Planned meal not found."
                                )
                        );

        if (!plannedMeal.getWeeklyMealPlan()
                .getWeeklyMealPlanId()
                .equals(weeklyMealPlanId)) {

            throw new InvalidMealPlanException(
                    "Planned meal does not belong to this weekly meal plan."
            );
        }

        return plannedMeal;
    }
}