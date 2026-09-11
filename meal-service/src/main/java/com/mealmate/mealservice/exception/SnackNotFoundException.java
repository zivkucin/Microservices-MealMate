package com.mealmate.mealservice.exception;

public class SnackNotFoundException extends RuntimeException {

    public SnackNotFoundException(String message) {
        super(message);
    }
}