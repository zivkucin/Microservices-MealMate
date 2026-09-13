package com.mealmate.shoppinglistservice.controller;

import com.mealmate.shoppinglistservice.dto.AddShoppingItemRequest;
import com.mealmate.shoppinglistservice.dto.ShoppingItemResponse;
import com.mealmate.shoppinglistservice.dto.UpdateShoppingItemRequest;
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
            description = "Adds a new item to a shopping list or merges it with an existing compatible item."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingItemResponse addShoppingItem(
            @PathVariable Long shoppingListId,
            @Valid @RequestBody AddShoppingItemRequest request
    ) {
        return shoppingItemService.addShoppingItem(
                shoppingListId,
                request
        );
    }

    @Operation(
            summary = "Update a shopping item",
            description = "Updates the name, quantity, or measurement unit of an existing shopping item."
    )
    @PutMapping("/{shoppingItemId}")
    public ShoppingItemResponse updateShoppingItem(
            @PathVariable Long shoppingListId,
            @PathVariable Long shoppingItemId,
            @Valid @RequestBody UpdateShoppingItemRequest request
    ) {
        return shoppingItemService.updateShoppingItem(
                shoppingListId,
                shoppingItemId,
                request
        );
    }

    @Operation(
            summary = "Delete a shopping item",
            description = "Removes an item from the shopping list."
    )
    @DeleteMapping("/{shoppingItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShoppingItem(
            @PathVariable Long shoppingListId,
            @PathVariable Long shoppingItemId
    ) {
        shoppingItemService.deleteShoppingItem(
                shoppingListId,
                shoppingItemId
        );
    }

    @Operation(
            summary = "Update purchase status",
            description = "Marks a shopping item as purchased or not purchased."
    )
    @PatchMapping("/{shoppingItemId}/purchase")
    public ShoppingItemResponse updatePurchaseStatus(
            @PathVariable Long shoppingListId,
            @PathVariable Long shoppingItemId,
            @RequestParam boolean purchased
    ) {
        return shoppingItemService.updatePurchaseStatus(
                shoppingListId,
                shoppingItemId,
                purchased
        );
    }

}