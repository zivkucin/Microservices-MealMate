package com.mealmate.shoppinglistservice.service;

import com.mealmate.shoppinglistservice.dto.AddShoppingItemRequest;
import com.mealmate.shoppinglistservice.dto.CreateShoppingListRequest;
import com.mealmate.shoppinglistservice.dto.ShoppingItemResponse;
import com.mealmate.shoppinglistservice.dto.ShoppingListResponse;
import com.mealmate.shoppinglistservice.entity.MeasurementUnit;
import com.mealmate.shoppinglistservice.exception.ShoppingItemNotFoundException;
import com.mealmate.shoppinglistservice.exception.ShoppingListNotFoundException;
import com.mealmate.shoppinglistservice.repository.ShoppingItemRepository;
import com.mealmate.shoppinglistservice.repository.ShoppingListRepository;
import org.junit.jupiter.api.BeforeEach;
import com.mealmate.shoppinglistservice.dto.UpdateShoppingItemRequest;
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
class ShoppingItemServiceIntegrationTest {

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
    void shouldAddShoppingItemSuccessfully() {

        ShoppingListResponse shoppingList =
                shoppingListService.createShoppingList(
                        new CreateShoppingListRequest(
                                1L,
                                LocalDate.of(2026, 9, 14)
                        )
                );

        AddShoppingItemRequest request =
                new AddShoppingItemRequest(
                        "Rice",
                        500.0,
                        MeasurementUnit.GRAM
                );

        ShoppingItemResponse response =
                shoppingItemService.addShoppingItem(
                        shoppingList.shoppingListId(),
                        request
                );

        assertNotNull(response.shoppingItemId());
        assertEquals("Rice", response.name());
        assertEquals(500.0, response.quantity());
        assertEquals(MeasurementUnit.GRAM, response.unit());
        assertFalse(response.purchased());

        assertEquals(1, shoppingItemRepository.count());
    }

    @Test
    void shouldMergeCompatibleUnits() {

        ShoppingListResponse shoppingList =
                shoppingListService.createShoppingList(
                        new CreateShoppingListRequest(
                                1L,
                                LocalDate.of(2026, 9, 14)
                        )
                );

        Long listId = shoppingList.shoppingListId();

        shoppingItemService.addShoppingItem(
                listId,
                new AddShoppingItemRequest(
                        "Rice",
                        500.0,
                        MeasurementUnit.GRAM
                )
        );

        ShoppingItemResponse response =
                shoppingItemService.addShoppingItem(
                        listId,
                        new AddShoppingItemRequest(
                                "Rice",
                                1.0,
                                MeasurementUnit.KILOGRAM
                        )
                );

        assertEquals("Rice", response.name());
        assertEquals(1.5, response.quantity());
        assertEquals(MeasurementUnit.KILOGRAM, response.unit());

        assertEquals(1, shoppingItemRepository.count());
    }

    @Test
    void shouldCreateSeparateItemForIncompatibleUnits() {

        ShoppingListResponse shoppingList =
                shoppingListService.createShoppingList(
                        new CreateShoppingListRequest(
                                1L,
                                LocalDate.of(2026, 9, 14)
                        )
                );

        Long listId = shoppingList.shoppingListId();

        shoppingItemService.addShoppingItem(
                listId,
                new AddShoppingItemRequest(
                        "Rice",
                        500.0,
                        MeasurementUnit.GRAM
                )
        );

        shoppingItemService.addShoppingItem(
                listId,
                new AddShoppingItemRequest(
                        "Rice",
                        2.0,
                        MeasurementUnit.PIECE
                )
        );

        assertEquals(2, shoppingItemRepository.count());
    }

    @Test
    void shouldThrowExceptionWhenShoppingListDoesNotExist() {

        AddShoppingItemRequest request =
                new AddShoppingItemRequest(
                        "Rice",
                        500.0,
                        MeasurementUnit.GRAM
                );

        assertThrows(
                ShoppingListNotFoundException.class,
                () -> shoppingItemService.addShoppingItem(
                        999L,
                        request
                )
        );

        assertEquals(0, shoppingItemRepository.count());
    }

    @Test
    void shouldDeleteShoppingItemSuccessfully() {

        ShoppingListResponse shoppingList =
                shoppingListService.createShoppingList(
                        new CreateShoppingListRequest(
                                1L,
                                LocalDate.of(2026, 9, 14)
                        )
                );

        Long listId = shoppingList.shoppingListId();

        ShoppingItemResponse item =
                shoppingItemService.addShoppingItem(
                        listId,
                        new AddShoppingItemRequest(
                                "Rice",
                                500.0,
                                MeasurementUnit.GRAM
                        )
                );

        shoppingItemService.deleteShoppingItem(
                listId,
                item.shoppingItemId()
        );

        assertEquals(0, shoppingItemRepository.count());

        assertThrows(
                ShoppingItemNotFoundException.class,
                () -> shoppingItemService.deleteShoppingItem(
                        listId,
                        item.shoppingItemId()
                )
        );
    }

    @Test
    void shouldUpdateShoppingItemSuccessfully() {

        ShoppingListResponse shoppingList =
                shoppingListService.createShoppingList(
                        new CreateShoppingListRequest(
                                1L,
                                LocalDate.of(2026, 9, 14)
                        )
                );

        Long listId = shoppingList.shoppingListId();

        ShoppingItemResponse item =
                shoppingItemService.addShoppingItem(
                        listId,
                        new AddShoppingItemRequest(
                                "Rice",
                                500.0,
                                MeasurementUnit.GRAM
                        )
                );

        UpdateShoppingItemRequest updateRequest =
                new UpdateShoppingItemRequest(
                        "Brown Rice",
                        750.0,
                        MeasurementUnit.GRAM
                );

        ShoppingItemResponse updated =
                shoppingItemService.updateShoppingItem(
                        listId,
                        item.shoppingItemId(),
                        updateRequest
                );

        assertEquals(item.shoppingItemId(), updated.shoppingItemId());
        assertEquals("Brown Rice", updated.name());
        assertEquals(750.0, updated.quantity());
        assertEquals(MeasurementUnit.GRAM, updated.unit());
    }

    @Test
    void shouldUpdatePurchaseStatusSuccessfully() {

        ShoppingListResponse shoppingList =
                shoppingListService.createShoppingList(
                        new CreateShoppingListRequest(
                                1L,
                                LocalDate.of(2026, 9, 14)
                        )
                );

        Long listId = shoppingList.shoppingListId();

        ShoppingItemResponse item =
                shoppingItemService.addShoppingItem(
                        listId,
                        new AddShoppingItemRequest(
                                "Rice",
                                500.0,
                                MeasurementUnit.GRAM
                        )
                );

        assertFalse(item.purchased());

        ShoppingItemResponse purchased =
                shoppingItemService.updatePurchaseStatus(
                        listId,
                        item.shoppingItemId(),
                        true
                );

        assertTrue(purchased.purchased());
        assertEquals(item.shoppingItemId(), purchased.shoppingItemId());
    }
}