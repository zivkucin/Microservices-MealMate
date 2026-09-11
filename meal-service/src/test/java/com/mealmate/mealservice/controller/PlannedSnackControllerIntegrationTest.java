package com.mealmate.mealservice.controller;

import com.mealmate.mealservice.entity.PlannedMeal;
import com.mealmate.mealservice.entity.PlannedSnack;
import com.mealmate.mealservice.entity.Snack;
import com.mealmate.mealservice.entity.WeeklyMealPlan;
import com.mealmate.mealservice.enums.MeasurementUnit;
import com.mealmate.mealservice.repository.PlannedMealRepository;
import com.mealmate.mealservice.repository.PlannedSnackRepository;
import com.mealmate.mealservice.repository.SnackRepository;
import com.mealmate.mealservice.repository.WeeklyMealPlanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PlannedSnackControllerIntegrationTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("mealmate_test")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configureDatabase(
            DynamicPropertyRegistry registry
    ) {
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
    private MockMvc mockMvc;

    @Autowired
    private WeeklyMealPlanRepository weeklyMealPlanRepository;

    @Autowired
    private PlannedMealRepository plannedMealRepository;

    @Autowired
    private SnackRepository snackRepository;

    @Autowired
    private PlannedSnackRepository plannedSnackRepository;

    @Test
    void shouldAddSnackToMeal() throws Exception {

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

        Snack snack = new Snack();
        snack.setName("Almonds");
        snack.setDefaultUnit(MeasurementUnit.GRAM);

        Snack savedSnack =
                snackRepository.save(snack);

        String requestBody = """
                {
                    "snackId": %d,
                    "quantity": 30,
                    "unit": "GRAM"
                }
                """.formatted(savedSnack.getSnackId());

        mockMvc.perform(
                        post(
                                "/meal-plans/{weeklyMealPlanId}/meals/{plannedMealId}/snacks",
                                savedPlan.getWeeklyMealPlanId(),
                                savedMeal.getPlannedMealId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plannedSnackId").isNumber())
                .andExpect(
                        jsonPath("$.plannedMealId")
                                .value(savedMeal.getPlannedMealId())
                )
                .andExpect(
                        jsonPath("$.snackId")
                                .value(savedSnack.getSnackId())
                )
                .andExpect(jsonPath("$.quantity").value(30.0))
                .andExpect(jsonPath("$.unit").value("GRAM"));

        PlannedSnack savedPlannedSnack =
                plannedSnackRepository
                        .findAllByPlannedMealPlannedMealId(
                                savedMeal.getPlannedMealId()
                        )
                        .stream()
                        .findFirst()
                        .orElseThrow();

        assertThat(savedPlannedSnack.getPlannedSnackId())
                .isNotNull();

        assertThat(savedPlannedSnack.getSnack().getSnackId())
                .isEqualTo(savedSnack.getSnackId());

        assertThat(savedPlannedSnack.getQuantity())
                .isEqualTo(30.0);

        assertThat(savedPlannedSnack.getUnit())
                .isEqualTo(MeasurementUnit.GRAM);
    }
}
