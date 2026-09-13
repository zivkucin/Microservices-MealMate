package com.mealmate.shoppinglistservice.repository;

import com.mealmate.shoppinglistservice.entity.ShoppingList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
class ShoppingListRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("mealmate_test")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Test
    void shouldSaveAndFindShoppingList() {
        ShoppingList shoppingList = new ShoppingList();

        shoppingList.setUserId(1L);
        shoppingList.setWeekStartDate(
                LocalDate.of(2026, 9, 14)
        );

        ShoppingList saved =
                shoppingListRepository.save(shoppingList);

        assertNotNull(saved.getShoppingListId());

        var found =
                shoppingListRepository.findById(
                        saved.getShoppingListId()
                );

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getUserId());
        assertEquals(
                LocalDate.of(2026, 9, 14),
                found.get().getWeekStartDate()
        );
    }
}