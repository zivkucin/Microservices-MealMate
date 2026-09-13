package com.mealmate.shoppinglistservice.service;

import com.mealmate.shoppinglistservice.dto.CreateShoppingListRequest;
import com.mealmate.shoppinglistservice.dto.ShoppingListResponse;
import com.mealmate.shoppinglistservice.mapper.ShoppingListMapper;
import com.mealmate.shoppinglistservice.repository.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import com.mealmate.shoppinglistservice.exception.ShoppingListAlreadyExistsException;
import org.springframework.stereotype.Service;
import com.mealmate.shoppinglistservice.exception.ShoppingListNotFoundException;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListMapper shoppingListMapper;

    public ShoppingListResponse createShoppingList(CreateShoppingListRequest request) {

        if (shoppingListRepository.existsByUserIdAndWeekStartDate(
                request.userId(),
                request.weekStartDate()
        )) {
            throw new ShoppingListAlreadyExistsException(
                    "Shopping list already exists for this user and week"
            );
        }

        var shoppingList = shoppingListMapper.toEntity(request);

        var savedShoppingList = shoppingListRepository.save(shoppingList);

        return shoppingListMapper.toResponse(savedShoppingList);
    }

    @Transactional(readOnly = true)
    public ShoppingListResponse getShoppingListById(Long shoppingListId) {

        var shoppingList = shoppingListRepository.findById(shoppingListId)
                .orElseThrow(() -> new ShoppingListNotFoundException(
                        "Shopping list not found with id: " + shoppingListId
                ));

        return shoppingListMapper.toResponse(shoppingList);
    }

}