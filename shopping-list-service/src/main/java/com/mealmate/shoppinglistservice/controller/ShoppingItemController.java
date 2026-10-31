package com.mealmate.shoppinglistservice.controller;

import com.mealmate.shoppinglistservice.dto.AddShoppingItemRequest;
import com.mealmate.shoppinglistservice.dto.ShoppingItemResponse;
import com.mealmate.shoppinglistservice.dto.UpdateShoppingItemRequest;
import com.mealmate.shoppinglistservice.security.SecurityUtils;
import com.mealmate.shoppinglistservice.service.ShoppingItemService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shopping-lists/{shoppingListId}/items")
@RequiredArgsConstructor
public class ShoppingItemController {

    private final ShoppingItemService shoppingItemService;

    @Operation(
            summary = "Add a shopping item",
            description = "Adds an item to the authenticated user's shopping list or merges it with an existing compatible item."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingItemResponse addShoppingItem(
            @PathVariable Long shoppingListId,
            @Valid @RequestBody AddShoppingItemRequest request
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        return shoppingItemService.addShoppingItem(
                currentUserId,
                shoppingListId,
                request
        );
    }

    @Operation(
            summary = "Update a shopping item",
            description = "Updates an item belonging to the authenticated user's shopping list."
    )
    @PutMapping("/{shoppingItemId}")
    public ShoppingItemResponse updateShoppingItem(
            @PathVariable Long shoppingListId,
            @PathVariable Long shoppingItemId,
            @Valid @RequestBody UpdateShoppingItemRequest request
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        return shoppingItemService.updateShoppingItem(
                currentUserId,
                shoppingListId,
                shoppingItemId,
                request
        );
    }

    @Operation(
            summary = "Delete a shopping item",
            description = "Removes an item from the authenticated user's shopping list."
    )
    @DeleteMapping("/{shoppingItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShoppingItem(
            @PathVariable Long shoppingListId,
            @PathVariable Long shoppingItemId
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        shoppingItemService.deleteShoppingItem(
                currentUserId,
                shoppingListId,
                shoppingItemId
        );
    }

    @Operation(
            summary = "Update purchase status",
            description = "Marks an item in the authenticated user's shopping list as purchased or not purchased."
    )
    @PatchMapping("/{shoppingItemId}/purchase")
    public ShoppingItemResponse updatePurchaseStatus(
            @PathVariable Long shoppingListId,
            @PathVariable Long shoppingItemId,
            @RequestParam boolean purchased
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        return shoppingItemService.updatePurchaseStatus(
                currentUserId,
                shoppingListId,
                shoppingItemId,
                purchased
        );
    }
}