package com.mealmate.recipe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecipeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleRecipeNotFound(RecipeNotFoundException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(IngredientAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleIngredientAlreadyExists(
            IngredientAlreadyExistsException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(IngredientNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleIngredientNotFound(
            IngredientNotFoundException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationErrors(
            MethodArgumentNotValidException exception) {

        return exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .reduce((message1, message2) -> message1 + "; " + message2)
                .orElse("Validation failed");
    }

    @ExceptionHandler(IngredientInUseException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleIngredientInUse(
            IngredientInUseException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(DuplicateRecipeIngredientException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateRecipeIngredient(
            DuplicateRecipeIngredientException exception) {
        return exception.getMessage();
    }
}