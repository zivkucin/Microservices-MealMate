package com.mealmate.mealservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.stream.Collectors;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WeeklyMealPlanNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleWeeklyMealPlanNotFound(
            WeeklyMealPlanNotFoundException exception
    ) {
        return new ErrorResponse(
                LocalDateTime.now(),
                404,
                "Not Found",
                exception.getMessage()
        );
    }

    @ExceptionHandler(PlannedMealNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlePlannedMealNotFound(
            PlannedMealNotFoundException exception
    ) {
        return new ErrorResponse(
                LocalDateTime.now(),
                404,
                "Not Found",
                exception.getMessage()
        );
    }

    @ExceptionHandler(NoRecipeAssignedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoRecipeAssigned(
            NoRecipeAssignedException exception
    ) {
        return new ErrorResponse(
                LocalDateTime.now(),
                404,
                "Not Found",
                exception.getMessage()
        );
    }

    @ExceptionHandler(PlannedSnackNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlePlannedSnackNotFound(
            PlannedSnackNotFoundException exception
    ) {
        return new ErrorResponse(
                LocalDateTime.now(),
                404,
                "Not Found",
                exception.getMessage()
        );
    }

    @ExceptionHandler(SnackNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleSnackNotFound(
            SnackNotFoundException exception
    ) {
        return new ErrorResponse(
                LocalDateTime.now(),
                404,
                "Not Found",
                exception.getMessage()
        );
    }

    @ExceptionHandler(RecipeAlreadyAssignedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleRecipeAlreadyAssigned(
            RecipeAlreadyAssignedException exception
    ) {
        return new ErrorResponse(
                LocalDateTime.now(),
                409,
                "Conflict",
                exception.getMessage()
        );
    }

    @ExceptionHandler(SnackAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleSnackAlreadyExists(
            SnackAlreadyExistsException exception
    ) {
        return new ErrorResponse(
                LocalDateTime.now(),
                409,
                "Conflict",
                exception.getMessage()
        );
    }

    @ExceptionHandler(MealPlanAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleMealPlanAlreadyExists(
            MealPlanAlreadyExistsException exception
    ) {
        return new ErrorResponse(
                LocalDateTime.now(),
                409,
                "Conflict",
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidMealPlanException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidMealPlan(
            InvalidMealPlanException exception
    ) {
        return new ErrorResponse(
                LocalDateTime.now(),
                400,
                "Bad Request",
                exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage()
                )
                .collect(Collectors.joining("; "));

        return new ErrorResponse(
                LocalDateTime.now(),
                400,
                "Bad Request",
                message
        );
    }

    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message
    ) {
    }
}