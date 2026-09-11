package com.mealmate.mealservice.service;

import com.mealmate.mealservice.dto.PlannedMealResponse;
import com.mealmate.mealservice.dto.WeeklyMealPlanResponse;
import com.mealmate.mealservice.entity.PlannedMeal;
import com.mealmate.mealservice.entity.WeeklyMealPlan;
import com.mealmate.mealservice.exception.InvalidMealPlanException;
import com.mealmate.mealservice.exception.MealPlanAlreadyExistsException;
import com.mealmate.mealservice.mapper.WeeklyMealPlanMapper;
import com.mealmate.mealservice.repository.WeeklyMealPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyMealPlanServiceTest {

    @Mock
    private WeeklyMealPlanRepository weeklyMealPlanRepository;

    @Mock
    private WeeklyMealPlanMapper weeklyMealPlanMapper;

    @InjectMocks
    private WeeklyMealPlanService weeklyMealPlanService;

    @Test
    void shouldCreateWeeklyMealPlanWith35Slots() {

        Long userId = 1L;
        LocalDate monday =
                LocalDate.of(2026, 9, 7);

        WeeklyMealPlan savedPlan =
                new WeeklyMealPlan();

        savedPlan.setWeeklyMealPlanId(1L);
        savedPlan.setUserId(userId);
        savedPlan.setWeekStartDate(monday);

        List<PlannedMeal> plannedMeals = new java.util.ArrayList<>();

        for (int mealSlotId = 1;
             mealSlotId <= 35;
             mealSlotId++) {

            PlannedMeal plannedMeal =
                    new PlannedMeal();

            plannedMeal.setPlannedMealId(
                    (long) mealSlotId
            );

            plannedMeal.setMealSlotId(mealSlotId);
            plannedMeal.setWeeklyMealPlan(savedPlan);

            plannedMeals.add(plannedMeal);
        }

        savedPlan.setPlannedMeals(plannedMeals);

        when(weeklyMealPlanRepository
                .findByUserIdAndWeekStartDate(
                        userId,
                        monday
                ))
                .thenReturn(Optional.empty());

        when(weeklyMealPlanRepository.save(
                any(WeeklyMealPlan.class)
        ))
                .thenAnswer(invocation -> {

                    WeeklyMealPlan plan =
                            invocation.getArgument(0);

                    if (plan.getWeeklyMealPlanId() == null) {
                        plan.setWeeklyMealPlanId(1L);
                    }

                    return plan;
                });

        WeeklyMealPlanResponse expectedResponse =
                new WeeklyMealPlanResponse(
                        1L,
                        userId,
                        monday,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        plannedMeals.stream()
                                .map(meal ->
                                        new PlannedMealResponse(
                                                meal.getPlannedMealId(),
                                                meal.getMealSlotId()
                                        )
                                )
                                .toList()
                );

        when(weeklyMealPlanMapper.toResponse(any(
                WeeklyMealPlan.class
        )))
                .thenReturn(expectedResponse);

        WeeklyMealPlanResponse result =
                weeklyMealPlanService.createWeeklyMealPlan(
                        userId,
                        monday
                );

        assertNotNull(result);

        assertEquals(
                userId,
                result.userId()
        );

        assertEquals(
                monday,
                result.weekStartDate()
        );

        assertNotNull(
                result.plannedMeals()
        );

        assertEquals(
                35,
                result.plannedMeals().size()
        );

        assertEquals(
                1,
                result.plannedMeals().get(0).mealSlotId()
        );

        assertEquals(
                35,
                result.plannedMeals().get(34).mealSlotId()
        );

        verify(
                weeklyMealPlanRepository,
                times(2)
        ).save(any(WeeklyMealPlan.class));

        verify(weeklyMealPlanMapper)
                .toResponse(any(WeeklyMealPlan.class));
    }

    @Test
    void shouldRejectNonMondayStartDate() {

        Long userId = 1L;

        LocalDate tuesday =
                LocalDate.of(2026, 9, 8);

        assertThrows(
                InvalidMealPlanException.class,
                () -> weeklyMealPlanService
                        .createWeeklyMealPlan(
                                userId,
                                tuesday
                        )
        );

        verify(
                weeklyMealPlanRepository,
                never()
        ).save(any(WeeklyMealPlan.class));

        verifyNoInteractions(weeklyMealPlanMapper);
    }

    @Test
    void shouldRejectDuplicateWeeklyMealPlan() {

        Long userId = 1L;

        LocalDate monday =
                LocalDate.of(2026, 9, 7);

        WeeklyMealPlan existingPlan =
                new WeeklyMealPlan();

        when(weeklyMealPlanRepository
                .findByUserIdAndWeekStartDate(
                        userId,
                        monday
                ))
                .thenReturn(Optional.of(existingPlan));

        assertThrows(
                MealPlanAlreadyExistsException.class,
                () -> weeklyMealPlanService
                        .createWeeklyMealPlan(
                                userId,
                                monday
                        )
        );

        verify(
                weeklyMealPlanRepository,
                never()
        ).save(any(WeeklyMealPlan.class));

        verifyNoInteractions(weeklyMealPlanMapper);
    }
}