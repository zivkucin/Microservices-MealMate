package com.mealmate.mealservice.service;

import com.mealmate.mealservice.dto.AddRecipeToMealRequest;
import com.mealmate.mealservice.dto.PlannedRecipeResponse;
import com.mealmate.mealservice.dto.UpdateRecipeQuantityRequest;
import com.mealmate.mealservice.entity.PlannedMeal;
import com.mealmate.mealservice.entity.PlannedRecipe;
import com.mealmate.mealservice.entity.WeeklyMealPlan;
import com.mealmate.mealservice.exception.InvalidMealPlanException;
import com.mealmate.mealservice.exception.RecipeAlreadyAssignedException;
import com.mealmate.mealservice.mapper.PlannedRecipeMapper;
import com.mealmate.mealservice.repository.PlannedMealRepository;
import com.mealmate.mealservice.repository.PlannedRecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlannedRecipeServiceTest {

    @Mock
    private PlannedMealRepository plannedMealRepository;

    @Mock
    private PlannedRecipeRepository plannedRecipeRepository;

    @Mock
    private PlannedRecipeMapper plannedRecipeMapper;

    @InjectMocks
    private PlannedRecipeService plannedRecipeService;

    private WeeklyMealPlan weeklyMealPlan;
    private PlannedMeal plannedMeal;

    @BeforeEach
    void setUp() {
        weeklyMealPlan = new WeeklyMealPlan();
        weeklyMealPlan.setWeeklyMealPlanId(2L);
        weeklyMealPlan.setUserId(1L);
        weeklyMealPlan.setWeekStartDate(
                LocalDate.of(2026, 9, 14)
        );

        plannedMeal = new PlannedMeal();
        plannedMeal.setPlannedMealId(36L);
        plannedMeal.setMealSlotId(1);
        plannedMeal.setWeeklyMealPlan(weeklyMealPlan);
    }

    @Test
    void shouldAddRecipeToMeal() {

        AddRecipeToMealRequest request =
                new AddRecipeToMealRequest(1L, 2);

        PlannedRecipe plannedRecipe = new PlannedRecipe();

        plannedRecipe.setPlannedRecipeId(1L);
        plannedRecipe.setPlannedMeal(plannedMeal);
        plannedRecipe.setRecipeId(1L);
        plannedRecipe.setQuantity(2);

        when(plannedMealRepository.findById(36L))
                .thenReturn(Optional.of(plannedMeal));

        when(plannedRecipeRepository
                .existsByPlannedMealPlannedMealId(36L))
                .thenReturn(false);

        when(plannedRecipeRepository.save(any(PlannedRecipe.class)))
                .thenReturn(plannedRecipe);

        when(plannedRecipeMapper.toResponse(plannedRecipe))
                .thenReturn(new PlannedRecipeResponse(
                        1L,
                        36L,
                        1L,
                        2
                ));

        PlannedRecipeResponse result =
                plannedRecipeService.addRecipeToMeal(
                        2L,
                        36L,
                        request
                );

        assertNotNull(result);
        assertEquals(1L, result.plannedRecipeId());
        assertEquals(36L, result.plannedMealId());
        assertEquals(1L, result.recipeId());
        assertEquals(2, result.quantity());

        verify(plannedRecipeRepository)
                .save(any(PlannedRecipe.class));

        verify(plannedRecipeMapper)
                .toResponse(plannedRecipe);
    }

    @Test
    void shouldRejectMealFromAnotherPlan() {

        WeeklyMealPlan anotherWeeklyMealPlan =
                new WeeklyMealPlan();

        anotherWeeklyMealPlan.setWeeklyMealPlanId(999L);

        plannedMeal.setWeeklyMealPlan(
                anotherWeeklyMealPlan
        );

        when(plannedMealRepository.findById(36L))
                .thenReturn(Optional.of(plannedMeal));

        InvalidMealPlanException exception =
                assertThrows(
                        InvalidMealPlanException.class,
                        () -> plannedRecipeService.addRecipeToMeal(
                                2L,
                                36L,
                                new AddRecipeToMealRequest(1L, 1)
                        )
                );

        assertEquals(
                "Planned meal does not belong to this weekly meal plan.",
                exception.getMessage()
        );

        verify(plannedRecipeRepository, never())
                .save(any(PlannedRecipe.class));

        verifyNoInteractions(plannedRecipeMapper);
    }

    @Test
    void shouldRejectDuplicateRecipe() {

        when(plannedMealRepository.findById(36L))
                .thenReturn(Optional.of(plannedMeal));

        when(plannedRecipeRepository
                .existsByPlannedMealPlannedMealId(36L))
                .thenReturn(true);

        RecipeAlreadyAssignedException exception =
                assertThrows(
                        RecipeAlreadyAssignedException.class,
                        () -> plannedRecipeService.addRecipeToMeal(
                                2L,
                                36L,
                                new AddRecipeToMealRequest(1L, 1)
                        )
                );

        assertEquals(
                "A recipe is already assigned to this meal slot.",
                exception.getMessage()
        );

        verify(plannedRecipeRepository, never())
                .save(any(PlannedRecipe.class));

        verifyNoInteractions(plannedRecipeMapper);
    }

    @Test
    void shouldUpdateRecipeQuantity() {

        PlannedRecipe plannedRecipe = new PlannedRecipe();

        plannedRecipe.setPlannedRecipeId(1L);
        plannedRecipe.setPlannedMeal(plannedMeal);
        plannedRecipe.setRecipeId(1L);
        plannedRecipe.setQuantity(1);

        when(plannedMealRepository.findById(36L))
                .thenReturn(Optional.of(plannedMeal));

        when(plannedRecipeRepository
                .findByPlannedMealPlannedMealId(36L))
                .thenReturn(Optional.of(plannedRecipe));

        when(plannedRecipeRepository.save(plannedRecipe))
                .thenReturn(plannedRecipe);

        when(plannedRecipeMapper.toResponse(plannedRecipe))
                .thenReturn(new PlannedRecipeResponse(
                        1L,
                        36L,
                        1L,
                        3
                ));

        PlannedRecipeResponse result =
                plannedRecipeService.updateRecipeQuantity(
                        2L,
                        36L,
                        new UpdateRecipeQuantityRequest(3)
                );

        assertNotNull(result);
        assertEquals(3, result.quantity());
        assertEquals(1L, result.recipeId());

        verify(plannedRecipeRepository)
                .save(plannedRecipe);

        verify(plannedRecipeMapper)
                .toResponse(plannedRecipe);
    }

    @Test
    void shouldRemoveRecipeFromMeal() {

        PlannedRecipe plannedRecipe = new PlannedRecipe();

        plannedRecipe.setPlannedRecipeId(1L);
        plannedRecipe.setPlannedMeal(plannedMeal);

        when(plannedMealRepository.findById(36L))
                .thenReturn(Optional.of(plannedMeal));

        when(plannedRecipeRepository
                .findByPlannedMealPlannedMealId(36L))
                .thenReturn(Optional.of(plannedRecipe));

        plannedRecipeService.removeRecipeFromMeal(
                2L,
                36L
        );

        verify(plannedRecipeRepository)
                .delete(plannedRecipe);

        verifyNoInteractions(plannedRecipeMapper);
    }
}