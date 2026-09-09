package com.mealmate.recipe.exception;

public class DuplicateRecipeIngredientException extends RuntimeException {

    public DuplicateRecipeIngredientException(String message) {
        super(message);
    }
}
