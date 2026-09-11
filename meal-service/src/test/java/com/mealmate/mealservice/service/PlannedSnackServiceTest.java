package com.mealmate.mealservice.service;

import com.mealmate.mealservice.dto.AddSnackToMealRequest;
import com.mealmate.mealservice.dto.PlannedSnackResponse;
import com.mealmate.mealservice.entity.PlannedMeal;
import com.mealmate.mealservice.entity.PlannedSnack;
import com.mealmate.mealservice.entity.Snack;
import com.mealmate.mealservice.entity.WeeklyMealPlan;
import com.mealmate.mealservice.enums.MeasurementUnit;
import com.mealmate.mealservice.exception.InvalidMealPlanException;
import com.mealmate.mealservice.exception.RecipeAlreadyAssignedException;
import com.mealmate.mealservice.exception.SnackNotFoundException;
import com.mealmate.mealservice.mapper.PlannedSnackMapper;
import com.mealmate.mealservice.repository.PlannedMealRepository;
import com.mealmate.mealservice.repository.PlannedRecipeRepository;
import com.mealmate.mealservice.repository.PlannedSnackRepository;
import com.mealmate.mealservice.repository.SnackRepository;
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
class PlannedSnackServiceTest {

    @Mock
    private PlannedMealRepository plannedMealRepository;

    @Mock
    private PlannedRecipeRepository plannedRecipeRepository;

    @Mock
    private PlannedSnackRepository plannedSnackRepository;

    @Mock
    private SnackRepository snackRepository;

    @Mock
    private PlannedSnackMapper plannedSnackMapper;

    @InjectMocks
    private PlannedSnackService plannedSnackService;

    private WeeklyMealPlan weeklyMealPlan;
    private PlannedMeal plannedMeal;
    private Snack snack;

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

