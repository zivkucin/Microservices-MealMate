package com.mealmate.recipe.service;

import com.mealmate.recipe.dto.IngredientRequest;
import com.mealmate.recipe.dto.IngredientResponse;
import com.mealmate.recipe.entity.Ingredient;
import com.mealmate.recipe.exception.IngredientAlreadyExistsException;
import com.mealmate.recipe.exception.IngredientInUseException;
import com.mealmate.recipe.exception.IngredientNotFoundException;
import com.mealmate.recipe.mapper.IngredientMapper;
import com.mealmate.recipe.repository.IngredientRepository;
import com.mealmate.recipe.repository.RecipeIngredientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private RecipeIngredientRepository recipeIngredientRepository;

    @Mock
    private IngredientMapper ingredientMapper;

    @InjectMocks
    private IngredientService ingredientService;

    @Test
    void shouldCreateIngredient() {

        // Arrange

        IngredientRequest request = new IngredientRequest();
        request.setName("Tomato");
        request.setUnit("pcs");

        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);
        ingredient.setName("Tomato");
        ingredient.setUnit("pcs");

        IngredientResponse response = new IngredientResponse();
        response.setId(1L);
        response.setName("Tomato");
        response.setUnit("pcs");

        when(ingredientRepository.existsByNameIgnoreCase("Tomato"))
                .thenReturn(false);

        when(ingredientMapper.toEntity(request))
                .thenReturn(ingredient);

        when(ingredientRepository.save(ingredient))
                .thenReturn(ingredient);

        when(ingredientMapper.toResponse(ingredient))
                .thenReturn(response);

        // Act

        IngredientResponse result =
                ingredientService.createIngredient(request);

        // Assert

        assertEquals(1L, result.getId());
        assertEquals("Tomato", result.getName());
        assertEquals("pcs", result.getUnit());

        verify(ingredientRepository).save(ingredient);
    }

    @Test
    void shouldThrowExceptionWhenIngredientAlreadyExists() {

        // Arrange

        IngredientRequest request = new IngredientRequest();
        request.setName("Tomato");
        request.setUnit("pcs");

        when(ingredientRepository.existsByNameIgnoreCase("Tomato"))
                .thenReturn(true);

        // Act & Assert

        assertThrows(
                IngredientAlreadyExistsException.class,
                () -> ingredientService.createIngredient(request)
        );

        verify(ingredientRepository, never())
                .save(any(Ingredient.class));
    }

    @Test
    void shouldFindIngredientById() {

        // Arrange

        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);
        ingredient.setName("Tomato");
        ingredient.setUnit("pcs");

        IngredientResponse response = new IngredientResponse();
        response.setId(1L);
        response.setName("Tomato");
        response.setUnit("pcs");

        when(ingredientRepository.findById(1L))
                .thenReturn(Optional.of(ingredient));

        when(ingredientMapper.toResponse(ingredient))
                .thenReturn(response);

        // Act

        IngredientResponse result =
                ingredientService.getIngredientById(1L);

        // Assert

        assertEquals(1L, result.getId());
        assertEquals("Tomato", result.getName());

        verify(ingredientMapper).toResponse(ingredient);
    }

    @Test
    void shouldThrowExceptionWhenIngredientDoesNotExist() {

        // Arrange

        when(ingredientRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert

        assertThrows(
                IngredientNotFoundException.class,
                () -> ingredientService.getIngredientById(999L)
        );
    }

    @Test
    void shouldUpdateIngredient() {

        // Arrange

        Ingredient existingIngredient = new Ingredient();
        existingIngredient.setId(1L);
        existingIngredient.setName("Tomato");
        existingIngredient.setUnit("pcs");

        IngredientRequest request = new IngredientRequest();
        request.setName("Cherry Tomato");
        request.setUnit("pcs");

        IngredientResponse response = new IngredientResponse();
        response.setId(1L);
        response.setName("Cherry Tomato");
        response.setUnit("pcs");

        when(ingredientRepository.findById(1L))
                .thenReturn(Optional.of(existingIngredient));

        when(ingredientRepository.existsByNameIgnoreCase("Cherry Tomato"))
                .thenReturn(false);

        when(ingredientRepository.save(existingIngredient))
                .thenReturn(existingIngredient);

        when(ingredientMapper.toResponse(existingIngredient))
                .thenReturn(response);

        // Act

        IngredientResponse result =
                ingredientService.updateIngredient(1L, request);

        // Assert

        assertEquals("Cherry Tomato", existingIngredient.getName());
        assertEquals("pcs", existingIngredient.getUnit());

        assertEquals("Cherry Tomato", result.getName());

        verify(ingredientRepository).save(existingIngredient);
    }

    @Test
    void shouldDeleteIngredient() {

        // Arrange

        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);
        ingredient.setName("Tomato");

        when(ingredientRepository.findById(1L))
                .thenReturn(Optional.of(ingredient));

        when(recipeIngredientRepository.existsByIngredientId(1L))
                .thenReturn(false);

        // Act

        ingredientService.deleteIngredient(1L);

        // Assert

        verify(ingredientRepository).delete(ingredient);
    }

    @Test
    void shouldThrowExceptionWhenDeletingIngredientInUse() {

        // Arrange

        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);
        ingredient.setName("Parmesan");

        when(ingredientRepository.findById(1L))
                .thenReturn(Optional.of(ingredient));

        when(recipeIngredientRepository.existsByIngredientId(1L))
                .thenReturn(true);

        // Act & Assert

        assertThrows(
                IngredientInUseException.class,
                () -> ingredientService.deleteIngredient(1L)
        );

        verify(ingredientRepository, never())
                .delete(any(Ingredient.class));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingIngredient() {

        // Arrange

        when(ingredientRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert

        assertThrows(
                IngredientNotFoundException.class,
                () -> ingredientService.deleteIngredient(999L)
        );

        verify(ingredientRepository, never())
                .delete(any(Ingredient.class));
    }

    @Test
    void shouldGetAllIngredients() {

        // Arrange

        Ingredient tomato = new Ingredient();
        tomato.setId(1L);
        tomato.setName("Tomato");
        tomato.setUnit("pcs");

        Ingredient eggs = new Ingredient();
        eggs.setId(2L);
        eggs.setName("Eggs");
        eggs.setUnit("pcs");

        IngredientResponse tomatoResponse = new IngredientResponse();
        tomatoResponse.setId(1L);
        tomatoResponse.setName("Tomato");

        IngredientResponse eggsResponse = new IngredientResponse();
        eggsResponse.setId(2L);
        eggsResponse.setName("Eggs");

        when(ingredientRepository.findAll())
                .thenReturn(List.of(tomato, eggs));

        when(ingredientMapper.toResponse(tomato))
                .thenReturn(tomatoResponse);

        when(ingredientMapper.toResponse(eggs))
                .thenReturn(eggsResponse);

        // Act

        List<IngredientResponse> result =
                ingredientService.getAllIngredients();

        // Assert

        assertEquals(2, result.size());
        assertEquals("Tomato", result.get(0).getName());
        assertEquals("Eggs", result.get(1).getName());
    }
}