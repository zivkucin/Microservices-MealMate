package com.mealmate.shoppinglistservice.service;

import com.mealmate.shoppinglistservice.dto.AddShoppingItemRequest;
import com.mealmate.shoppinglistservice.dto.ShoppingItemResponse;
import com.mealmate.shoppinglistservice.dto.UpdateShoppingItemRequest;
import com.mealmate.shoppinglistservice.entity.MeasurementUnit;
import com.mealmate.shoppinglistservice.entity.ShoppingItem;
import com.mealmate.shoppinglistservice.entity.ShoppingList;
import com.mealmate.shoppinglistservice.exception.ShoppingItemNotFoundException;
import com.mealmate.shoppinglistservice.exception.ShoppingListNotFoundException;
import com.mealmate.shoppinglistservice.mapper.ShoppingItemMapper;
import com.mealmate.shoppinglistservice.repository.ShoppingItemRepository;
import com.mealmate.shoppinglistservice.repository.ShoppingListRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingItemServiceTest {

    @Mock
    private ShoppingItemRepository shoppingItemRepository;

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @Mock
    private ShoppingItemMapper shoppingItemMapper;

    @InjectMocks
    private ShoppingItemService shoppingItemService;

    @Test
    void shouldAddNewShoppingItem() {

        Long shoppingListId = 1L;

        ShoppingList shoppingList = createShoppingList();

        AddShoppingItemRequest request =
                new AddShoppingItemRequest(
                        "Rice",
                        500.0,
                        MeasurementUnit.GRAM
                );

        ShoppingItem item = new ShoppingItem();

        ShoppingItemResponse response =
                createResponse(1L, "Rice", 500.0, MeasurementUnit.GRAM, false);

        when(shoppingListRepository.findById(shoppingListId))
                .thenReturn(Optional.of(shoppingList));

        when(shoppingItemRepository
                .findAllByShoppingListShoppingListIdAndName(
                        shoppingListId,
                        "Rice"
                ))
                .thenReturn(List.of());

        when(shoppingItemMapper.toEntity(request))
                .thenReturn(item);

        when(shoppingItemRepository.save(item))
                .thenReturn(item);

        when(shoppingItemMapper.toResponse(item))
                .thenReturn(response);

        ShoppingItemResponse result =
                shoppingItemService.addShoppingItem(
                        shoppingListId,
                        request
                );

        assertEquals(500.0, result.quantity());
        assertEquals(MeasurementUnit.GRAM, result.unit());

        verify(shoppingItemMapper).toEntity(request);
        verify(shoppingItemRepository).save(item);

        assertSame(shoppingList, item.getShoppingList());
    }

    @Test
    void shouldMergeGramAndGram() {

        Long shoppingListId = 1L;
        ShoppingList shoppingList = createShoppingList();

        ShoppingItem existingItem =
                createItem("Rice", 500.0, MeasurementUnit.GRAM, shoppingList);

        AddShoppingItemRequest request =
                new AddShoppingItemRequest(
                        "Rice",
                        300.0,
                        MeasurementUnit.GRAM
                );

        ShoppingItemResponse response =
                createResponse(1L, "Rice", 800.0, MeasurementUnit.GRAM, false);

        when(shoppingListRepository.findById(shoppingListId))
                .thenReturn(Optional.of(shoppingList));

        when(shoppingItemRepository
                .findAllByShoppingListShoppingListIdAndName(
                        shoppingListId,
                        "Rice"
                ))
                .thenReturn(List.of(existingItem));

        when(shoppingItemRepository.save(existingItem))
                .thenReturn(existingItem);

        when(shoppingItemMapper.toResponse(existingItem))
                .thenReturn(response);

        ShoppingItemResponse result =
                shoppingItemService.addShoppingItem(
                        shoppingListId,
                        request
                );

        assertEquals(800.0, result.quantity());
        assertEquals(MeasurementUnit.GRAM, result.unit());
        assertFalse(result.purchased());

        verify(shoppingItemRepository).save(existingItem);
        verify(shoppingItemMapper, never()).toEntity(any());
    }

    @Test
    void shouldMergeGramAndKilogram() {

        Long shoppingListId = 1L;
        ShoppingList shoppingList = createShoppingList();

        ShoppingItem existingItem =
                createItem("Rice", 750.0, MeasurementUnit.GRAM, shoppingList);

        AddShoppingItemRequest request =
                new AddShoppingItemRequest(
                        "Rice",
                        1.0,
                        MeasurementUnit.KILOGRAM
                );

        ShoppingItemResponse response =
                createResponse(1L, "Rice", 1.75, MeasurementUnit.KILOGRAM, false);

        when(shoppingListRepository.findById(shoppingListId))
                .thenReturn(Optional.of(shoppingList));

        when(shoppingItemRepository
                .findAllByShoppingListShoppingListIdAndName(
                        shoppingListId,
                        "Rice"
                ))
                .thenReturn(List.of(existingItem));

        when(shoppingItemRepository.save(existingItem))
                .thenReturn(existingItem);

        when(shoppingItemMapper.toResponse(existingItem))
                .thenReturn(response);

        ShoppingItemResponse result =
                shoppingItemService.addShoppingItem(
                        shoppingListId,
                        request
                );

        assertEquals(1.75, result.quantity());
        assertEquals(MeasurementUnit.KILOGRAM, result.unit());
        assertFalse(result.purchased());
    }

    @Test
    void shouldMergeMilliliterAndLiter() {

        Long shoppingListId = 1L;
        ShoppingList shoppingList = createShoppingList();

        ShoppingItem existingItem =
                createItem("Milk", 500.0, MeasurementUnit.MILLILITER, shoppingList);

        AddShoppingItemRequest request =
                new AddShoppingItemRequest(
                        "Milk",
                        1.0,
                        MeasurementUnit.LITER
                );

        ShoppingItemResponse response =
                createResponse(1L, "Milk", 1.5, MeasurementUnit.LITER, false);

        when(shoppingListRepository.findById(shoppingListId))
                .thenReturn(Optional.of(shoppingList));

        when(shoppingItemRepository
                .findAllByShoppingListShoppingListIdAndName(
                        shoppingListId,
                        "Milk"
                ))
                .thenReturn(List.of(existingItem));

        when(shoppingItemRepository.save(existingItem))
                .thenReturn(existingItem);

        when(shoppingItemMapper.toResponse(existingItem))
                .thenReturn(response);

        ShoppingItemResponse result =
                shoppingItemService.addShoppingItem(
                        shoppingListId,
                        request
                );

        assertEquals(1.5, result.quantity());
        assertEquals(MeasurementUnit.LITER, result.unit());
    }

    @Test
    void shouldMergePieces() {

        Long shoppingListId = 1L;
        ShoppingList shoppingList = createShoppingList();

        ShoppingItem existingItem =
                createItem("Egg", 4.0, MeasurementUnit.PIECE, shoppingList);

        AddShoppingItemRequest request =
                new AddShoppingItemRequest(
                        "Egg",
                        6.0,
                        MeasurementUnit.PIECE
                );

        ShoppingItemResponse response =
                createResponse(1L, "Egg", 10.0, MeasurementUnit.PIECE, false);

        when(shoppingListRepository.findById(shoppingListId))
                .thenReturn(Optional.of(shoppingList));

        when(shoppingItemRepository
                .findAllByShoppingListShoppingListIdAndName(
                        shoppingListId,
                        "Egg"
                ))
                .thenReturn(List.of(existingItem));

        when(shoppingItemRepository.save(existingItem))
                .thenReturn(existingItem);

        when(shoppingItemMapper.toResponse(existingItem))
                .thenReturn(response);

        ShoppingItemResponse result =
                shoppingItemService.addShoppingItem(
                        shoppingListId,
                        request
                );

        assertEquals(10.0, result.quantity());
        assertEquals(MeasurementUnit.PIECE, result.unit());
    }

    @Test
    void shouldCreateNewItemWhenUnitsAreNotCompatible() {

        Long shoppingListId = 1L;
        ShoppingList shoppingList = createShoppingList();

        ShoppingItem existingItem =
                createItem("Brown Rice", 1.75, MeasurementUnit.KILOGRAM, shoppingList);

        AddShoppingItemRequest request =
                new AddShoppingItemRequest(
                        "Brown Rice",
                        2.0,
                        MeasurementUnit.PIECE
                );

        ShoppingItem newItem = new ShoppingItem();

        ShoppingItemResponse response =
                createResponse(2L, "Brown Rice", 2.0, MeasurementUnit.PIECE, false);

        when(shoppingListRepository.findById(shoppingListId))
                .thenReturn(Optional.of(shoppingList));

        when(shoppingItemRepository
                .findAllByShoppingListShoppingListIdAndName(
                        shoppingListId,
                        "Brown Rice"
                ))
                .thenReturn(List.of(existingItem));

        when(shoppingItemMapper.toEntity(request))
                .thenReturn(newItem);

        when(shoppingItemRepository.save(newItem))
                .thenReturn(newItem);

        when(shoppingItemMapper.toResponse(newItem))
                .thenReturn(response);

        ShoppingItemResponse result =
                shoppingItemService.addShoppingItem(
                        shoppingListId,
                        request
                );

        assertEquals(2.0, result.quantity());
        assertEquals(MeasurementUnit.PIECE, result.unit());

        verify(shoppingItemMapper).toEntity(request);
        verify(shoppingItemRepository).save(newItem);
    }

    @Test
    void shouldThrowExceptionWhenShoppingListDoesNotExist() {

        Long shoppingListId = 999L;

        AddShoppingItemRequest request =
                new AddShoppingItemRequest(
                        "Rice",
                        500.0,
                        MeasurementUnit.GRAM
                );

        when(shoppingListRepository.findById(shoppingListId))
                .thenReturn(Optional.empty());

        assertThrows(
                ShoppingListNotFoundException.class,
                () -> shoppingItemService.addShoppingItem(
                        shoppingListId,
                        request
                )
        );

        verify(shoppingItemRepository, never())
                .save(any());

        verify(shoppingItemMapper, never())
                .toEntity(any());
    }

    @Test
    void shouldUpdateShoppingItem() {

        Long shoppingListId = 1L;
        Long shoppingItemId = 1L;

        ShoppingList shoppingList = createShoppingList();

        ShoppingItem item =
                createItem("Rice", 500.0, MeasurementUnit.GRAM, shoppingList);

        UpdateShoppingItemRequest request =
                new UpdateShoppingItemRequest(
                        "Brown Rice",
                        750.0,
                        MeasurementUnit.GRAM
                );

        ShoppingItemResponse response =
                createResponse(
                        shoppingItemId,
                        "Brown Rice",
                        750.0,
                        MeasurementUnit.GRAM,
                        false
                );

        when(shoppingListRepository.findById(shoppingListId))
                .thenReturn(Optional.of(shoppingList));

        when(shoppingItemRepository.findById(shoppingItemId))
                .thenReturn(Optional.of(item));

        when(shoppingItemRepository.save(item))
                .thenReturn(item);

        when(shoppingItemMapper.toResponse(item))
                .thenReturn(response);

        ShoppingItemResponse result =
                shoppingItemService.updateShoppingItem(
                        shoppingListId,
                        shoppingItemId,
                        request
                );

        assertEquals("Brown Rice", result.name());
        assertEquals(750.0, result.quantity());

        verify(shoppingItemMapper).updateEntity(request, item);
        verify(shoppingItemRepository).save(item);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingItemFromDifferentShoppingList() {

        Long shoppingListId = 1L;
        Long otherShoppingListId = 2L;
        Long shoppingItemId = 1L;

        ShoppingList requestedList = createShoppingList();

        ShoppingList otherList = createShoppingList();
        otherList.setShoppingListId(otherShoppingListId);

        ShoppingItem item =
                createItem("Rice", 500.0, MeasurementUnit.GRAM, otherList);

        UpdateShoppingItemRequest request =
                new UpdateShoppingItemRequest(
                        "Brown Rice",
                        750.0,
                        MeasurementUnit.GRAM
                );

        when(shoppingListRepository.findById(shoppingListId))
                .thenReturn(Optional.of(requestedList));

        when(shoppingItemRepository.findById(shoppingItemId))
                .thenReturn(Optional.of(item));

        assertThrows(
                ShoppingItemNotFoundException.class,
                () -> shoppingItemService.updateShoppingItem(
                        shoppingListId,
                        shoppingItemId,
                        request
                )
        );

        verify(shoppingItemMapper, never())
                .updateEntity(any(), any());

        verify(shoppingItemRepository, never())
                .save(any());
    }

    @Test
    void shouldDeleteShoppingItem() {

        Long shoppingListId = 1L;
        Long shoppingItemId = 1L;

        ShoppingList shoppingList = createShoppingList();

        ShoppingItem item =
                createItem("Rice", 500.0, MeasurementUnit.GRAM, shoppingList);

        when(shoppingItemRepository.findById(shoppingItemId))
                .thenReturn(Optional.of(item));

        shoppingItemService.deleteShoppingItem(
                shoppingListId,
                shoppingItemId
        );

        verify(shoppingItemRepository).delete(item);
    }

    @Test
    void shouldUpdatePurchaseStatus() {

        Long shoppingListId = 1L;
        Long shoppingItemId = 1L;

        ShoppingList shoppingList = createShoppingList();

        ShoppingItem item =
                createItem("Rice", 500.0, MeasurementUnit.GRAM, shoppingList);

        ShoppingItemResponse response =
                createResponse(
                        shoppingItemId,
                        "Rice",
                        500.0,
                        MeasurementUnit.GRAM,
                        true
                );

        when(shoppingItemRepository.findById(shoppingItemId))
                .thenReturn(Optional.of(item));

        when(shoppingItemRepository.save(item))
                .thenReturn(item);

        when(shoppingItemMapper.toResponse(item))
                .thenReturn(response);

        ShoppingItemResponse result =
                shoppingItemService.updatePurchaseStatus(
                        shoppingListId,
                        shoppingItemId,
                        true
                );

        assertTrue(result.purchased());
        assertTrue(item.isPurchased());

        verify(shoppingItemRepository).save(item);
    }

    private ShoppingList createShoppingList() {

        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setShoppingListId(1L);
        shoppingList.setUserId(1L);
        shoppingList.setWeekStartDate(
                LocalDate.of(2026, 9, 14)
        );

        return shoppingList;
    }

    private ShoppingItem createItem(
            String name,
            double quantity,
            MeasurementUnit unit,
            ShoppingList shoppingList
    ) {

        ShoppingItem item = new ShoppingItem();
        item.setShoppingItemId(1L);
        item.setShoppingList(shoppingList);
        item.setName(name);
        item.setQuantity(quantity);
        item.setUnit(unit);

        return item;
    }

    private ShoppingItemResponse createResponse(
            Long shoppingItemId,
            String name,
            double quantity,
            MeasurementUnit unit,
            boolean purchased
    ) {

        return new ShoppingItemResponse(
                shoppingItemId,
                1L,
                name,
                quantity,
                unit,
                purchased,
                null,
                null
        );
    }
}