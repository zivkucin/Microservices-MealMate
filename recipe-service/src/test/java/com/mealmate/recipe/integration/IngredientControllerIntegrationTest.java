package com.mealmate.recipe.integration;

import com.mealmate.recipe.dto.IngredientRequest;
import com.mealmate.recipe.entity.Ingredient;
import com.mealmate.recipe.repository.IngredientRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Value("${jwt.secret}")
    private String jwtSecret;

    @BeforeEach
    void cleanDatabase() {

        ingredientRepository.deleteAll();

        // Podrazumevano svaki zahtev šaljemo kao USER 1.
        // Ovo sprečava 403 zbog nedostatka Authorization header-a.
        restTemplate.getRestTemplate()
                .getInterceptors()
                .clear();

        restTemplate.getRestTemplate()
                .getInterceptors()
                .add((request, body, execution) -> {

                    if (request.getHeaders()
                            .getFirst(HttpHeaders.AUTHORIZATION) == null) {

                        request.getHeaders().setBearerAuth(
                                generateTestToken(1L, "USER")
                        );
                    }

                    return execution.execute(request, body);
                });
    }

    private String generateTestToken(Long userId, String role) {

        SecretKey key = Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );

        Date now = new Date();

        return Jwts.builder()
                .subject("test-" + role.toLowerCase())
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(
                        new Date(
                                now.getTime() + 60 * 60 * 1000
                        )
                )
                .signWith(key)
                .compact();
    }

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
        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

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

        Ingredient savedIngredient =
                ingredientRepository.saveAndFlush(ingredient);

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/ingredients/" + savedIngredient.getId(),
                String.class
        );

        // Assert
        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertTrue(response.getBody().contains("Sugar"));
        assertTrue(response.getBody().contains("g"));
    }

    @Test
    void shouldReturn404WhenIngredientDoesNotExist() {

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/ingredients/999999",
                String.class
        );

        // Assert
        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );
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
        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );
    }

    @Test
    void shouldReturn409WhenIngredientAlreadyExists() {

        // Arrange
        IngredientRequest request = new IngredientRequest();
        request.setName("Milk");
        request.setUnit("ml");

        ResponseEntity<String> firstResponse =
                restTemplate.postForEntity(
                        "/ingredients",
                        request,
                        String.class
                );

        assertEquals(
                HttpStatus.CREATED,
                firstResponse.getStatusCode()
        );

        // Act
        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/ingredients",
                        request,
                        String.class
                );

        // Assert
        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );
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
        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertTrue(response.getBody().contains("Salt"));
        assertTrue(response.getBody().contains("Sugar"));
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
        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertTrue(response.getBody().contains("Sugar"));
        assertTrue(response.getBody().contains("Brown Sugar"));
        assertFalse(response.getBody().contains("Salt"));
    }
}