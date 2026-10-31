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
import java.util.List;
import java.util.Optional;

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

        Long userId = 1L;
        LocalDate weekStartDate = LocalDate.of(2026, 9, 14);

        CreateShoppingListRequest request =
                new CreateShoppingListRequest(weekStartDate);

        ShoppingList shoppingList = new ShoppingList();

        ShoppingList savedShoppingList = new ShoppingList();
        savedShoppingList.setShoppingListId(1L);
        savedShoppingList.setUserId(userId);
        savedShoppingList.setWeekStartDate(weekStartDate);
        savedShoppingList.setCreatedAt(LocalDateTime.now());
        savedShoppingList.setUpdatedAt(LocalDateTime.now());

        ShoppingListResponse response =
                new ShoppingListResponse(
                        1L,
                        userId,
                        weekStartDate,
                        savedShoppingList.getCreatedAt(),
                        savedShoppingList.getUpdatedAt(),
                        List.of()
                );

        when(shoppingListRepository.existsByUserIdAndWeekStartDate(
                userId,
                weekStartDate
        )).thenReturn(false);

        when(shoppingListMapper.toEntity(request))
                .thenReturn(shoppingList);

        when(shoppingListRepository.save(shoppingList))
                .thenReturn(savedShoppingList);

        when(shoppingListMapper.toResponse(savedShoppingList))
                .thenReturn(response);

        ShoppingListResponse result =
                shoppingListService.createShoppingList(
                        userId,
                        request
                );

        assertNotNull(result);
        assertEquals(1L, result.shoppingListId());
        assertEquals(userId, result.userId());
        assertEquals(weekStartDate, result.weekStartDate());

        // userId se preuzima iz autentifikovanog korisnika
        assertEquals(userId, shoppingList.getUserId());

        verify(shoppingListRepository)
                .existsByUserIdAndWeekStartDate(userId, weekStartDate);

        verify(shoppingListMapper).toEntity(request);
        verify(shoppingListRepository).save(shoppingList);
        verify(shoppingListMapper).toResponse(savedShoppingList);
    }

    @Test
    void shouldThrowExceptionWhenShoppingListAlreadyExists() {

        Long userId = 1L;
        LocalDate weekStartDate = LocalDate.of(2026, 9, 14);

        CreateShoppingListRequest request =
                new CreateShoppingListRequest(weekStartDate);

        when(shoppingListRepository.existsByUserIdAndWeekStartDate(
                userId,
                weekStartDate
        )).thenReturn(true);

        assertThrows(
                ShoppingListAlreadyExistsException.class,
                () -> shoppingListService.createShoppingList(
                        userId,
                        request
                )
        );

        verify(shoppingListRepository)
                .existsByUserIdAndWeekStartDate(userId, weekStartDate);

        verify(shoppingListRepository, never()).save(any());
        verify(shoppingListMapper, never()).toEntity(any());
    }

    @Test
    void shouldReturnShoppingListSuccessfully() {

        Long userId = 1L;
        Long shoppingListId = 1L;
        LocalDate weekStartDate = LocalDate.of(2026, 9, 14);

        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setShoppingListId(shoppingListId);
        shoppingList.setUserId(userId);
        shoppingList.setWeekStartDate(weekStartDate);

        ShoppingListResponse response =
                new ShoppingListResponse(
                        shoppingListId,
                        userId,
                        weekStartDate,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        List.of()
                );

        when(shoppingListRepository.findById(shoppingListId))
                .thenReturn(Optional.of(shoppingList));

        when(shoppingListMapper.toResponse(shoppingList))
                .thenReturn(response);

        ShoppingListResponse result =
                shoppingListService.getShoppingListById(
                        userId,
                        shoppingListId
                );

        assertNotNull(result);
        assertEquals(shoppingListId, result.shoppingListId());
        assertEquals(userId, result.userId());
        assertEquals(weekStartDate, result.weekStartDate());

        verify(shoppingListRepository).findById(shoppingListId);
        verify(shoppingListMapper).toResponse(shoppingList);
    }

    @Test
    void shouldRejectAccessToAnotherUsersShoppingList() {

        Long ownerId = 1L;
        Long anotherUserId = 3L;
        Long shoppingListId = 1L;

        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setShoppingListId(shoppingListId);
        shoppingList.setUserId(ownerId);
        shoppingList.setWeekStartDate(
                LocalDate.of(2026, 9, 14)
        );

        when(shoppingListRepository.findById(shoppingListId))
                .thenReturn(Optional.of(shoppingList));

        assertThrows(
                ShoppingListNotFoundException.class,
                () -> shoppingListService.getShoppingListById(
                        anotherUserId,
                        shoppingListId
                )
        );

        verify(shoppingListRepository).findById(shoppingListId);
        verify(shoppingListMapper, never()).toResponse(any());
    }

    @Test
    void shouldThrowExceptionWhenShoppingListDoesNotExist() {

        Long userId = 1L;
        Long shoppingListId = 999L;

        when(shoppingListRepository.findById(shoppingListId))
                .thenReturn(Optional.empty());

        assertThrows(
                ShoppingListNotFoundException.class,
                () -> shoppingListService.getShoppingListById(
                        userId,
                        shoppingListId
                )
        );

        verify(shoppingListRepository).findById(shoppingListId);
        verify(shoppingListMapper, never()).toResponse(any());
    }
}