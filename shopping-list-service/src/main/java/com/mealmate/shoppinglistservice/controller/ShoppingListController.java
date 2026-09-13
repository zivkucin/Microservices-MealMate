package com.mealmate.shoppinglistservice.controller;

import com.mealmate.shoppinglistservice.dto.CreateShoppingListRequest;
import com.mealmate.shoppinglistservice.dto.ShoppingListResponse;
import com.mealmate.shoppinglistservice.service.ShoppingListService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shopping-lists")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @Operation(
            summary = "Create a shopping list",
            description = "Creates a new shopping list for a user and a specific week."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingListResponse createShoppingList(
            @Valid @RequestBody CreateShoppingListRequest request
    ) {
        return shoppingListService.createShoppingList(request);
    }

    @Operation(
            summary = "Get a shopping list",
            description = "Returns a shopping list together with all its shopping items."
    )
    @GetMapping("/{shoppingListId}")
    public ShoppingListResponse getShoppingListById(
            @PathVariable Long shoppingListId
    ) {
        return shoppingListService.getShoppingListById(shoppingListId);
    }

}