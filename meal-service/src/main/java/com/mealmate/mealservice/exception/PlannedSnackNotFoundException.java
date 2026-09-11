package com.mealmate.mealservice.exception;

public class PlannedSnackNotFoundException extends RuntimeException {

    public PlannedSnackNotFoundException(String message) {
        super(message);
    }
}