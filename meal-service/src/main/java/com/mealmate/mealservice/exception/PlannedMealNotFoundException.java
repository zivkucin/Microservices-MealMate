package com.mealmate.mealservice.exception;

public class PlannedMealNotFoundException extends RuntimeException {

    public PlannedMealNotFoundException(String message) {
        super(message);
    }
}