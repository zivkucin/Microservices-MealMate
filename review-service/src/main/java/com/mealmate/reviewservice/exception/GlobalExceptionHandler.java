package com.mealmate.reviewservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReviewNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleReviewNotFound(
            ReviewNotFoundException exception
    ) {
        return createErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(ReviewAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleReviewAlreadyExists(
            ReviewAlreadyExistsException exception
    ) {
        return createErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(OwnRecipeReviewException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleOwnRecipeReview(
            OwnRecipeReviewException exception
    ) {
        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    private Map<String, Object> createErrorResponse(
            HttpStatus status,
            String message
    ) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );
    }
}
