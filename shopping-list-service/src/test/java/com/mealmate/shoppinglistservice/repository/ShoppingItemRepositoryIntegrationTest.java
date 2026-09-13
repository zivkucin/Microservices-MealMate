package com.mealmate.shoppinglistservice.repository;

import com.mealmate.shoppinglistservice.entity.MeasurementUnit;
import com.mealmate.shoppinglistservice.entity.ShoppingItem;
import com.mealmate.shoppinglistservice.entity.ShoppingList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
class ShoppingItemRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16")
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

    @Autowired
    private ShoppingItemRepository shoppingItemRepository;

    @Test
    void shouldSaveShoppingItemAndFindByListAndName() {
        ShoppingList shoppingList = new ShoppingList();

        shoppingList.setUserId(1L);
        shoppingList.setWeekStartDate(
                LocalDate.of(2026, 9, 14)
        );

        ShoppingList savedList =
                shoppingListRepository.save(shoppingList);

        ShoppingItem shoppingItem = new ShoppingItem();

        shoppingItem.setShoppingList(savedList);
        shoppingItem.setName("Rice");
        shoppingItem.setQuantity(500.0);
        shoppingItem.setUnit(MeasurementUnit.GRAM);
        shoppingItem.setPurchased(false);

        ShoppingItem savedItem =
                shoppingItemRepository.save(shoppingItem);

        assertNotNull(savedItem.getShoppingItemId());

        List<ShoppingItem> foundItems =
                shoppingItemRepository
                        .findAllByShoppingListShoppingListIdAndName(
                                savedList.getShoppingListId(),
                                "Rice"
                        );

        assertEquals(1, foundItems.size());

        ShoppingItem foundItem = foundItems.get(0);

        assertEquals("Rice", foundItem.getName());
        assertEquals(500.0, foundItem.getQuantity());
        assertEquals(MeasurementUnit.GRAM, foundItem.getUnit());
        assertFalse(foundItem.isPurchased());
        assertEquals(
                savedList.getShoppingListId(),
                foundItem.getShoppingList().getShoppingListId()
        );
    }
}