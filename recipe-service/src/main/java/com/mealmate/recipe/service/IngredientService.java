package com.mealmate.recipe.service;

import com.mealmate.recipe.exception.IngredientInUseException;
import com.mealmate.recipe.repository.RecipeIngredientRepository;
import com.mealmate.recipe.dto.IngredientRequest;
import com.mealmate.recipe.dto.IngredientResponse;
import com.mealmate.recipe.entity.Ingredient;
import com.mealmate.recipe.exception.IngredientAlreadyExistsException;
import com.mealmate.recipe.exception.IngredientNotFoundException;
import com.mealmate.recipe.mapper.IngredientMapper;
import com.mealmate.recipe.repository.IngredientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;
    private final RecipeIngredientRepository recipeIngredientRepository;

    public IngredientService(
            IngredientRepository ingredientRepository,
            IngredientMapper ingredientMapper,
            RecipeIngredientRepository recipeIngredientRepository) {

        this.ingredientRepository = ingredientRepository;
        this.ingredientMapper = ingredientMapper;
        this.recipeIngredientRepository = recipeIngredientRepository;
    }

    public IngredientResponse createIngredient(IngredientRequest request) {

        if (ingredientRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IngredientAlreadyExistsException(
                    "Ingredient already exists: " + request.getName()
            );
        }

        Ingredient ingredient = ingredientMapper.toEntity(request);

        Ingredient savedIngredient = ingredientRepository.save(ingredient);

        return ingredientMapper.toResponse(savedIngredient);
    }

    public List<IngredientResponse> getAllIngredients() {
        return ingredientRepository.findAll()
                .stream()
                .map(ingredientMapper::toResponse)
                .toList();
    }

    public IngredientResponse getIngredientById(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() ->
                        new IngredientNotFoundException("Ingredient not found"));

        return ingredientMapper.toResponse(ingredient);
    }

    public IngredientResponse updateIngredient(
            Long id,
            IngredientRequest request) {

        Ingredient existingIngredient = ingredientRepository.findById(id)
                .orElseThrow(() ->
                        new IngredientNotFoundException(
                                "Ingredient not found: " + id
                        ));

        if (ingredientRepository.existsByNameIgnoreCase(request.getName())
                && !existingIngredient.getName().equalsIgnoreCase(request.getName())) {

            throw new IngredientAlreadyExistsException(
                    "Ingredient already exists: " + request.getName()
            );
        }

        existingIngredient.setName(request.getName());
        existingIngredient.setUnit(request.getUnit());

        Ingredient updatedIngredient =
                ingredientRepository.save(existingIngredient);

        return ingredientMapper.toResponse(updatedIngredient);
    }

    public void deleteIngredient(Long id) {

        Ingredient existingIngredient = ingredientRepository.findById(id)
                .orElseThrow(() ->
                        new IngredientNotFoundException(
                                "Ingredient not found: " + id
                        ));

        if (recipeIngredientRepository.existsByIngredientId(id)) {
            throw new IngredientInUseException(
                    "Ingredient is used by one or more recipes: "
                            + existingIngredient.getName()
            );
        }

        ingredientRepository.delete(existingIngredient);
    }

    public List<IngredientResponse> searchIngredients(String name) {

        return ingredientRepository
                .findByNameContainingIgnoreCase(name)
                .stream()
                .map(ingredientMapper::toResponse)
                .toList();
    }
}