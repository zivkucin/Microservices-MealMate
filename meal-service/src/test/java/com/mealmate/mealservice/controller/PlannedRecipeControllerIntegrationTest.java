package com.mealmate.mealservice.controller;

import com.mealmate.mealservice.entity.PlannedMeal;
import com.mealmate.mealservice.entity.PlannedRecipe;
import com.mealmate.mealservice.entity.WeeklyMealPlan;
import com.mealmate.mealservice.repository.PlannedMealRepository;
import com.mealmate.mealservice.repository.PlannedRecipeRepository;
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
class PlannedRecipeControllerIntegrationTest {

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
    private PlannedRecipeRepository plannedRecipeRepository;

    @Test
    void shouldAddRecipeToMeal() throws Exception {

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

        String requestBody = """
                {
                    "recipeId": 1,
                    "quantity": 2
                }
                """;

        mockMvc.perform(
                        post(
                                "/meal-plans/{weeklyMealPlanId}/meals/{plannedMealId}/recipe",
                                savedPlan.getWeeklyMealPlanId(),
                                savedMeal.getPlannedMealId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plannedRecipeId").isNumber())
                .andExpect(
                        jsonPath("$.plannedMealId")
                                .value(savedMeal.getPlannedMealId())
                )
                .andExpect(jsonPath("$.recipeId").value(1))
                .andExpect(jsonPath("$.quantity").value(2));

        PlannedRecipe savedRecipe =
                plannedRecipeRepository
                        .findByPlannedMealPlannedMealId(
                                savedMeal.getPlannedMealId()
                        )
                        .orElseThrow();

        assertThat(savedRecipe.getPlannedRecipeId())
                .isNotNull();

        assertThat(savedRecipe.getRecipeId())
                .isEqualTo(1L);

        assertThat(savedRecipe.getQuantity())
                .isEqualTo(2);
    }
}