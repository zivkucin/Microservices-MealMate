package com.mealmate.mealservice.exception;

public class WeeklyMealPlanNotFoundException extends RuntimeException {

    public WeeklyMealPlanNotFoundException(String message) {
        super(message);
    }
}