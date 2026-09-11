package com.mealmate.mealservice.exception;

public class NoRecipeAssignedException extends RuntimeException {

    public NoRecipeAssignedException(String message) {
        super(message);
    }
}