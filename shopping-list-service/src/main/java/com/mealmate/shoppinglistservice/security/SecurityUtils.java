package com.mealmate.shoppinglistservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static JwtUserDetails getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal()
                instanceof JwtUserDetails userDetails)) {

            throw new IllegalStateException(
                    "No authenticated user found."
            );
        }

        return userDetails;
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