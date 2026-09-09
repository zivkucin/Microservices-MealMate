package com.mealmate.recipe.service;

import com.mealmate.recipe.dto.RecipeIngredientRequest;
import com.mealmate.recipe.dto.RecipeRequest;
import com.mealmate.recipe.dto.RecipeResponse;
import com.mealmate.recipe.entity.Ingredient;
import com.mealmate.recipe.entity.Recipe;
import com.mealmate.recipe.entity.RecipeIngredient;
import com.mealmate.recipe.enums.RecipeCategory;
import com.mealmate.recipe.exception.DuplicateRecipeIngredientException;
import com.mealmate.recipe.exception.IngredientNotFoundException;
import com.mealmate.recipe.exception.RecipeNotFoundException;
import com.mealmate.recipe.mapper.RecipeMapper;
import com.mealmate.recipe.repository.IngredientRepository;
import com.mealmate.recipe.repository.RecipeRepository;
import com.mealmate.recipe.specification.RecipeSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;
    private final IngredientRepository ingredientRepository;

    public RecipeService(
            RecipeRepository recipeRepository,
            RecipeMapper recipeMapper,
            IngredientRepository ingredientRepository) {

        this.recipeRepository = recipeRepository;
        this.recipeMapper = recipeMapper;
        this.ingredientRepository = ingredientRepository;
    }

    public RecipeResponse createRecipe(RecipeRequest request) {

        Recipe recipe = recipeMapper.toEntity(request);

        Set<Long> ingredientIds = new HashSet<>();

        if (request.getIngredients() != null) {

            for (RecipeIngredientRequest ingredientRequest : request.getIngredients()) {

                if (!ingredientIds.add(ingredientRequest.getIngredientId())) {
                    throw new DuplicateRecipeIngredientException(
                            "Ingredient already added to this recipe: "
                                    + ingredientRequest.getIngredientId()
                    );
                }

                Ingredient ingredient = ingredientRepository
                        .findById(ingredientRequest.getIngredientId())
                        .orElseThrow(() ->
                                new IngredientNotFoundException(
                                        "Ingredient not found: "
                                                + ingredientRequest.getIngredientId()
                                ));

                RecipeIngredient recipeIngredient = new RecipeIngredient();

                recipeIngredient.setRecipe(recipe);
                recipeIngredient.setIngredient(ingredient);
                recipeIngredient.setQuantity(ingredientRequest.getQuantity());

                recipe.getRecipeIngredients().add(recipeIngredient);
            }
        }

        Recipe savedRecipe = recipeRepository.save(recipe);

        return recipeMapper.toResponse(savedRecipe);
    }

    public Page<RecipeResponse> getAllRecipes(
            String name,
            RecipeCategory category,
            Integer maxPreparationTime,
            Pageable pageable) {

        Specification<Recipe> specification = Specification.allOf();

        if (name != null && !name.isBlank()) {
            specification = specification.and(
                    RecipeSpecification.nameContains(name)
            );
        }

        if (category != null) {
            specification = specification.and(
                    RecipeSpecification.categoryEquals(category)
            );
        }

        if (maxPreparationTime != null) {
            specification = specification.and(
                    RecipeSpecification.maxPreparationTime(maxPreparationTime)
            );
        }

        return recipeRepository
                .findAll(specification, pageable)
                .map(recipeMapper::toResponse);
    }

    public RecipeResponse getRecipeById(Long id) {

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() ->
                        new RecipeNotFoundException("Recipe not found"));

        return recipeMapper.toResponse(recipe);
    }

    public RecipeResponse updateRecipe(
            Long id,
            RecipeRequest request) {

        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() ->
                        new RecipeNotFoundException("Recipe not found"));

        Set<Long> ingredientIds = new HashSet<>();
        List<RecipeIngredient> newRecipeIngredients = new ArrayList<>();

        if (request.getIngredients() != null) {

            for (RecipeIngredientRequest ingredientRequest : request.getIngredients()) {

                if (!ingredientIds.add(ingredientRequest.getIngredientId())) {
                    throw new DuplicateRecipeIngredientException(
                            "Ingredient already added to this recipe: "
                                    + ingredientRequest.getIngredientId()
                    );
                }

                Ingredient ingredient = ingredientRepository
                        .findById(ingredientRequest.getIngredientId())
                        .orElseThrow(() ->
                                new IngredientNotFoundException(
                                        "Ingredient not found: "
                                                + ingredientRequest.getIngredientId()
                                ));

                RecipeIngredient recipeIngredient = new RecipeIngredient();

                recipeIngredient.setRecipe(existingRecipe);
                recipeIngredient.setIngredient(ingredient);
                recipeIngredient.setQuantity(ingredientRequest.getQuantity());

                newRecipeIngredients.add(recipeIngredient);
            }
        }

        existingRecipe.setName(request.getName());
        existingRecipe.setDescription(request.getDescription());
        existingRecipe.setInstructions(request.getInstructions());
        existingRecipe.setPreparationTime(request.getPreparationTime());
        existingRecipe.setCategory(request.getCategory());
        existingRecipe.setUserId(request.getUserId());
        existingRecipe.setImageUrl(request.getImageUrl());

        existingRecipe.getRecipeIngredients().clear();
        existingRecipe.getRecipeIngredients().addAll(newRecipeIngredients);

        Recipe updatedRecipe = recipeRepository.save(existingRecipe);

        return recipeMapper.toResponse(updatedRecipe);
    }

    public void deleteRecipe(Long id) {

        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() ->
                        new RecipeNotFoundException("Recipe not found"));

        recipeRepository.delete(existingRecipe);
    }


}