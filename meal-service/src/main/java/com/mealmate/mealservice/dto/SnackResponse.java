package com.mealmate.mealservice.dto;

import com.mealmate.mealservice.enums.MeasurementUnit;

import java.time.LocalDateTime;

public record SnackResponse(
        Long snackId,
        String name,
        MeasurementUnit defaultUnit,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}