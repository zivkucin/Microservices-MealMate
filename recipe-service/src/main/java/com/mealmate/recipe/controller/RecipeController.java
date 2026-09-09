package com.mealmate.recipe.controller;

import com.mealmate.recipe.dto.RecipeRequest;
import com.mealmate.recipe.dto.RecipeResponse;
import com.mealmate.recipe.enums.RecipeCategory;
import com.mealmate.recipe.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/recipes")
@Tag(
        name = "Recipes",
        description = "Recipe management endpoints"
)
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @Operation(summary = "Create a new recipe")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecipeResponse createRecipe(
            @Valid @RequestBody RecipeRequest request) {
        return recipeService.createRecipe(request);
    }

    @Operation(summary = "Get all recipes")
    @GetMapping
    public Page<RecipeResponse> getAllRecipes(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) RecipeCategory category,
            @RequestParam(required = false) Integer maxPreparationTime,
            Pageable pageable) {

        return recipeService.getAllRecipes(
                name,
                category,
                maxPreparationTime,
                pageable
        );
    }

    @Operation(summary = "Get recipe by ID")
    @GetMapping("/{id}")
    public RecipeResponse getRecipeById(
            @PathVariable Long id) {
        return recipeService.getRecipeById(id);
    }

    @Operation(summary = "Update recipe")
    @PutMapping("/{id}")
    public RecipeResponse updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeRequest request) {
        return recipeService.updateRecipe(id, request);
    }

    @Operation(summary = "Delete recipe")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
    }
}