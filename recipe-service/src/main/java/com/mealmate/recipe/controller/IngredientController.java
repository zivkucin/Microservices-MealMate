package com.mealmate.recipe.controller;

import com.mealmate.recipe.dto.IngredientRequest;
import com.mealmate.recipe.dto.IngredientResponse;
import com.mealmate.recipe.service.IngredientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/ingredients")
@Tag(
        name = "Ingredients",
        description = "Ingredient management endpoints"
)
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @Operation(summary = "Create a new ingredient")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IngredientResponse createIngredient(
            @Valid @RequestBody IngredientRequest request) {
        return ingredientService.createIngredient(request);
    }

    @Operation(summary = "Get all ingredients")
    @GetMapping
    public List<IngredientResponse> getAllIngredients() {
        return ingredientService.getAllIngredients();
    }

    @Operation(summary = "Search ingredients by name")
    @GetMapping("/search")
    public List<IngredientResponse> searchIngredients(
            @RequestParam String name) {

        return ingredientService.searchIngredients(name);
    }

    @Operation(summary = "Get ingredient by ID")
    @GetMapping("/{id}")
    public IngredientResponse getIngredientById(
            @PathVariable Long id) {
        return ingredientService.getIngredientById(id);
    }

    @Operation(summary = "Update ingredient")
    @PutMapping("/{id}")
    public IngredientResponse updateIngredient(
            @PathVariable Long id,
            @Valid @RequestBody IngredientRequest request) {

        return ingredientService.updateIngredient(id, request);
    }





    @Operation(summary = "Delete ingredient")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIngredient(@PathVariable Long id) {
        ingredientService.deleteIngredient(id);
    }
}