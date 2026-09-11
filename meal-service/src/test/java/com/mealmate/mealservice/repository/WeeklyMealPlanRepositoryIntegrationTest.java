package com.mealmate.mealservice.repository;

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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class WeeklyMealPlanRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
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

    @Test
    void shouldSaveAndFindWeeklyMealPlanByUserAndWeek() {

        WeeklyMealPlan plan = new WeeklyMealPlan();
        plan.setUserId(1L);
        plan.setWeekStartDate(
                LocalDate.of(2026, 9, 14)
        );

        WeeklyMealPlan saved =
                weeklyMealPlanRepository.save(plan);

        Optional<WeeklyMealPlan> found =
                weeklyMealPlanRepository
                        .findByUserIdAndWeekStartDate(
                                1L,
                                LocalDate.of(2026, 9, 14)
                        );

        assertThat(saved.getWeeklyMealPlanId())
                .isNotNull();

        assertThat(found)
                .isPresent();

        assertThat(found.get().getUserId())
                .isEqualTo(1L);

        assertThat(found.get().getWeekStartDate())
                .isEqualTo(
                        LocalDate.of(2026, 9, 14)
                );
    }
}