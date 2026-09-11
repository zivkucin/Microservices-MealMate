package com.mealmate.mealservice.repository;

import com.mealmate.mealservice.entity.WeeklyMealPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface WeeklyMealPlanRepository extends JpaRepository<WeeklyMealPlan, Long> {

    Optional<WeeklyMealPlan> findByUserIdAndWeekStartDate(
            Long userId,
            LocalDate weekStartDate
    );
}