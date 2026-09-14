package com.mealmate.reviewservice.dto;

public record ReviewSummaryResponse(
        Long recipeId,
        Double averageRating,
        Long reviewCount,
        Long fiveStars,
        Long fourStars,
        Long threeStars,
        Long twoStars,
        Long oneStar
){
    
}