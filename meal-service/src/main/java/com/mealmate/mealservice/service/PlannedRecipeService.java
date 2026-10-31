package com.mealmate.mealservice.service;

import com.mealmate.mealservice.dto.AddRecipeToMealRequest;
import com.mealmate.mealservice.dto.PlannedRecipeResponse;
import com.mealmate.mealservice.dto.UpdateRecipeQuantityRequest;
import com.mealmate.mealservice.entity.PlannedMeal;
import com.mealmate.mealservice.entity.PlannedRecipe;
import com.mealmate.mealservice.exception.InvalidMealPlanException;
import com.mealmate.mealservice.exception.NoRecipeAssignedException;
import com.mealmate.mealservice.exception.PlannedMealNotFoundException;
import com.mealmate.mealservice.exception.RecipeAlreadyAssignedException;
import com.mealmate.mealservice.mapper.PlannedRecipeMapper;
import com.mealmate.mealservice.repository.PlannedMealRepository;
import com.mealmate.mealservice.repository.PlannedRecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlannedRecipeService {

    private final PlannedMealRepository plannedMealRepository;
    private final PlannedRecipeRepository plannedRecipeRepository;
    private final PlannedRecipeMapper plannedRecipeMapper;

    public PlannedRecipeService(
            PlannedMealRepository plannedMealRepository,
            PlannedRecipeRepository plannedRecipeRepository,
            PlannedRecipeMapper plannedRecipeMapper
    ) {
        this.plannedMealRepository = plannedMealRepository;
        this.plannedRecipeRepository = plannedRecipeRepository;
        this.plannedRecipeMapper = plannedRecipeMapper;
    }

    @Transactional
    public PlannedRecipeResponse addRecipeToMeal(
            Long userId,
            Long weeklyMealPlanId,
            Long plannedMealId,
            AddRecipeToMealRequest request
    ) {
        PlannedMeal plannedMeal = findAndValidatePlannedMeal(
                userId,
                weeklyMealPlanId,
                plannedMealId
        );

        if (plannedRecipeRepository.existsByPlannedMealPlannedMealId(
                plannedMealId
        )) {
            throw new RecipeAlreadyAssignedException(
                    "A recipe is already assigned to this meal slot."
            );
        }

        PlannedRecipe plannedRecipe = new PlannedRecipe();
        plannedRecipe.setPlannedMeal(plannedMeal);
        plannedRecipe.setRecipeId(request.recipeId());
        plannedRecipe.setQuantity(request.quantity());

        PlannedRecipe saved =
                plannedRecipeRepository.save(plannedRecipe);

        return plannedRecipeMapper.toResponse(saved);
    }

    @Transactional
    public void removeRecipeFromMeal(
            Long userId,
            Long weeklyMealPlanId,
            Long plannedMealId
    ) {
        findAndValidatePlannedMeal(
                userId,
                weeklyMealPlanId,
                plannedMealId
        );

        PlannedRecipe plannedRecipe =
                plannedRecipeRepository
                        .findByPlannedMealPlannedMealId(plannedMealId)
                        .orElseThrow(() ->
                                new NoRecipeAssignedException(
                                        "No recipe assigned to this meal slot."
                                )
                        );

        plannedRecipeRepository.delete(plannedRecipe);
    }

    @Transactional
    public PlannedRecipeResponse updateRecipeQuantity(
            Long userId,
            Long weeklyMealPlanId,
            Long plannedMealId,
            UpdateRecipeQuantityRequest request
    ) {
        findAndValidatePlannedMeal(
                userId,
                weeklyMealPlanId,
                plannedMealId
        );

        PlannedRecipe plannedRecipe =
                plannedRecipeRepository
                        .findByPlannedMealPlannedMealId(plannedMealId)
                        .orElseThrow(() ->
                                new NoRecipeAssignedException(
                                        "No recipe assigned to this meal slot."
                                )
                        );

        plannedRecipe.setQuantity(request.quantity());

        PlannedRecipe updated =
                plannedRecipeRepository.save(plannedRecipe);

        return plannedRecipeMapper.toResponse(updated);
    }

    private PlannedMeal findAndValidatePlannedMeal(
            Long userId,
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

        Long planUserId =
                plannedMeal.getWeeklyMealPlan().getUserId();

        if (!planUserId.equals(userId)) {
            throw new InvalidMealPlanException(
                    "You are not allowed to access this weekly meal plan."
            );
        }

        return plannedMeal;
    }
}