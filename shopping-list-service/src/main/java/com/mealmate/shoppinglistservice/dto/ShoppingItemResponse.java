package com.mealmate.shoppinglistservice.dto;

import com.mealmate.shoppinglistservice.entity.MeasurementUnit;

import java.time.LocalDateTime;

public record ShoppingItemResponse(

        Long shoppingItemId,
        Long shoppingListId,
        String name,
        Double quantity,
        MeasurementUnit unit,
        boolean purchased,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}