package com.mealmate.shoppinglistservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateShoppingListRequest(

        @NotNull
        LocalDate weekStartDate

) {
}