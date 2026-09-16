package com.mealmate.reviewservice.client;

import com.mealmate.reviewservice.dto.RecipeResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface RecipeClient {

    @GetExchange("/recipes/{recipeId}")
    RecipeResponse getRecipeById(@PathVariable("recipeId") Long recipeId);
}