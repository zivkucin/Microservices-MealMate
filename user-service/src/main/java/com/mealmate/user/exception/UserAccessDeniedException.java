package com.mealmate.user.exception;

public class UserAccessDeniedException extends RuntimeException {

    public UserAccessDeniedException() {
        super("You do not have permission to modify this user");
    }
}