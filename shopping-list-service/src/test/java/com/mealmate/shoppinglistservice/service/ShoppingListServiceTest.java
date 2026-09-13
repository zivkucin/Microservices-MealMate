package com.mealmate.shoppinglistservice.service;

import com.mealmate.shoppinglistservice.dto.CreateShoppingListRequest;
import com.mealmate.shoppinglistservice.dto.ShoppingListResponse;
import com.mealmate.shoppinglistservice.entity.ShoppingList;
import com.mealmate.shoppinglistservice.exception.ShoppingListAlreadyExistsException;
import com.mealmate.shoppinglistservice.exception.ShoppingListNotFoundException;
import com.mealmate.shoppinglistservice.mapper.ShoppingListMapper;
import com.mealmate.shoppinglistservice.repository.ShoppingListRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @Mock
    private ShoppingListMapper shoppingListMapper;

    @InjectMocks
    private ShoppingListService shoppingListService;

    @Test
    void shouldCreateShoppingListSuccessfully() {

        CreateShoppingListRequest request =
                new CreateShoppingListRequest(
                        1L,
                        LocalDate.of(2026, 9, 14)
                );

        ShoppingList shoppingList = new ShoppingList();

        ShoppingList savedShoppingList = new ShoppingList();
        savedShoppingList.setShoppingListId(1L);
        savedShoppingList.setUserId(1L);
        savedShoppingList.setWeekStartDate(
                LocalDate.of(2026, 9, 14)
        );
        savedShoppingList.setCreatedAt(LocalDateTime.now());
        savedShoppingList.setUpdatedAt(LocalDateTime.now());

        ShoppingListResponse response =
                new ShoppingListResponse(
                        1L,
                        1L,
                        LocalDate.of(2026, 9, 14),
                        savedShoppingList.getCreatedAt(),
                        savedShoppingList.getUpdatedAt(),
                        java.util.List.of()
                );

        when(shoppingListRepository.existsByUserIdAndWeekStartDate(
                request.userId(),
                request.weekStartDate()
        )).thenReturn(false);

        when(shoppingListMapper.toEntity(request))
                .thenReturn(shoppingList);

        when(shoppingListRepository.save(shoppingList))
                .thenReturn(savedShoppingList);

        when(shoppingListMapper.toResponse(savedShoppingList))
                .thenReturn(response);

        ShoppingListResponse result =
                shoppingListService.createShoppingList(request);

        assertNotNull(result);
        assertEquals(1L, result.shoppingListId());
        assertEquals(1L, result.userId());
        assertEquals(
                LocalDate.of(2026, 9, 14),
                result.weekStartDate()
        );

        verify(shoppingListRepository)
                .existsByUserIdAndWeekStartDate(
                        request.userId(),
                        request.weekStartDate()
                );

        verify(shoppingListRepository)
                .save(shoppingList);

        verify(shoppingListMapper)
                .toEntity(request);

        verify(shoppingListMapper)
                .toResponse(savedShoppingList);
    }

    @Test
    void shouldThrowExceptionWhenShoppingListAlreadyExists() {

        CreateShoppingListRequest request =
                new CreateShoppingListRequest(
                        1L,
                        LocalDate.of(2026, 9, 14)
                );

        when(shoppingListRepository.existsByUserIdAndWeekStartDate(
                request.userId(),
                request.weekStartDate()
        )).thenReturn(true);

        assertThrows(
                ShoppingListAlreadyExistsException.class,
                () -> shoppingListService.createShoppingList(request)
        );

        verify(shoppingListRepository)
                .existsByUserIdAndWeekStartDate(
                        request.userId(),
                        request.weekStartDate()
                );

        verify(shoppingListRepository, never())
                .save(any());

        verify(shoppingListMapper, never())
                .toEntity(any());
    }

    @Test
    void shouldReturnShoppingListSuccessfully() {

        Long shoppingListId = 1L;

        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setShoppingListId(shoppingListId);
        shoppingList.setUserId(1L);
        shoppingList.setWeekStartDate(
                LocalDate.of(2026, 9, 14)
        );

        ShoppingListResponse response =
                new ShoppingListResponse(
                        shoppingListId,
                        1L,
                        LocalDate.of(2026, 9, 14),
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        java.util.List.of()
                );

        when(shoppingListRepository.findById(shoppingListId))
                .thenReturn(java.util.Optional.of(shoppingList));

        when(shoppingListMapper.toResponse(shoppingList))
                .thenReturn(response);

        ShoppingListResponse result =
                shoppingListService.getShoppingListById(shoppingListId);

        assertNotNull(result);
        assertEquals(shoppingListId, result.shoppingListId());
        assertEquals(1L, result.userId());

        verify(shoppingListRepository)
                .findById(shoppingListId);

        verify(shoppingListMapper)
                .toResponse(shoppingList);
    }

    @Test
    void shouldThrowExceptionWhenShoppingListDoesNotExist() {

        Long shoppingListId = 999L;

        when(shoppingListRepository.findById(shoppingListId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                ShoppingListNotFoundException.class,
                () -> shoppingListService.getShoppingListById(shoppingListId)
        );

        verify(shoppingListRepository)
                .findById(shoppingListId);

        verify(shoppingListMapper, never())
                .toResponse(any());
    }
}