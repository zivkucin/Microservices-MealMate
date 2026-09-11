package com.mealmate.mealservice.service;

import com.mealmate.mealservice.dto.AddSnackToMealRequest;
import com.mealmate.mealservice.dto.PlannedSnackResponse;
import com.mealmate.mealservice.entity.PlannedMeal;
import com.mealmate.mealservice.entity.PlannedSnack;
import com.mealmate.mealservice.exception.InvalidMealPlanException;
import com.mealmate.mealservice.exception.PlannedMealNotFoundException;
import com.mealmate.mealservice.exception.PlannedSnackNotFoundException;
import com.mealmate.mealservice.exception.RecipeAlreadyAssignedException;
import com.mealmate.mealservice.exception.SnackNotFoundException;
import com.mealmate.mealservice.mapper.PlannedSnackMapper;
import com.mealmate.mealservice.repository.PlannedMealRepository;
import com.mealmate.mealservice.repository.PlannedRecipeRepository;
import com.mealmate.mealservice.repository.PlannedSnackRepository;
import com.mealmate.mealservice.repository.SnackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlannedSnackService {

    private final PlannedMealRepository plannedMealRepository;
    private final PlannedRecipeRepository plannedRecipeRepository;
    private final PlannedSnackRepository plannedSnackRepository;
    private final SnackRepository snackRepository;
    private final PlannedSnackMapper plannedSnackMapper;

    public PlannedSnackService(
            PlannedMealRepository plannedMealRepository,
            PlannedRecipeRepository plannedRecipeRepository,
            PlannedSnackRepository plannedSnackRepository,
            SnackRepository snackRepository,
            PlannedSnackMapper plannedSnackMapper
    ) {
        this.plannedMealRepository = plannedMealRepository;
        this.plannedRecipeRepository = plannedRecipeRepository;
        this.plannedSnackRepository = plannedSnackRepository;
        this.snackRepository = snackRepository;
        this.plannedSnackMapper = plannedSnackMapper;
    }

    @Transactional
    public PlannedSnackResponse addSnackToMeal(
            Long weeklyMealPlanId,
            Long plannedMealId,
            AddSnackToMealRequest request
    ) {
        PlannedMeal plannedMeal =
                findAndValidatePlannedMeal(
                        weeklyMealPlanId,
                        plannedMealId
                );

        if (plannedRecipeRepository
                .existsByPlannedMealPlannedMealId(plannedMealId)) {

            throw new RecipeAlreadyAssignedException(
                    "A recipe is already assigned to this meal slot."
            );
        }

        var snack = snackRepository.findById(request.snackId())
                .orElseThrow(() ->
                        new SnackNotFoundException(
                                "Snack not found."
                        )
                );

        PlannedSnack plannedSnack = new PlannedSnack();

        plannedSnack.setPlannedMeal(plannedMeal);
        plannedSnack.setSnack(snack);
        plannedSnack.setQuantity(request.quantity());
        plannedSnack.setUnit(request.unit());

        PlannedSnack saved =
                plannedSnackRepository.save(plannedSnack);

        return plannedSnackMapper.toResponse(saved);
    }

    @Transactional
    public void removeSnackFromMeal(
            Long weeklyMealPlanId,
            Long plannedMealId,
            Long plannedSnackId
    ) {
        PlannedMeal plannedMeal =
                findAndValidatePlannedMeal(
                        weeklyMealPlanId,
                        plannedMealId
                );

        PlannedSnack plannedSnack =
                plannedSnackRepository.findById(plannedSnackId)
                        .orElseThrow(() ->
                                new PlannedSnackNotFoundException(
                                        "Planned snack not found."
                                )
                        );

        if (!plannedSnack.getPlannedMeal()
                .getPlannedMealId()
                .equals(plannedMealId)) {

            throw new InvalidMealPlanException(
                    "Planned snack does not belong to this meal slot."
            );
        }

        plannedSnackRepository.delete(plannedSnack);
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