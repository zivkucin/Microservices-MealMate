package com.mealmate.mealservice.controller;

import com.mealmate.mealservice.entity.WeeklyMealPlan;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class WeeklyMealPlanControllerIntegrationTest {

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

    @Test
    void shouldCreateWeeklyMealPlan() throws Exception {

        String requestBody = """
                {
                    "userId": 1,
                    "weekStartDate": "2026-09-14"
                }
                """;

        mockMvc.perform(
                        post("/meal-plans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.weeklyMealPlanId").isNumber())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.weekStartDate").value("2026-09-14"))
                .andExpect(jsonPath("$.plannedMeals").isArray())
                .andExpect(jsonPath("$.plannedMeals.length()").value(35));

        WeeklyMealPlan savedPlan =
                weeklyMealPlanRepository
                        .findByUserIdAndWeekStartDate(
                                1L,
                                java.time.LocalDate.of(2026, 9, 14)
                        )
                        .orElseThrow();

        org.assertj.core.api.Assertions.assertThat(
                savedPlan.getWeeklyMealPlanId()
        ).isNotNull();
    }
}