        snack = new Snack();
        snack.setSnackId(1L);
        snack.setName("Banana");
        snack.setDefaultUnit(MeasurementUnit.PIECE);
    }

    @Test
    void shouldAddSnackToMeal() {

        AddSnackToMealRequest request =
                new AddSnackToMealRequest(
                        1L,
                        2.0,
                        MeasurementUnit.PIECE
                );

        PlannedSnack plannedSnack = new PlannedSnack();

        plannedSnack.setPlannedSnackId(1L);
        plannedSnack.setPlannedMeal(plannedMeal);
        plannedSnack.setSnack(snack);
        plannedSnack.setQuantity(2.0);
        plannedSnack.setUnit(MeasurementUnit.PIECE);

        when(plannedMealRepository.findById(38L))
                .thenReturn(Optional.of(plannedMeal));

        when(plannedRecipeRepository
                .existsByPlannedMealPlannedMealId(38L))
                .thenReturn(false);

        when(snackRepository.findById(1L))
                .thenReturn(Optional.of(snack));

        when(plannedSnackRepository.save(any(PlannedSnack.class)))
                .thenReturn(plannedSnack);

        when(plannedSnackMapper.toResponse(plannedSnack))
                .thenReturn(new PlannedSnackResponse(
                        1L,
                        38L,
                        1L,
                        2.0,
                        MeasurementUnit.PIECE
                ));

        PlannedSnackResponse result =
                plannedSnackService.addSnackToMeal(
                        2L,
                        38L,
                        request
                );

        assertNotNull(result);
        assertEquals(1L, result.plannedSnackId());
        assertEquals(38L, result.plannedMealId());
        assertEquals(1L, result.snackId());
        assertEquals(2.0, result.quantity());
        assertEquals(
                MeasurementUnit.PIECE,
                result.unit()
        );

        verify(plannedSnackRepository)
                .save(any(PlannedSnack.class));

        verify(plannedSnackMapper)
                .toResponse(plannedSnack);
    }

    @Test
    void shouldAllowMultipleSnacksInSameMeal() {

        when(plannedMealRepository.findById(38L))
                .thenReturn(Optional.of(plannedMeal));

        when(plannedRecipeRepository
                .existsByPlannedMealPlannedMealId(38L))
                .thenReturn(false);

        when(snackRepository.findById(1L))
                .thenReturn(Optional.of(snack));

        PlannedSnack firstSnack = new PlannedSnack();

        firstSnack.setPlannedSnackId(1L);
        firstSnack.setPlannedMeal(plannedMeal);
        firstSnack.setSnack(snack);
        firstSnack.setQuantity(2.0);
        firstSnack.setUnit(MeasurementUnit.PIECE);

        when(plannedSnackRepository.save(any(PlannedSnack.class)))
                .thenReturn(firstSnack);

        when(plannedSnackMapper.toResponse(firstSnack))
                .thenReturn(new PlannedSnackResponse(
                        1L,
                        38L,
                        1L,
                        2.0,
                        MeasurementUnit.PIECE
                ));

        PlannedSnackResponse result =
                plannedSnackService.addSnackToMeal(
                        2L,
                        38L,
                        new AddSnackToMealRequest(
                                1L,
                                2.0,
                                MeasurementUnit.PIECE
                        )
                );

        assertNotNull(result);
        assertEquals(1L, result.plannedSnackId());

        verify(plannedSnackRepository)
                .save(any(PlannedSnack.class));

        verify(plannedSnackMapper)
                .toResponse(firstSnack);
    }

    @Test
    void shouldRejectMealFromAnotherPlan() {

        when(plannedMealRepository.findById(38L))
                .thenReturn(Optional.of(plannedMeal));

        InvalidMealPlanException exception =
                assertThrows(
                        InvalidMealPlanException.class,
                        () -> plannedSnackService.addSnackToMeal(
                                999L,
                                38L,
                                new AddSnackToMealRequest(
                                        1L,
                                        2.0,
                                        MeasurementUnit.PIECE
                                )
                        )
                );

        assertEquals(
                "Planned meal does not belong to this weekly meal plan.",
                exception.getMessage()
        );

        verify(plannedSnackRepository, never())
                .save(any(PlannedSnack.class));

        verifyNoInteractions(plannedSnackMapper);
    }

    @Test
    void shouldRejectSnackWhenRecipeAlreadyExists() {

        when(plannedMealRepository.findById(38L))
                .thenReturn(Optional.of(plannedMeal));

        when(plannedRecipeRepository
                .existsByPlannedMealPlannedMealId(38L))
                .thenReturn(true);

        RecipeAlreadyAssignedException exception =
                assertThrows(
                        RecipeAlreadyAssignedException.class,
                        () -> plannedSnackService.addSnackToMeal(
                                2L,
                                38L,
                                new AddSnackToMealRequest(
                                        1L,
                                        2.0,
                                        MeasurementUnit.PIECE
                                )
                        )
                );

        assertEquals(
                "A recipe is already assigned to this meal slot.",
                exception.getMessage()
        );

        verify(plannedSnackRepository, never())
                .save(any(PlannedSnack.class));

        verifyNoInteractions(plannedSnackMapper);
    }

    @Test
    void shouldRejectNonExistingSnack() {

        when(plannedMealRepository.findById(38L))
                .thenReturn(Optional.of(plannedMeal));

        when(plannedRecipeRepository
                .existsByPlannedMealPlannedMealId(38L))
                .thenReturn(false);

        when(snackRepository.findById(999L))
                .thenReturn(Optional.empty());

        SnackNotFoundException exception =
                assertThrows(
                        SnackNotFoundException.class,
                        () -> plannedSnackService.addSnackToMeal(
                                2L,
                                38L,
                                new AddSnackToMealRequest(
                                        999L,
                                        2.0,
                                        MeasurementUnit.PIECE
                                )
                        )
                );

        assertEquals(
                "Snack not found.",
                exception.getMessage()
        );

        verify(plannedSnackRepository, never())
                .save(any(PlannedSnack.class));

        verifyNoInteractions(plannedSnackMapper);
    }

    @Test
    void shouldRemoveSnackFromMeal() {

        PlannedSnack plannedSnack = new PlannedSnack();

        plannedSnack.setPlannedSnackId(1L);
        plannedSnack.setPlannedMeal(plannedMeal);
        plannedSnack.setSnack(snack);
        plannedSnack.setQuantity(2.0);
        plannedSnack.setUnit(MeasurementUnit.PIECE);

        when(plannedMealRepository.findById(38L))
                .thenReturn(Optional.of(plannedMeal));

        when(plannedSnackRepository.findById(1L))
                .thenReturn(Optional.of(plannedSnack));

        plannedSnackService.removeSnackFromMeal(
                2L,
                38L,
                1L
        );

        verify(plannedSnackRepository)
                .delete(plannedSnack);

        verifyNoInteractions(plannedSnackMapper);
    }
}