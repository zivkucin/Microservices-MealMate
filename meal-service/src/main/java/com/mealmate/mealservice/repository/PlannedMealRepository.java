package com.mealmate.mealservice.repository;

import com.mealmate.mealservice.entity.PlannedMeal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlannedMealRepository extends JpaRepository<PlannedMeal, Long> {

    Optional<PlannedMeal> findByWeeklyMealPlanWeeklyMealPlanIdAndMealSlotId(
            Long weeklyMealPlanId,
            Integer mealSlotId
    );

    List<PlannedMeal> findAllByWeeklyMealPlanWeeklyMealPlanIdOrderByMealSlotId(
            Long weeklyMealPlanId
    );

    boolean existsByPlannedMealIdAndWeeklyMealPlanWeeklyMealPlanId(
            Long plannedMealId,
            Long weeklyMealPlanId
    );
}