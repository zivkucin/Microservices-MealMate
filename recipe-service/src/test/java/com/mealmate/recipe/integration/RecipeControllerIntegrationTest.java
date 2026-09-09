package com.mealmate.recipe.integration;

import com.mealmate.recipe.dto.RecipeIngredientRequest;
import com.mealmate.recipe.dto.RecipeRequest;
import com.mealmate.recipe.entity.Ingredient;
import com.mealmate.recipe.entity.Recipe;
import com.mealmate.recipe.enums.RecipeCategory;
import com.mealmate.recipe.repository.IngredientRepository;
import com.mealmate.recipe.repository.RecipeIngredientRepository;
import com.mealmate.recipe.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class RecipeControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;

    @BeforeEach
    void cleanDatabase() {
        recipeIngredientRepository.deleteAll();
        recipeRepository.deleteAll();
        ingredientRepository.deleteAll();
    }

    @Test
    void shouldCreateRecipeWithIngredients() {

        // Arrange

        Ingredient spaghetti = new Ingredient();
        spaghetti.setName("Spaghetti");
        spaghetti.setUnit("g");

        Ingredient eggs = new Ingredient();
        eggs.setName("Eggs");
        eggs.setUnit("pcs");

        Ingredient savedSpaghetti =
                ingredientRepository.saveAndFlush(spaghetti);

        Ingredient savedEggs =
                ingredientRepository.saveAndFlush(eggs);

        RecipeIngredientRequest spaghettiRequest =
                new RecipeIngredientRequest();

        spaghettiRequest.setIngredientId(savedSpaghetti.getId());
        spaghettiRequest.setQuantity(300.0);

        RecipeIngredientRequest eggsRequest =
                new RecipeIngredientRequest();

        eggsRequest.setIngredientId(savedEggs.getId());
        eggsRequest.setQuantity(3.0);

        RecipeRequest request = new RecipeRequest();
        request.setName("Carbonara");
        request.setDescription("Italian pasta");
        request.setInstructions("Cook pasta.");
        request.setPreparationTime(25);
        request.setCategory(RecipeCategory.ITALIAN);
        request.setUserId(1L);
        request.setIngredients(
                List.of(spaghettiRequest, eggsRequest)
        );

        // Act

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/recipes",
                        request,
                        String.class
                );

        System.out.println("STATUS: " + response.getStatusCode());
        System.out.println("BODY: " + response.getBody());

        // Assert

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        Recipe savedRecipe = recipeRepository.findAll().stream()
                .filter(recipe -> "Carbonara".equals(recipe.getName()))
                .findFirst()
                .orElseThrow();

        assertEquals("Carbonara", savedRecipe.getName());
        assertEquals(25, savedRecipe.getPreparationTime());
        assertEquals(RecipeCategory.ITALIAN, savedRecipe.getCategory());
        assertEquals(1L, savedRecipe.getUserId());

        assertEquals(
                2,
                recipeIngredientRepository.countByRecipeId(savedRecipe.getId())
        );
    }

    @Test
    void shouldGetRecipeById() {
        Recipe recipe = new Recipe();
        recipe.setName("Pancakes");
        recipe.setDescription("Simple pancakes");
        recipe.setInstructions("Mix and cook.");
        recipe.setPreparationTime(15);
        recipe.setCategory(RecipeCategory.BREAKFAST);
        recipe.setUserId(1L);

        Recipe savedRecipe = recipeRepository.saveAndFlush(recipe);

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/recipes/" + savedRecipe.getId(),
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertTrue(response.getBody().contains("\"name\":\"Pancakes\""));
        assertTrue(response.getBody().contains("\"preparationTime\":15"));
        assertTrue(response.getBody().contains("\"category\":\"BREAKFAST\""));
    }

    @Test
    void shouldReturn404WhenRecipeDoesNotExist() {
        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/recipes/999",
                        String.class
                );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldGetAllRecipes() {

        Recipe recipe1 = new Recipe();
        recipe1.setName("Pasta");
        recipe1.setDescription("Easy pasta");
        recipe1.setInstructions("Cook pasta.");
        recipe1.setPreparationTime(20);
        recipe1.setCategory(RecipeCategory.ITALIAN);
        recipe1.setUserId(1L);

        Recipe recipe2 = new Recipe();
        recipe2.setName("Pancakes");
        recipe2.setDescription("Simple pancakes");
        recipe2.setInstructions("Mix and cook.");
        recipe2.setPreparationTime(15);
        recipe2.setCategory(RecipeCategory.BREAKFAST);
        recipe2.setUserId(1L);

        recipeRepository.saveAndFlush(recipe1);
        recipeRepository.saveAndFlush(recipe2);

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/recipes?page=0&size=10",
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        System.out.println("STATUS: " + response.getStatusCode());
        System.out.println("BODY: " + response.getBody());
    }

    @Test
    void shouldFilterRecipesByCategory() {

        Recipe italianRecipe = new Recipe();
        italianRecipe.setName("Pasta Carbonara");
        italianRecipe.setDescription("Italian pasta");
        italianRecipe.setInstructions("Cook pasta.");
        italianRecipe.setPreparationTime(25);
        italianRecipe.setCategory(RecipeCategory.ITALIAN);
        italianRecipe.setUserId(1L);

        Recipe breakfastRecipe = new Recipe();
        breakfastRecipe.setName("Pancakes");
        breakfastRecipe.setDescription("Breakfast");
        breakfastRecipe.setInstructions("Mix and cook.");
        breakfastRecipe.setPreparationTime(15);
        breakfastRecipe.setCategory(RecipeCategory.BREAKFAST);
        breakfastRecipe.setUserId(1L);

        recipeRepository.saveAndFlush(italianRecipe);
        recipeRepository.saveAndFlush(breakfastRecipe);

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/recipes?category=ITALIAN&page=0&size=10",
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        System.out.println("STATUS: " + response.getStatusCode());
        System.out.println("BODY: " + response.getBody());
    }

    @Test
    void shouldFilterRecipesByName() {

        Recipe pastaRecipe = new Recipe();
        pastaRecipe.setName("Pasta Carbonara");
        pastaRecipe.setDescription("Italian pasta");
        pastaRecipe.setInstructions("Cook pasta.");
        pastaRecipe.setPreparationTime(25);
        pastaRecipe.setCategory(RecipeCategory.ITALIAN);
        pastaRecipe.setUserId(1L);

        Recipe chickenRecipe = new Recipe();
        chickenRecipe.setName("Chicken Salad");
        chickenRecipe.setDescription("Fresh salad");
        chickenRecipe.setInstructions("Mix ingredients.");
        chickenRecipe.setPreparationTime(15);
        chickenRecipe.setCategory(RecipeCategory.VEGETARIAN);
        chickenRecipe.setUserId(1L);

        recipeRepository.saveAndFlush(pastaRecipe);
        recipeRepository.saveAndFlush(chickenRecipe);

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/recipes?name=pasta&page=0&size=10",
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        System.out.println("STATUS: " + response.getStatusCode());
        System.out.println("BODY: " + response.getBody());
    }

    @Test
    void shouldFilterRecipesByMaxPreparationTime() {

        Recipe quickRecipe = new Recipe();
        quickRecipe.setName("Quick Pasta");
        quickRecipe.setDescription("Fast meal");
        quickRecipe.setInstructions("Cook quickly.");
        quickRecipe.setPreparationTime(15);
        quickRecipe.setCategory(RecipeCategory.ITALIAN);
        quickRecipe.setUserId(1L);

        Recipe slowRecipe = new Recipe();
        slowRecipe.setName("Slow Roast");
        slowRecipe.setDescription("Long meal");
        slowRecipe.setInstructions("Cook slowly.");
        slowRecipe.setPreparationTime(90);
        slowRecipe.setCategory(RecipeCategory.OTHER);
        slowRecipe.setUserId(1L);

        recipeRepository.saveAndFlush(quickRecipe);
        recipeRepository.saveAndFlush(slowRecipe);

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/recipes?maxPreparationTime=30&page=0&size=10",
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        System.out.println("STATUS: " + response.getStatusCode());
        System.out.println("BODY: " + response.getBody());
    }

    @Test
    void shouldUpdateRecipe() {

        Recipe recipe = new Recipe();
        recipe.setName("Old Recipe");
        recipe.setDescription("Old description");
        recipe.setInstructions("Old instructions");
        recipe.setPreparationTime(30);
        recipe.setCategory(RecipeCategory.OTHER);
        recipe.setUserId(1L);

        Recipe savedRecipe = recipeRepository.saveAndFlush(recipe);

        Ingredient tomato = new Ingredient();
        tomato.setName("Tomato");
        tomato.setUnit("pcs");

        Ingredient savedTomato =
                ingredientRepository.saveAndFlush(tomato);

        RecipeIngredientRequest tomatoRequest =
                new RecipeIngredientRequest();
        tomatoRequest.setIngredientId(savedTomato.getId());
        tomatoRequest.setQuantity(2.0);

        RecipeRequest updateRequest = new RecipeRequest();
        updateRequest.setName("Updated Recipe");
        updateRequest.setDescription("Updated description");
        updateRequest.setInstructions("Updated instructions");
        updateRequest.setPreparationTime(20);
        updateRequest.setCategory(RecipeCategory.ITALIAN);
        updateRequest.setUserId(1L);
        updateRequest.setIngredients(List.of(tomatoRequest));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/recipes/" + savedRecipe.getId(),
                        HttpMethod.PUT,
                        new HttpEntity<>(updateRequest),
                        String.class
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        System.out.println("STATUS: " + response.getStatusCode());
        System.out.println("BODY: " + response.getBody());

        Recipe updatedRecipe =
                recipeRepository.findById(savedRecipe.getId())
                        .orElseThrow();

        assertEquals("Updated Recipe", updatedRecipe.getName());
        assertEquals("Updated description", updatedRecipe.getDescription());
        assertEquals("Updated instructions", updatedRecipe.getInstructions());
        assertEquals(20, updatedRecipe.getPreparationTime());
        assertEquals(RecipeCategory.ITALIAN, updatedRecipe.getCategory());
    }

    @Test
    void shouldDeleteRecipe() {

        Recipe recipe = new Recipe();
        recipe.setName("Recipe to Delete");
        recipe.setDescription("Will be deleted");
        recipe.setInstructions("Delete me.");
        recipe.setPreparationTime(10);
        recipe.setCategory(RecipeCategory.OTHER);
        recipe.setUserId(1L);

        Recipe savedRecipe = recipeRepository.saveAndFlush(recipe);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/recipes/" + savedRecipe.getId(),
                        HttpMethod.DELETE,
                        null,
                        String.class
                );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        assertEquals(
                false,
                recipeRepository.existsById(savedRecipe.getId())
        );
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingRecipe() {

        Ingredient tomato = new Ingredient();
        tomato.setName("TomatoForNotFoundTest");
        tomato.setUnit("pcs");

        Ingredient savedTomato =
                ingredientRepository.saveAndFlush(tomato);

        RecipeIngredientRequest tomatoRequest =
                new RecipeIngredientRequest();
        tomatoRequest.setIngredientId(savedTomato.getId());
        tomatoRequest.setQuantity(2.0);

        RecipeRequest request = new RecipeRequest();
        request.setName("Updated Recipe");
        request.setDescription("Updated description");
        request.setInstructions("Updated instructions");
        request.setPreparationTime(20);
        request.setCategory(RecipeCategory.ITALIAN);
        request.setUserId(1L);
        request.setIngredients(List.of(tomatoRequest));

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/recipes/999",
                        HttpMethod.PUT,
                        new HttpEntity<>(request),
                        String.class
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );
    }

}