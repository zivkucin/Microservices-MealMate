package com.mealmate.shoppinglistservice.exception;

public class ShoppingListAlreadyExistsException extends RuntimeException {

    public ShoppingListAlreadyExistsException(String message) {
        super(message);
    }
}