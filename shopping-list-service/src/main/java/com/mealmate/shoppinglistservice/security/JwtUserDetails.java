package com.mealmate.shoppinglistservice.security;

public record JwtUserDetails(
        Long userId,
        String username,
        String role
) {
}