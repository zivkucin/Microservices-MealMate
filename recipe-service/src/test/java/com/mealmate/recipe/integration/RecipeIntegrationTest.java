package com.mealmate.recipe.integration;

import com.mealmate.recipe.entity.Recipe;
import com.mealmate.recipe.enums.RecipeCategory;
import com.mealmate.recipe.repository.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.mealmate.recipe.entity.Ingredient;
import com.mealmate.recipe.entity.RecipeIngredient;
import com.mealmate.recipe.repository.IngredientRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class RecipeIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private IngredientRepository ingredientRepository;

    @Test
    @Transactional
    void shouldSaveAndFindRecipe() {

        // Arrange
        Recipe recipe = new Recipe();

        recipe.setName("Carbonara");
        recipe.setDescription("Italian pasta");
        recipe.setInstructions("Cook pasta.");
        recipe.setPreparationTime(25);
        recipe.setCategory(RecipeCategory.ITALIAN);
        recipe.setUserId(1L);

        // Act
        Recipe savedRecipe = recipeRepository.save(recipe);

        Recipe foundRecipe = recipeRepository
                .findById(savedRecipe.getId())
                .orElseThrow();

        // Assert
        assertNotNull(foundRecipe.getId());
        assertEquals("Carbonara", foundRecipe.getName());
        assertEquals(25, foundRecipe.getPreparationTime());
        assertEquals(RecipeCategory.ITALIAN, foundRecipe.getCategory());
    }

    @Test
    @Transactional
    void shouldSaveRecipeWithIngredients() {

        // Arrange

        Ingredient spaghetti = new Ingredient();
        spaghetti.setName("Spaghetti");
        spaghetti.setUnit("g");

        Ingredient eggs = new Ingredient();
        eggs.setName("Eggs");
        eggs.setUnit("pcs");

        Ingredient savedSpaghetti =
                ingredientRepository.save(spaghetti);

        Ingredient savedEggs =
                ingredientRepository.save(eggs);

        Recipe recipe = new Recipe();

        recipe.setName("Carbonara");
        recipe.setDescription("Italian pasta");
        recipe.setInstructions("Cook pasta.");
        recipe.setPreparationTime(25);
        recipe.setCategory(RecipeCategory.ITALIAN);
        recipe.setUserId(1L);

        RecipeIngredient spaghettiIngredient =
                new RecipeIngredient();

        spaghettiIngredient.setRecipe(recipe);
        spaghettiIngredient.setIngredient(savedSpaghetti);
        spaghettiIngredient.setQuantity(300.0);

        RecipeIngredient eggsIngredient =
                new RecipeIngredient();

        eggsIngredient.setRecipe(recipe);
        eggsIngredient.setIngredient(savedEggs);
        eggsIngredient.setQuantity(3.0);

        recipe.getRecipeIngredients()
                .add(spaghettiIngredient);

        recipe.getRecipeIngredients()
                .add(eggsIngredient);

        // Act

        Recipe savedRecipe =
                recipeRepository.save(recipe);

        Recipe foundRecipe =
                recipeRepository.findById(savedRecipe.getId())
                        .orElseThrow();

        // Assert

        assertNotNull(foundRecipe.getId());

        assertEquals(
                2,
                foundRecipe.getRecipeIngredients().size()
        );

        assertEquals(
                "Spaghetti",
                foundRecipe.getRecipeIngredients()
                        .get(0)
                        .getIngredient()
                        .getName()
        );

        assertEquals(
                300.0,
                foundRecipe.getRecipeIngredients()
                        .get(0)
                        .getQuantity()
        );

        assertEquals(
                "Eggs",
                foundRecipe.getRecipeIngredients()
                        .get(1)
                        .getIngredient()
                        .getName()
        );

        assertEquals(
                3.0,
                foundRecipe.getRecipeIngredients()
                        .get(1)
                        .getQuantity()
        );
    }

    @Test
    @Transactional
    void shouldNotAllowSameIngredientTwiceInRecipe() {

        // Arrange

        Ingredient ingredient = new Ingredient();
        ingredient.setName("Tomato");
        ingredient.setUnit("g");

        ingredientRepository.save(ingredient);

        Recipe recipe = new Recipe();
        recipe.setName("Tomato Pasta");
        recipe.setDescription("Pasta with tomato");
        recipe.setInstructions("Cook pasta and add tomato.");
        recipe.setPreparationTime(20);
        recipe.setCategory(RecipeCategory.ITALIAN);
        recipe.setUserId(1L);

        RecipeIngredient first =
                new RecipeIngredient();

        first.setRecipe(recipe);
        first.setIngredient(ingredient);
        first.setQuantity(200.0);

        RecipeIngredient second =
                new RecipeIngredient();

        second.setRecipe(recipe);
        second.setIngredient(ingredient);
        second.setQuantity(300.0);

        recipe.getRecipeIngredients().add(first);
        recipe.getRecipeIngredients().add(second);

        // Act & Assert

        assertThrows(
                DataIntegrityViolationException.class,
                () -> recipeRepository.saveAndFlush(recipe)
        );
    }

    @Test
    @Transactional
    void shouldSaveAndFindIngredient() {

        // Arrange
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Salt");
        ingredient.setUnit("g");

        // Act
        Ingredient savedIngredient = ingredientRepository.saveAndFlush(ingredient);

        Ingredient foundIngredient = ingredientRepository
                .findById(savedIngredient.getId())
                .orElseThrow();

        // Assert
        assertNotNull(foundIngredient.getId());
        assertEquals("Salt", foundIngredient.getName());
        assertEquals("g", foundIngredient.getUnit());
    }

    @Test
    @Transactional
    void shouldNotAllowDuplicateIngredientName() {

        // Arrange
        Ingredient first = new Ingredient();
        first.setName("Pepper");
        first.setUnit("g");

        Ingredient second = new Ingredient();
        second.setName("Pepper");
        second.setUnit("g");

        // Act
        ingredientRepository.saveAndFlush(first);

        // Assert
        assertThrows(
                DataIntegrityViolationException.class,
                () -> ingredientRepository.saveAndFlush(second)
        );
    }
}