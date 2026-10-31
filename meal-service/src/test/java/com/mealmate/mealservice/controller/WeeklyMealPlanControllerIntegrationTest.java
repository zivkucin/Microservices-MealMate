package com.mealmate.mealservice.controller;

import com.mealmate.mealservice.entity.WeeklyMealPlan;
import com.mealmate.mealservice.repository.WeeklyMealPlanRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class WeeklyMealPlanControllerIntegrationTest {

    private static final String JWT_SECRET =
            "my-super-secret-key-that-is-at-least-32-characters-long";

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

        registry.add(
                "jwt.secret",
                () -> JWT_SECRET
        );
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WeeklyMealPlanRepository weeklyMealPlanRepository;

    private String createUserToken(Long userId) {

        SecretKey secretKey =
                Keys.hmacShaKeyFor(
                        JWT_SECRET.getBytes(StandardCharsets.UTF_8)
                );

        Date now = new Date();

        Date expiration =
                new Date(
                        now.getTime() + 60 * 60 * 1000
                );

        return Jwts.builder()
                .subject("testuser")
                .claim("userId", userId)
                .claim("role", "USER")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    @Test
    void shouldCreateWeeklyMealPlan() throws Exception {

        String requestBody = """
                {
                    "weekStartDate": "2026-09-14"
                }
                """;

        String token =
                createUserToken(1L);

        mockMvc.perform(
                        post("/meal-plans")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.weeklyMealPlanId")
                                .isNumber()
                )
                .andExpect(
                        jsonPath("$.userId")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.weekStartDate")
                                .value("2026-09-14")
                )
                .andExpect(
                        jsonPath("$.plannedMeals")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.plannedMeals.length()")
                                .value(35)
                );

        WeeklyMealPlan savedPlan =
                weeklyMealPlanRepository
                        .findByUserIdAndWeekStartDate(
                                1L,
                                LocalDate.of(
                                        2026,
                                        9,
                                        14
                                )
                        )
                        .orElseThrow();

        assertThat(
                savedPlan.getWeeklyMealPlanId()
        )
                .isNotNull();
    }

    @Test
    void shouldRejectUnauthorizedRequest() throws Exception {

        String requestBody = """
                {
                    "weekStartDate": "2026-09-21"
                }
                """;

        mockMvc.perform(
                        post("/meal-plans")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(status().isForbidden());
    }
}