package com.mealmate.mealservice.security;

public record JwtUserDetails(
        Long userId,
        String username,
        String role
) {
}