package com.mealmate.reviewservice.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long reviewId,
        Long userId,
        Long recipeId,
        Integer rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}