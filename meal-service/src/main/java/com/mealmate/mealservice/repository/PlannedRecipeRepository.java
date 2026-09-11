package com.mealmate.mealservice.repository;

import com.mealmate.mealservice.entity.PlannedRecipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlannedRecipeRepository extends JpaRepository<PlannedRecipe, Long> {

    Optional<PlannedRecipe> findByPlannedMealPlannedMealId(Long plannedMealId);

    boolean existsByPlannedMealPlannedMealId(Long plannedMealId);
}