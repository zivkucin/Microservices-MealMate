package com.mealmate.user.security;

public record JwtUserDetails(
        Long userId,
        String username,
        String role
) {
}