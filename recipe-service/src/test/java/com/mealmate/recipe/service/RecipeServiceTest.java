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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeMapper recipeMapper;

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    void shouldFindRecipeById() {

        // Arrange
        Recipe recipe = new Recipe();
        recipe.setId(4L);
        recipe.setName("Carbonara");

        RecipeResponse response = new RecipeResponse();
        response.setId(4L);
        response.setName("Carbonara");

        when(recipeRepository.findById(4L))
                .thenReturn(Optional.of(recipe));

        when(recipeMapper.toResponse(recipe))
                .thenReturn(response);

        // Act
        RecipeResponse result = recipeService.getRecipeById(4L);

        // Assert
        assertEquals(4L, result.getId());
        assertEquals("Carbonara", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenRecipeDoesNotExist() {

        // Arrange
        when(recipeRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                RecipeNotFoundException.class,
                () -> recipeService.getRecipeById(999L)
        );
    }

    @Test
    void shouldCreateRecipeWithIngredients() {

        // Arrange

        RecipeRequest request = new RecipeRequest();
        request.setName("Carbonara");
        request.setDescription("Klasična italijanska pasta");
        request.setInstructions("Skuvati testeninu.");
        request.setPreparationTime(25);
        request.setCategory(RecipeCategory.ITALIAN);
        request.setUserId(1L);
        request.setImageUrl("https://example.com/carbonara.jpg");

        RecipeIngredientRequest spaghettiRequest =
                new RecipeIngredientRequest();
        spaghettiRequest.setIngredientId(1L);
        spaghettiRequest.setQuantity(300.0);

        RecipeIngredientRequest eggsRequest =
                new RecipeIngredientRequest();
        eggsRequest.setIngredientId(2L);
        eggsRequest.setQuantity(3.0);

        request.setIngredients(List.of(
                spaghettiRequest,
                eggsRequest
        ));

        Recipe recipe = new Recipe();
        recipe.setName("Carbonara");

        Ingredient spaghetti = new Ingredient();
        spaghetti.setId(1L);
        spaghetti.setName("Spaghetti");
        spaghetti.setUnit("g");

        Ingredient eggs = new Ingredient();
        eggs.setId(2L);
        eggs.setName("Eggs");
        eggs.setUnit("pcs");

        RecipeResponse response = new RecipeResponse();
        response.setId(10L);
        response.setName("Carbonara");

        when(recipeMapper.toEntity(request))
                .thenReturn(recipe);

        when(ingredientRepository.findById(1L))
                .thenReturn(Optional.of(spaghetti));

        when(ingredientRepository.findById(2L))
                .thenReturn(Optional.of(eggs));

        when(recipeRepository.save(recipe))
                .thenReturn(recipe);

        when(recipeMapper.toResponse(recipe))
                .thenReturn(response);

        // Act

        RecipeResponse result = recipeService.createRecipe(request);

        // Assert

        assertEquals(10L, result.getId());
        assertEquals("Carbonara", result.getName());

        assertEquals(2, recipe.getRecipeIngredients().size());

        assertEquals(
                spaghetti,
                recipe.getRecipeIngredients().get(0).getIngredient()
        );

        assertEquals(
                300.0,
                recipe.getRecipeIngredients().get(0).getQuantity()
        );

        assertEquals(
                eggs,
                recipe.getRecipeIngredients().get(1).getIngredient()
        );

        assertEquals(
                3.0,
                recipe.getRecipeIngredients().get(1).getQuantity()
        );

        verify(recipeRepository).save(recipe);
        verify(recipeMapper).toResponse(recipe);
    }

    @Test
    void shouldDeleteRecipe() {

        // Arrange

        Recipe recipe = new Recipe();
        recipe.setId(4L);

        when(recipeRepository.findById(4L))
                .thenReturn(Optional.of(recipe));

        // Act

        recipeService.deleteRecipe(4L);

        // Assert

        verify(recipeRepository).delete(recipe);
    }

    @Test
    void shouldGetAllRecipesWithPagination() {

        // Arrange

        Pageable pageable = PageRequest.of(0, 2);

        Recipe recipe1 = new Recipe();
        recipe1.setId(1L);
        recipe1.setName("Carbonara");

        Recipe recipe2 = new Recipe();
        recipe2.setId(2L);
        recipe2.setName("Pizza");

        RecipeResponse response1 = new RecipeResponse();
        response1.setId(1L);
        response1.setName("Carbonara");

        RecipeResponse response2 = new RecipeResponse();
        response2.setId(2L);
        response2.setName("Pizza");

        Page<Recipe> recipePage =
                new PageImpl<>(List.of(recipe1, recipe2), pageable, 3);

        when(recipeRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(recipePage);

        when(recipeMapper.toResponse(recipe1))
                .thenReturn(response1);

        when(recipeMapper.toResponse(recipe2))
                .thenReturn(response2);

        // Act

        Page<RecipeResponse> result =
                recipeService.getAllRecipes(
                        null,
                        null,
                        null,
                        pageable
                );

        // Assert

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());

        assertEquals("Carbonara", result.getContent().get(0).getName());
        assertEquals("Pizza", result.getContent().get(1).getName());

        verify(recipeRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void shouldFindRecipesUsingAllFilters() {

        // Arrange

        Pageable pageable = PageRequest.of(0, 10);

        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("Carbonara");
        recipe.setCategory(RecipeCategory.ITALIAN);
        recipe.setPreparationTime(25);

        RecipeResponse response = new RecipeResponse();
        response.setId(1L);
        response.setName("Carbonara");

        Page<Recipe> recipePage =
                new PageImpl<>(List.of(recipe), pageable, 1);

        when(recipeRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(recipePage);

        when(recipeMapper.toResponse(recipe))
                .thenReturn(response);

        // Act

        Page<RecipeResponse> result =
                recipeService.getAllRecipes(
                        "Carbon",
                        RecipeCategory.ITALIAN,
                        30,
                        pageable
                );

        // Assert

        assertEquals(1, result.getContent().size());
        assertEquals("Carbonara", result.getContent().get(0).getName());

        verify(recipeRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void shouldFindRecipesByName() {

        // Arrange

        Pageable pageable = PageRequest.of(0, 10);

        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("Carbonara");

        RecipeResponse response = new RecipeResponse();
        response.setId(1L);
        response.setName("Carbonara");

        Page<Recipe> recipePage =
                new PageImpl<>(List.of(recipe), pageable, 1);

        when(recipeRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(recipePage);

        when(recipeMapper.toResponse(recipe))
                .thenReturn(response);

        // Act

        Page<RecipeResponse> result =
                recipeService.getAllRecipes(
                        "Carbon",
                        null,
                        null,
                        pageable
                );

        // Assert

        assertEquals(1, result.getContent().size());
        assertEquals("Carbonara", result.getContent().get(0).getName());

        verify(recipeRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void shouldFindRecipesByCategory() {

        // Arrange

        Pageable pageable = PageRequest.of(0, 10);

        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("Carbonara");
        recipe.setCategory(RecipeCategory.ITALIAN);

        RecipeResponse response = new RecipeResponse();
        response.setId(1L);
        response.setName("Carbonara");

        Page<Recipe> recipePage =
                new PageImpl<>(List.of(recipe), pageable, 1);

        when(recipeRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(recipePage);

        when(recipeMapper.toResponse(recipe))
                .thenReturn(response);

        // Act

        Page<RecipeResponse> result =
                recipeService.getAllRecipes(
                        null,
                        RecipeCategory.ITALIAN,
                        null,
                        pageable
                );

        // Assert

        assertEquals(1, result.getContent().size());
        assertEquals("Carbonara", result.getContent().get(0).getName());

        verify(recipeRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void shouldFindRecipesByMaxPreparationTime() {

        // Arrange

        Pageable pageable = PageRequest.of(0, 10);

        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("Carbonara");
        recipe.setPreparationTime(25);

        RecipeResponse response = new RecipeResponse();
        response.setId(1L);
        response.setName("Carbonara");

        Page<Recipe> recipePage =
                new PageImpl<>(List.of(recipe), pageable, 1);

        when(recipeRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(recipePage);

        when(recipeMapper.toResponse(recipe))
                .thenReturn(response);

        // Act

        Page<RecipeResponse> result =
                recipeService.getAllRecipes(
                        null,
                        null,
                        30,
                        pageable
                );

        // Assert

        assertEquals(1, result.getContent().size());
        assertEquals("Carbonara", result.getContent().get(0).getName());

        verify(recipeRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void shouldThrowExceptionWhenIngredientDoesNotExist() {

        // Arrange

        RecipeRequest request = new RecipeRequest();
        request.setName("Carbonara");

        RecipeIngredientRequest ingredientRequest =
                new RecipeIngredientRequest();

        ingredientRequest.setIngredientId(999L);
        ingredientRequest.setQuantity(100.0);

        request.setIngredients(List.of(ingredientRequest));

        Recipe recipe = new Recipe();

        when(recipeMapper.toEntity(request))
                .thenReturn(recipe);

        when(ingredientRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert

        assertThrows(
                IngredientNotFoundException.class,
                () -> recipeService.createRecipe(request)
        );

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingRecipe() {

        // Arrange

        when(recipeRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert

        assertThrows(
                RecipeNotFoundException.class,
                () -> recipeService.deleteRecipe(999L)
        );

        verify(recipeRepository, never()).delete(any(Recipe.class));
    }

    @Test
    void shouldUpdateRecipe() {

        // Arrange

        Recipe existingRecipe = new Recipe();
        existingRecipe.setId(4L);
        existingRecipe.setName("Old Carbonara");

        RecipeRequest request = new RecipeRequest();
        request.setName("New Carbonara");
        request.setDescription("New description");
        request.setInstructions("New instructions");
        request.setPreparationTime(30);
        request.setCategory(RecipeCategory.ITALIAN);
        request.setUserId(1L);
        request.setImageUrl("https://example.com/new.jpg");
        request.setIngredients(List.of());

        when(recipeRepository.findById(4L))
                .thenReturn(Optional.of(existingRecipe));

        when(recipeRepository.save(existingRecipe))
                .thenReturn(existingRecipe);

        RecipeResponse response = new RecipeResponse();
        response.setId(4L);
        response.setName("New Carbonara");

        when(recipeMapper.toResponse(existingRecipe))
                .thenReturn(response);

        // Act

        RecipeResponse result =
                recipeService.updateRecipe(4L, request);

        // Assert

        assertEquals("New Carbonara", existingRecipe.getName());
        assertEquals("New description", existingRecipe.getDescription());
        assertEquals(30, existingRecipe.getPreparationTime());

        assertEquals(4L, result.getId());
        assertEquals("New Carbonara", result.getName());

        verify(recipeRepository).save(existingRecipe);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingRecipe() {

        // Arrange
        RecipeRequest request = new RecipeRequest();
        request.setName("New Carbonara");

        when(recipeRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                RecipeNotFoundException.class,
                () -> recipeService.updateRecipe(999L, request)
        );

        verify(recipeRepository, never())
                .save(any(Recipe.class));
    }
    @Test
    void shouldUpdateRecipeIngredients() {

        // Arrange

        Recipe existingRecipe = new Recipe();
        existingRecipe.setId(4L);

        Ingredient oldIngredient = new Ingredient();
        oldIngredient.setId(1L);
        oldIngredient.setName("Spaghetti");
        oldIngredient.setUnit("g");

        RecipeIngredient oldRecipeIngredient = new RecipeIngredient();
        oldRecipeIngredient.setRecipe(existingRecipe);
        oldRecipeIngredient.setIngredient(oldIngredient);
        oldRecipeIngredient.setQuantity(300.0);

        existingRecipe.getRecipeIngredients()
                .add(oldRecipeIngredient);

        Ingredient newIngredient = new Ingredient();
        newIngredient.setId(3L);
        newIngredient.setName("Parmesan");
        newIngredient.setUnit("g");

        RecipeIngredientRequest newIngredientRequest =
                new RecipeIngredientRequest();

        newIngredientRequest.setIngredientId(3L);
        newIngredientRequest.setQuantity(100.0);

        RecipeRequest request = new RecipeRequest();
        request.setName("Updated Carbonara");
        request.setDescription("Updated description");
        request.setInstructions("Updated instructions");
        request.setPreparationTime(30);
        request.setCategory(RecipeCategory.ITALIAN);
        request.setUserId(1L);
        request.setImageUrl("new-image");
        request.setIngredients(List.of(newIngredientRequest));

        when(recipeRepository.findById(4L))
                .thenReturn(Optional.of(existingRecipe));

        when(ingredientRepository.findById(3L))
                .thenReturn(Optional.of(newIngredient));

        when(recipeRepository.save(existingRecipe))
                .thenReturn(existingRecipe);

        RecipeResponse response = new RecipeResponse();
        response.setId(4L);
        response.setName("Updated Carbonara");

        when(recipeMapper.toResponse(existingRecipe))
                .thenReturn(response);

        // Act

        RecipeResponse result =
                recipeService.updateRecipe(4L, request);

        // Assert

        assertEquals("Updated Carbonara", result.getName());

        assertEquals(
                1,
                existingRecipe.getRecipeIngredients().size()
        );

        assertEquals(
                newIngredient,
                existingRecipe.getRecipeIngredients()
                        .getFirst()
                        .getIngredient()
        );

        assertEquals(
                100.0,
                existingRecipe.getRecipeIngredients()
                        .getFirst()
                        .getQuantity()
        );

        verify(recipeRepository).save(existingRecipe);
    }

    @Test
    void shouldThrowExceptionWhenSameIngredientIsAddedTwice() {

        // Arrange

        RecipeRequest request = new RecipeRequest();
        request.setName("Carbonara");

        RecipeIngredientRequest firstIngredient =
                new RecipeIngredientRequest();
        firstIngredient.setIngredientId(1L);
        firstIngredient.setQuantity(300.0);

        RecipeIngredientRequest secondIngredient =
                new RecipeIngredientRequest();
        secondIngredient.setIngredientId(1L);
        secondIngredient.setQuantity(100.0);

        request.setIngredients(List.of(
                firstIngredient,
                secondIngredient
        ));

        Recipe recipe = new Recipe();

        Ingredient spaghetti = new Ingredient();
        spaghetti.setId(1L);
        spaghetti.setName("Spaghetti");
        spaghetti.setUnit("g");

        when(recipeMapper.toEntity(request))
                .thenReturn(recipe);

        when(ingredientRepository.findById(1L))
                .thenReturn(Optional.of(spaghetti));

        // Act & Assert

        assertThrows(
                DuplicateRecipeIngredientException.class,
                () -> recipeService.createRecipe(request)
        );

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void shouldThrowExceptionWhenSameIngredientIsAddedTwiceDuringUpdate() {

        // Arrange

        Recipe existingRecipe = new Recipe();
        existingRecipe.setId(4L);
        existingRecipe.setName("Carbonara");

        RecipeRequest request = new RecipeRequest();
        request.setName("New Carbonara");

        RecipeIngredientRequest firstIngredient =
                new RecipeIngredientRequest();
        firstIngredient.setIngredientId(1L);
        firstIngredient.setQuantity(300.0);

        RecipeIngredientRequest secondIngredient =
                new RecipeIngredientRequest();
        secondIngredient.setIngredientId(1L);
        secondIngredient.setQuantity(100.0);

        request.setIngredients(List.of(
                firstIngredient,
                secondIngredient
        ));

        Ingredient spaghetti = new Ingredient();
        spaghetti.setId(1L);
        spaghetti.setName("Spaghetti");
        spaghetti.setUnit("g");

        when(recipeRepository.findById(4L))
                .thenReturn(Optional.of(existingRecipe));

        when(ingredientRepository.findById(1L))
                .thenReturn(Optional.of(spaghetti));

        // Act & Assert

        assertThrows(
                DuplicateRecipeIngredientException.class,
                () -> recipeService.updateRecipe(4L, request)
        );

        verify(recipeRepository, never()).save(any(Recipe.class));
    }
}