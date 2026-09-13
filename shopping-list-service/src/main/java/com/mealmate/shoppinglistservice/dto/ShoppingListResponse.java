package com.mealmate.shoppinglistservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ShoppingListResponse(

        Long shoppingListId,
        Long userId,
        LocalDate weekStartDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ShoppingItemResponse> items

) {
}