package com.mealmate.recipe.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static JwtUserDetails getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !(authentication.getDetails() instanceof JwtUserDetails)) {

            throw new IllegalStateException(
                    "Authenticated user information is not available"
            );
        }

        return (JwtUserDetails) authentication.getDetails();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().userId();
    }

    public static String getCurrentUsername() {
        return getCurrentUser().username();
    }

    public static String getCurrentUserRole() {
        return getCurrentUser().role();
    }
}