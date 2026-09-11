package com.mealmate.mealservice.exception;

public class MealPlanAlreadyExistsException extends RuntimeException {

    public MealPlanAlreadyExistsException(String message) {
        super(message);
    }
}