package com.mealmate.user.exception;

public class UserDisabledException extends RuntimeException {

    public UserDisabledException() {
        super("User account is disabled");
    }
}