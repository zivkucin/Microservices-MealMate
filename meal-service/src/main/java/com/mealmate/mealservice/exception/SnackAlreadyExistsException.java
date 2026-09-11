package com.mealmate.mealservice.exception;

public class SnackAlreadyExistsException extends RuntimeException {

    public SnackAlreadyExistsException(String message) {
        super(message);
    }
}