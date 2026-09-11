package com.mealmate.mealservice.repository;

import com.mealmate.mealservice.entity.PlannedMeal;
import com.mealmate.mealservice.entity.PlannedSnack;
import com.mealmate.mealservice.entity.Snack;
import com.mealmate.mealservice.entity.WeeklyMealPlan;
import com.mealmate.mealservice.enums.MeasurementUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class PlannedSnackRepositoryIntegrationTest {

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
    private SnackRepository snackRepository;

    @Autowired
    private PlannedSnackRepository plannedSnackRepository;

    @Test
    void shouldFindAllPlannedSnacksByPlannedMealId() {

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

        Snack almonds = new Snack();
        almonds.setName("Almonds");
        almonds.setDefaultUnit(MeasurementUnit.GRAM);

        Snack savedAlmonds =
                snackRepository.save(almonds);

        Snack banana = new Snack();
        banana.setName("Banana");
        banana.setDefaultUnit(MeasurementUnit.PIECE);

        Snack savedBanana =
                snackRepository.save(banana);

        PlannedSnack plannedAlmonds = new PlannedSnack();
        plannedAlmonds.setPlannedMeal(savedMeal);
        plannedAlmonds.setSnack(savedAlmonds);
        plannedAlmonds.setQuantity(30.0);
        plannedAlmonds.setUnit(MeasurementUnit.GRAM);

        PlannedSnack plannedBanana = new PlannedSnack();
        plannedBanana.setPlannedMeal(savedMeal);
        plannedBanana.setSnack(savedBanana);
        plannedBanana.setQuantity(2.0);
        plannedBanana.setUnit(MeasurementUnit.PIECE);

        plannedSnackRepository.save(plannedAlmonds);
        plannedSnackRepository.save(plannedBanana);

        List<PlannedSnack> found =
                plannedSnackRepository
                        .findAllByPlannedMealPlannedMealId(
                                savedMeal.getPlannedMealId()
                        );

        assertThat(found)
                .hasSize(2);

        assertThat(found)
                .extracting(
                        plannedSnack ->
                                plannedSnack.getSnack().getName()
                )
                .containsExactlyInAnyOrder(
                        "Almonds",
                        "Banana"
                );
    }
}