package com.mealmate.recipe.exception;

public class IngredientAlreadyExistsException extends RuntimeException {

    public IngredientAlreadyExistsException(String message) {
        super(message);
    }
}
