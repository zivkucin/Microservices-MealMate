package com.mealmate.mealservice.exception;

public class InvalidMealPlanException extends RuntimeException {
    public InvalidMealPlanException(String message) {
        super(message);
    }
}
