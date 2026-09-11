package com.mealmate.mealservice.repository;

import com.mealmate.mealservice.entity.PlannedMeal;
import com.mealmate.mealservice.entity.PlannedRecipe;
import com.mealmate.mealservice.entity.WeeklyMealPlan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class PlannedRecipeRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("mealmate_test")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );
        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );
    }

    @Autowired
    private WeeklyMealPlanRepository weeklyMealPlanRepository;

    @Autowired
    private PlannedMealRepository plannedMealRepository;

    @Autowired
    private PlannedRecipeRepository plannedRecipeRepository;

    @Test
    void shouldFindPlannedRecipeByPlannedMealId() {

        WeeklyMealPlan plan = new WeeklyMealPlan();
        plan.setUserId(1L);
        plan.setWeekStartDate(
                LocalDate.of(2026, 9, 14)
        );

        WeeklyMealPlan savedPlan =
                weeklyMealPlanRepository.save(plan);

        PlannedMeal plannedMeal = new PlannedMeal();
        plannedMeal.setMealSlotId(1);
        plannedMeal.setWeeklyMealPlan(savedPlan);

        PlannedMeal savedMeal =
                plannedMealRepository.save(plannedMeal);

        PlannedRecipe plannedRecipe = new PlannedRecipe();
        plannedRecipe.setPlannedMeal(savedMeal);
        plannedRecipe.setRecipeId(1L);
        plannedRecipe.setQuantity(2);

        PlannedRecipe savedRecipe =
                plannedRecipeRepository.save(plannedRecipe);

        var found =
                plannedRecipeRepository
                        .findByPlannedMealPlannedMealId(
                                savedMeal.getPlannedMealId()
                        );

        assertThat(savedRecipe.getPlannedRecipeId())
                .isNotNull();

        assertThat(found)
                .isPresent();

        assertThat(found.get().getRecipeId())
                .isEqualTo(1L);

        assertThat(found.get().getQuantity())
                .isEqualTo(2);

        assertThat(
                found.get()
                        .getPlannedMeal()
                        .getPlannedMealId()
        ).isEqualTo(
                savedMeal.getPlannedMealId()
        );
    }
}