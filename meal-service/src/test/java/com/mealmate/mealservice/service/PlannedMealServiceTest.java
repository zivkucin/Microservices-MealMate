package com.mealmate.mealservice.service;

import com.mealmate.mealservice.dto.PlannedMealContentResponse;
import com.mealmate.mealservice.dto.PlannedRecipeResponse;
import com.mealmate.mealservice.dto.PlannedSnackResponse;
import com.mealmate.mealservice.entity.PlannedMeal;
import com.mealmate.mealservice.entity.PlannedRecipe;
import com.mealmate.mealservice.entity.PlannedSnack;
import com.mealmate.mealservice.entity.Snack;
import com.mealmate.mealservice.entity.WeeklyMealPlan;
import com.mealmate.mealservice.enums.MeasurementUnit;
import com.mealmate.mealservice.mapper.PlannedRecipeMapper;
import com.mealmate.mealservice.mapper.PlannedSnackMapper;
import com.mealmate.mealservice.repository.PlannedMealRepository;
import com.mealmate.mealservice.repository.PlannedRecipeRepository;
import com.mealmate.mealservice.repository.PlannedSnackRepository;
import org.junit.jupiter.api.BeforeEach;
import com.mealmate.mealservice.exception.InvalidMealPlanException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlannedMealServiceTest {

    @Mock
    private PlannedMealRepository plannedMealRepository;

    @Mock
    private PlannedRecipeRepository plannedRecipeRepository;

    @Mock
    private PlannedSnackRepository plannedSnackRepository;

    @Mock
    private PlannedRecipeMapper plannedRecipeMapper;

    @Mock
    private PlannedSnackMapper plannedSnackMapper;

    @InjectMocks
    private PlannedMealService plannedMealService;

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
        plannedMeal.setPlannedMealId(38L);
        plannedMeal.setMealSlotId(3);
        plannedMeal.setWeeklyMealPlan(weeklyMealPlan);
    }

    @Test
    void shouldGetMealWithRecipe() {

        PlannedRecipe plannedRecipe = new PlannedRecipe();

        plannedRecipe.setPlannedRecipeId(1L);
        plannedRecipe.setPlannedMeal(plannedMeal);
        plannedRecipe.setRecipeId(1L);
        plannedRecipe.setQuantity(2);

        PlannedRecipeResponse recipeResponse =
                new PlannedRecipeResponse(
                        1L,
                        38L,
                        1L,
                        2
                );

        when(plannedMealRepository.findById(38L))
                .thenReturn(Optional.of(plannedMeal));

        when(plannedRecipeRepository
                .findByPlannedMealPlannedMealId(38L))
                .thenReturn(Optional.of(plannedRecipe));

        when(plannedSnackRepository
                .findAllByPlannedMealPlannedMealId(38L))
                .thenReturn(List.of());

        when(plannedRecipeMapper.toResponse(plannedRecipe))
                .thenReturn(recipeResponse);

        PlannedMealContentResponse result =
                plannedMealService.getMealContent(
                        2L,
                        38L
                );

        assertNotNull(result);

        assertEquals(
                38L,
                result.plannedMealId()
        );

        assertEquals(
                3,
                result.mealSlotId()
        );

        assertNotNull(result.recipe());

        assertEquals(
                1L,
                result.recipe().plannedRecipeId()
        );

        assertEquals(
                1L,
                result.recipe().recipeId()
        );

        assertEquals(
                2,
                result.recipe().quantity()
        );

        assertTrue(result.snacks().isEmpty());

        verify(plannedRecipeMapper)
                .toResponse(plannedRecipe);

        verifyNoInteractions(plannedSnackMapper);
    }

    @Test
    void shouldGetMealWithSnacks() {

        Snack snack = new Snack();
        snack.setSnackId(1L);
        snack.setName("Banana");
        snack.setDefaultUnit(MeasurementUnit.PIECE);

        PlannedSnack plannedSnack = new PlannedSnack();

        plannedSnack.setPlannedSnackId(1L);
        plannedSnack.setPlannedMeal(plannedMeal);
        plannedSnack.setSnack(snack);
        plannedSnack.setQuantity(2.0);
        plannedSnack.setUnit(MeasurementUnit.PIECE);

        PlannedSnackResponse snackResponse =
                new PlannedSnackResponse(
                        1L,
                        38L,
                        1L,
                        2.0,
                        MeasurementUnit.PIECE
                );

        when(plannedMealRepository.findById(38L))
                .thenReturn(Optional.of(plannedMeal));

        when(plannedRecipeRepository
                .findByPlannedMealPlannedMealId(38L))
                .thenReturn(Optional.empty());

        when(plannedSnackRepository
                .findAllByPlannedMealPlannedMealId(38L))
                .thenReturn(List.of(plannedSnack));

        when(plannedSnackMapper.toResponse(plannedSnack))
                .thenReturn(snackResponse);

        PlannedMealContentResponse result =
                plannedMealService.getMealContent(
                        2L,
                        38L
                );

        assertNotNull(result);

        assertNull(result.recipe());

        assertNotNull(result.snacks());

        assertEquals(
                1,
                result.snacks().size()
        );

        assertEquals(
                1L,
                result.snacks().get(0).plannedSnackId()
        );

        assertEquals(
                1L,
                result.snacks().get(0).snackId()
        );

        assertEquals(
                2.0,
                result.snacks().get(0).quantity()
        );

        assertEquals(
                MeasurementUnit.PIECE,
                result.snacks().get(0).unit()
        );

        verify(plannedSnackMapper)
                .toResponse(plannedSnack);

        verifyNoInteractions(plannedRecipeMapper);
    }

    @Test
    void shouldGetEmptyMeal() {

        when(plannedMealRepository.findById(38L))
                .thenReturn(Optional.of(plannedMeal));

        when(plannedRecipeRepository
                .findByPlannedMealPlannedMealId(38L))
                .thenReturn(Optional.empty());

        when(plannedSnackRepository
                .findAllByPlannedMealPlannedMealId(38L))
                .thenReturn(List.of());

        PlannedMealContentResponse result =
                plannedMealService.getMealContent(
                        2L,
                        38L
                );

        assertNotNull(result);

        assertEquals(
                38L,
                result.plannedMealId()
        );

        assertEquals(
                3,
                result.mealSlotId()
        );

        assertNull(result.recipe());

        assertTrue(result.snacks().isEmpty());

        verifyNoInteractions(plannedRecipeMapper);
        verifyNoInteractions(plannedSnackMapper);
    }

    @Test
    void shouldRejectMealFromAnotherPlan() {

        when(plannedMealRepository.findById(38L))
                .thenReturn(Optional.of(plannedMeal));

        InvalidMealPlanException exception =
                assertThrows(
                        InvalidMealPlanException.class,
                        () -> plannedMealService.getMealContent(
                                999L,
                                38L
                        )
                );

        assertEquals(
                "Planned meal does not belong to this weekly meal plan.",
                exception.getMessage()
        );

        verifyNoInteractions(plannedRecipeRepository);
        verifyNoInteractions(plannedSnackRepository);
        verifyNoInteractions(plannedRecipeMapper);
        verifyNoInteractions(plannedSnackMapper);
    }

    @Test
    void shouldClearMeal() {

        PlannedRecipe plannedRecipe = new PlannedRecipe();

        plannedRecipe.setPlannedRecipeId(1L);
        plannedRecipe.setPlannedMeal(plannedMeal);

        when(plannedMealRepository.findById(38L))
                .thenReturn(Optional.of(plannedMeal));

        when(plannedRecipeRepository
                .findByPlannedMealPlannedMealId(38L))
                .thenReturn(Optional.of(plannedRecipe));

        plannedMealService.clearMeal(
                2L,
                38L
        );

        verify(plannedRecipeRepository)
                .delete(plannedRecipe);

        verify(plannedSnackRepository)
                .deleteAllByPlannedMealPlannedMealId(38L);

        verifyNoInteractions(plannedRecipeMapper);
        verifyNoInteractions(plannedSnackMapper);
    }
}