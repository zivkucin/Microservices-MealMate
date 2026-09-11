package com.mealmate.mealservice.service;

import com.mealmate.mealservice.dto.WeeklyMealPlanResponse;
import com.mealmate.mealservice.entity.PlannedMeal;
import com.mealmate.mealservice.entity.WeeklyMealPlan;
import com.mealmate.mealservice.exception.InvalidMealPlanException;
import com.mealmate.mealservice.exception.MealPlanAlreadyExistsException;
import com.mealmate.mealservice.exception.WeeklyMealPlanNotFoundException;
import com.mealmate.mealservice.mapper.WeeklyMealPlanMapper;
import com.mealmate.mealservice.repository.WeeklyMealPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeeklyMealPlanService {

    private static final int TOTAL_MEAL_SLOTS = 35;

    private final WeeklyMealPlanRepository weeklyMealPlanRepository;
    private final WeeklyMealPlanMapper weeklyMealPlanMapper;

    public WeeklyMealPlanService(
            WeeklyMealPlanRepository weeklyMealPlanRepository,
            WeeklyMealPlanMapper weeklyMealPlanMapper
    ) {
        this.weeklyMealPlanRepository = weeklyMealPlanRepository;
        this.weeklyMealPlanMapper = weeklyMealPlanMapper;
    }

    @Transactional
    public WeeklyMealPlanResponse createWeeklyMealPlan(
            Long userId,
            LocalDate weekStartDate
    ) {
        validateWeekStartDate(weekStartDate);

        if (weeklyMealPlanRepository
                .findByUserIdAndWeekStartDate(userId, weekStartDate)
                .isPresent()) {

            throw new MealPlanAlreadyExistsException(
                    "Weekly meal plan already exists for this user and week."
            );
        }

        WeeklyMealPlan weeklyMealPlan = new WeeklyMealPlan();
        weeklyMealPlan.setUserId(userId);
        weeklyMealPlan.setWeekStartDate(weekStartDate);

        WeeklyMealPlan savedPlan =
                weeklyMealPlanRepository.save(weeklyMealPlan);

        List<PlannedMeal> plannedMeals = new ArrayList<>();

        for (int mealSlotId = 1;
             mealSlotId <= TOTAL_MEAL_SLOTS;
             mealSlotId++) {

            PlannedMeal plannedMeal = new PlannedMeal();
            plannedMeal.setMealSlotId(mealSlotId);
            plannedMeal.setWeeklyMealPlan(savedPlan);

            plannedMeals.add(plannedMeal);
        }

        savedPlan.setPlannedMeals(plannedMeals);

        WeeklyMealPlan savedFinalPlan =
                weeklyMealPlanRepository.save(savedPlan);

        return weeklyMealPlanMapper.toResponse(savedFinalPlan);
    }

    private void validateWeekStartDate(LocalDate weekStartDate) {

        if (weekStartDate == null) {
            throw new InvalidMealPlanException(
                    "Week start date cannot be null."
            );
        }

        if (weekStartDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new InvalidMealPlanException(
                    "Week start date must be a Monday."
            );
        }
    }

    @Transactional(readOnly = true)
    public WeeklyMealPlanResponse getWeeklyMealPlan(
            Long weeklyMealPlanId
    ) {
        WeeklyMealPlan plan =
                weeklyMealPlanRepository.findById(weeklyMealPlanId)
                        .orElseThrow(() ->
                                new WeeklyMealPlanNotFoundException(
                                        "Weekly meal plan not found."
                                )
                        );

        return weeklyMealPlanMapper.toResponse(plan);
    }
}