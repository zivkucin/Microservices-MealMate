package com.mealmate.recipe.integration;

import com.mealmate.recipe.dto.IngredientRequest;
import com.mealmate.recipe.entity.Ingredient;
import com.mealmate.recipe.repository.IngredientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class IngredientControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Test
    void shouldCreateIngredient() {

        // Arrange
        IngredientRequest request = new IngredientRequest();
        request.setName("Flour");
        request.setUnit("g");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/ingredients",
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        Ingredient savedIngredient = ingredientRepository
                .findByNameContainingIgnoreCase("Flour")
                .stream()
                .findFirst()
                .orElseThrow();

        assertEquals("Flour", savedIngredient.getName());
        assertEquals("g", savedIngredient.getUnit());
    }

    @Test
    void shouldGetIngredientById() {

        // Arrange
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Sugar");
        ingredient.setUnit("g");

        Ingredient savedIngredient = ingredientRepository.saveAndFlush(ingredient);

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/ingredients/" + savedIngredient.getId(),
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().contains("Sugar"));
        assertEquals(true, response.getBody().contains("g"));
    }

    @Test
    void shouldReturn404WhenIngredientDoesNotExist() {

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/ingredients/999999",
                String.class
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturn400WhenIngredientNameIsBlank() {

        // Arrange
        IngredientRequest request = new IngredientRequest();
        request.setName("");
        request.setUnit("g");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/ingredients",
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldReturn409WhenIngredientAlreadyExists() {

        // Arrange
        IngredientRequest request = new IngredientRequest();
        request.setName("Milk");
        request.setUnit("ml");

        restTemplate.postForEntity(
                "/ingredients",
                request,
                String.class
        );

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/ingredients",
                request,
                String.class
        );

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void shouldGetAllIngredients() {

        // Arrange
        Ingredient salt = new Ingredient();
        salt.setName("Salt");
        salt.setUnit("g");

        Ingredient sugar = new Ingredient();
        sugar.setName("Sugar");
        sugar.setUnit("g");

        ingredientRepository.saveAndFlush(salt);
        ingredientRepository.saveAndFlush(sugar);

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/ingredients",
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().contains("Salt"));
        assertEquals(true, response.getBody().contains("Sugar"));
    }

    @Test
    void shouldSearchIngredientsByName() {

        // Arrange
        Ingredient sugar = new Ingredient();
        sugar.setName("Sugar");
        sugar.setUnit("g");

        Ingredient brownSugar = new Ingredient();
        brownSugar.setName("Brown Sugar");
        brownSugar.setUnit("g");

        Ingredient salt = new Ingredient();
        salt.setName("Salt");
        salt.setUnit("g");

        ingredientRepository.saveAndFlush(sugar);
        ingredientRepository.saveAndFlush(brownSugar);
        ingredientRepository.saveAndFlush(salt);

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/ingredients/search?name=sugar",
                String.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(true, response.getBody().contains("Sugar"));
        assertEquals(true, response.getBody().contains("Brown Sugar"));
        assertEquals(false, response.getBody().contains("Salt"));
    }
}