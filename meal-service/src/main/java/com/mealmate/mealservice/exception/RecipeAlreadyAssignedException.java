package com.mealmate.mealservice.exception;

public class RecipeAlreadyAssignedException extends RuntimeException {

    public RecipeAlreadyAssignedException(String message) {
        super(message);
    }
}