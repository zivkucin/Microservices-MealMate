package com.mealmate.recipe.security;

public record JwtUserDetails(
        Long userId,
        String username,
        String role
) {
}