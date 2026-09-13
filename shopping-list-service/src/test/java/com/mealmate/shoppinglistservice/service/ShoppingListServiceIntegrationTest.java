package com.mealmate.shoppinglistservice.service;

import com.mealmate.shoppinglistservice.dto.AddShoppingItemRequest;
import com.mealmate.shoppinglistservice.dto.CreateShoppingListRequest;
import com.mealmate.shoppinglistservice.dto.ShoppingItemResponse;
import com.mealmate.shoppinglistservice.dto.ShoppingListResponse;
import com.mealmate.shoppinglistservice.entity.MeasurementUnit;
import com.mealmate.shoppinglistservice.exception.ShoppingListAlreadyExistsException;
import com.mealmate.shoppinglistservice.repository.ShoppingItemRepository;
import com.mealmate.shoppinglistservice.repository.ShoppingListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class ShoppingListServiceIntegrationTest {

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
    private ShoppingListService shoppingListService;

    @Autowired
    private ShoppingItemService shoppingItemService;

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private ShoppingItemRepository shoppingItemRepository;

    @BeforeEach
    void cleanDatabase() {
        shoppingItemRepository.deleteAll();
        shoppingListRepository.deleteAll();
    }

    @Test
    void shouldCreateAndRetrieveShoppingList() {
        CreateShoppingListRequest request =
                new CreateShoppingListRequest(
                        1L,
                        LocalDate.of(2026, 9, 14)
                );

        ShoppingListResponse created =
                shoppingListService.createShoppingList(request);

        assertNotNull(created.shoppingListId());
        assertEquals(1L, created.userId());
        assertEquals(
                LocalDate.of(2026, 9, 14),
                created.weekStartDate()
        );

        ShoppingListResponse found =
                shoppingListService.getShoppingListById(
                        created.shoppingListId()
                );

        assertEquals(
                created.shoppingListId(),
                found.shoppingListId()
        );
        assertEquals(1L, found.userId());
        assertEquals(
                LocalDate.of(2026, 9, 14),
                found.weekStartDate()
        );
    }

    @Test
    void shouldNotCreateDuplicateShoppingList() {
        CreateShoppingListRequest request =
                new CreateShoppingListRequest(
                        1L,
                        LocalDate.of(2026, 9, 14)
                );

        shoppingListService.createShoppingList(request);

        assertThrows(
                ShoppingListAlreadyExistsException.class,
                () -> shoppingListService.createShoppingList(request)
        );
    }

    @Test
    void shouldAddAndMergeShoppingItems() {
        CreateShoppingListRequest listRequest =
                new CreateShoppingListRequest(
                        1L,
                        LocalDate.of(2026, 9, 14)
                );

        ShoppingListResponse shoppingList =
                shoppingListService.createShoppingList(listRequest);

        Long listId = shoppingList.shoppingListId();

        AddShoppingItemRequest firstItem =
                new AddShoppingItemRequest(
                        "Rice",
                        500.0,
                        MeasurementUnit.GRAM
                );

        ShoppingItemResponse firstResponse =
                shoppingItemService.addShoppingItem(
                        listId,
                        firstItem
                );

        assertEquals("Rice", firstResponse.name());
        assertEquals(500.0, firstResponse.quantity());
        assertEquals(
                MeasurementUnit.GRAM,
                firstResponse.unit()
        );

        AddShoppingItemRequest secondItem =
                new AddShoppingItemRequest(
                        "Rice",
                        1.0,
                        MeasurementUnit.KILOGRAM
                );

        ShoppingItemResponse mergedResponse =
                shoppingItemService.addShoppingItem(
                        listId,
                        secondItem
                );

        assertEquals("Rice", mergedResponse.name());
        assertEquals(1.5, mergedResponse.quantity());
        assertEquals(
                MeasurementUnit.KILOGRAM,
                mergedResponse.unit()
        );

        assertEquals(
                1,
                shoppingItemRepository.count()
        );
    }
}