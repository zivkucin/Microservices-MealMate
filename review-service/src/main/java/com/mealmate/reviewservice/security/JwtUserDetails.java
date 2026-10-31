package com.mealmate.reviewservice.security;

public record JwtUserDetails(
        Long userId,
        String username,
        String role
) {
}