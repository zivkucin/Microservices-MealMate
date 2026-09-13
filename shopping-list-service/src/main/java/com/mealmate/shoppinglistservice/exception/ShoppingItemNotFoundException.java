package com.mealmate.shoppinglistservice.exception;

public class ShoppingItemNotFoundException extends RuntimeException {

    public ShoppingItemNotFoundException(String message) {
        super(message);
    }
}