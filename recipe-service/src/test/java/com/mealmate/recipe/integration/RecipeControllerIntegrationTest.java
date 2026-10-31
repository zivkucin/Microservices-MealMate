package com.mealmate.recipe.integration;

import com.mealmate.recipe.dto.RecipeIngredientRequest;
import com.mealmate.recipe.dto.RecipeRequest;
import com.mealmate.recipe.dto.UpdateRecipeRequest;
import com.mealmate.recipe.entity.Ingredient;
import com.mealmate.recipe.entity.Recipe;
import com.mealmate.recipe.enums.RecipeCategory;
import com.mealmate.recipe.repository.IngredientRepository;
import com.mealmate.recipe.repository.RecipeIngredientRepository;
import com.mealmate.recipe.repository.RecipeRepository;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
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

    @Value("${jwt.secret}")
    private String jwtSecret;

    @BeforeEach
    void cleanDatabase() {
        recipeIngredientRepository.deleteAll();
        recipeRepository.deleteAll();
        ingredientRepository.deleteAll();

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
                .expiration(new Date(now.getTime() + 60 * 60 * 1000))
                .signWith(key)
                .compact();
    }

    private HttpHeaders adminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(
                generateTestToken(99L, "ADMIN")
        );
        return headers;
    }

    private RecipeIngredientRequest createIngredientRequest(
            String name,
            String unit,
            double quantity
    ) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(name);
        ingredient.setUnit(unit);
        ingredient = ingredientRepository.save(ingredient);

        RecipeIngredientRequest request =
                new RecipeIngredientRequest();

        request.setIngredientId(ingredient.getId());
        request.setQuantity(quantity);

        return request;
    }

    @Test
    void shouldCreateRecipeWithIngredients() {

        Ingredient spaghetti = new Ingredient();
        spaghetti.setName("Spaghetti");
        spaghetti.setUnit("g");
        spaghetti = ingredientRepository.save(spaghetti);

        Ingredient eggs = new Ingredient();
        eggs.setName("Eggs");
        eggs.setUnit("pcs");
        eggs = ingredientRepository.save(eggs);

        RecipeIngredientRequest spaghettiRequest =
                new RecipeIngredientRequest();

        spaghettiRequest.setIngredientId(spaghetti.getId());
        spaghettiRequest.setQuantity(300.0);

        RecipeIngredientRequest eggsRequest =
                new RecipeIngredientRequest();

        eggsRequest.setIngredientId(eggs.getId());
        eggsRequest.setQuantity(3.0);

        RecipeRequest request = new RecipeRequest();

        request.setName("Carbonara");
        request.setDescription("Italian pasta");
        request.setInstructions("Cook pasta.");
        request.setPreparationTime(25);
        request.setCategory(RecipeCategory.ITALIAN);
        request.setIngredients(
                List.of(spaghettiRequest, eggsRequest)
        );

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/recipes",
                        request,
                        String.class
                );

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Carbonara"));
        assertTrue(response.getBody().contains("Spaghetti"));
        assertTrue(response.getBody().contains("Eggs"));

        assertEquals(
                1L,
                recipeRepository.count()
        );

        assertEquals(
                2L,
                recipeIngredientRepository.count()
        );
    }

    @Test
    void shouldGetRecipeById() {

        Recipe recipe = new Recipe();

        recipe.setName("Pasta Carbonara");
        recipe.setDescription("Italian pasta");
        recipe.setInstructions("Cook pasta.");
        recipe.setPreparationTime(25);
        recipe.setCategory(RecipeCategory.ITALIAN);
        recipe.setUserId(1L);

        recipe = recipeRepository.save(recipe);

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/recipes/" + recipe.getId(),
                        String.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertTrue(
                response.getBody().contains("Pasta Carbonara")
        );
    }

    @Test
    void shouldReturn404WhenRecipeDoesNotExist() {

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/recipes/999999",
                        String.class
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );
    }

    @Test
    void shouldGetAllRecipes() {

        Recipe pasta = new Recipe();

        pasta.setName("Pasta");
        pasta.setDescription("Easy pasta");
        pasta.setInstructions("Cook pasta.");
        pasta.setPreparationTime(20);
        pasta.setCategory(RecipeCategory.ITALIAN);
        pasta.setUserId(1L);

        recipeRepository.save(pasta);

        Recipe pancakes = new Recipe();

        pancakes.setName("Pancakes");
        pancakes.setDescription("Simple pancakes");
        pancakes.setInstructions("Mix and cook.");
        pancakes.setPreparationTime(15);
        pancakes.setCategory(RecipeCategory.BREAKFAST);
        pancakes.setUserId(1L);

        recipeRepository.save(pancakes);

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/recipes",
                        String.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertTrue(response.getBody().contains("Pasta"));
        assertTrue(response.getBody().contains("Pancakes"));
    }

    @Test
    void shouldFilterRecipesByCategory() {

        Recipe italian = new Recipe();

        italian.setName("Pasta Carbonara");
        italian.setDescription("Italian pasta");
        italian.setInstructions("Cook pasta.");
        italian.setPreparationTime(25);
        italian.setCategory(RecipeCategory.ITALIAN);
        italian.setUserId(1L);

        recipeRepository.save(italian);

        Recipe breakfast = new Recipe();

        breakfast.setName("Pancakes");
        breakfast.setDescription("Simple pancakes");
        breakfast.setInstructions("Mix and cook.");
        breakfast.setPreparationTime(15);
        breakfast.setCategory(RecipeCategory.BREAKFAST);
        breakfast.setUserId(1L);

        recipeRepository.save(breakfast);

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/recipes?category=ITALIAN",
                        String.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertTrue(
                response.getBody().contains("Pasta Carbonara")
        );

        assertFalse(
                response.getBody().contains("Pancakes")
        );
    }

    @Test
    void shouldFilterRecipesByName() {

        Recipe carbonara = new Recipe();

        carbonara.setName("Pasta Carbonara");
        carbonara.setDescription("Italian pasta");
        carbonara.setInstructions("Cook pasta.");
        carbonara.setPreparationTime(25);
        carbonara.setCategory(RecipeCategory.ITALIAN);
        carbonara.setUserId(1L);

        recipeRepository.save(carbonara);

        Recipe soup = new Recipe();

        soup.setName("Tomato Soup");
        soup.setDescription("Simple soup");
        soup.setInstructions("Boil tomatoes.");
        soup.setPreparationTime(30);
        soup.setCategory(RecipeCategory.OTHER);
        soup.setUserId(1L);

        recipeRepository.save(soup);

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/recipes?name=Pasta",
                        String.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertTrue(
                response.getBody().contains("Pasta Carbonara")
        );

        assertFalse(
                response.getBody().contains("Tomato Soup")
        );
    }

    @Test
    void shouldFilterRecipesByMaxPreparationTime() {

        Recipe quick = new Recipe();

        quick.setName("Quick Pasta");
        quick.setDescription("Fast meal");
        quick.setInstructions("Cook quickly.");
        quick.setPreparationTime(15);
        quick.setCategory(RecipeCategory.ITALIAN);
        quick.setUserId(1L);

        recipeRepository.save(quick);

        Recipe slow = new Recipe();

        slow.setName("Slow Roast");
        slow.setDescription("Long meal");
        slow.setInstructions("Cook slowly.");
        slow.setPreparationTime(90);
        slow.setCategory(RecipeCategory.OTHER);
        slow.setUserId(1L);

        recipeRepository.save(slow);

        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "/recipes?maxPreparationTime=30",
                        String.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertTrue(
                response.getBody().contains("Quick Pasta")
        );

        assertFalse(
                response.getBody().contains("Slow Roast")
        );
    }

    @Test
    void shouldUpdateRecipe() {

        Recipe recipe = new Recipe();

        recipe.setName("Old Recipe");
        recipe.setDescription("Old description");
        recipe.setInstructions("Old instructions");
        recipe.setPreparationTime(15);
        recipe.setCategory(RecipeCategory.ITALIAN);
        recipe.setUserId(1L);

        recipe = recipeRepository.save(recipe);

        RecipeIngredientRequest ingredientRequest =
                createIngredientRequest(
                        "Tomato",
                        "pcs",
                        2.0
                );

        UpdateRecipeRequest updateRequest =
                new UpdateRecipeRequest();

        updateRequest.setName("Updated Recipe");
        updateRequest.setDescription(
                "Updated description"
        );
        updateRequest.setInstructions(
                "Updated instructions"
        );
        updateRequest.setPreparationTime(20);
        updateRequest.setCategory(
                RecipeCategory.ITALIAN
        );
        updateRequest.setIngredients(
                List.of(ingredientRequest)
        );

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/recipes/" + recipe.getId(),
                        HttpMethod.PUT,
                        new HttpEntity<>(updateRequest),
                        String.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertTrue(
                response.getBody().contains(
                        "Updated Recipe"
                )
        );

        Recipe updated =
                recipeRepository
                        .findById(recipe.getId())
                        .orElseThrow();

        assertEquals(
                "Updated Recipe",
                updated.getName()
        );

        assertEquals(
                1L,
                updated.getUserId()
        );
    }

    @Test
    void shouldDeleteRecipe() {

        Recipe recipe = new Recipe();

        recipe.setName("Recipe to delete");
        recipe.setDescription("Description");
        recipe.setInstructions("Instructions");
        recipe.setPreparationTime(20);
        recipe.setCategory(RecipeCategory.ITALIAN);
        recipe.setUserId(1L);

        recipe = recipeRepository.save(recipe);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/recipes/" + recipe.getId(),
                        HttpMethod.DELETE,
                        null,
                        String.class
                );

        assertEquals(
                HttpStatus.NO_CONTENT,
                response.getStatusCode()
        );

        assertFalse(
                recipeRepository.existsById(recipe.getId())
        );
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingRecipe() {

        RecipeIngredientRequest ingredientRequest =
                createIngredientRequest(
                        "Tomato",
                        "pcs",
                        2.0
                );

        UpdateRecipeRequest updateRequest =
                new UpdateRecipeRequest();

        updateRequest.setName("Updated Recipe");
        updateRequest.setDescription(
                "Updated description"
        );
        updateRequest.setInstructions(
                "Updated instructions"
        );
        updateRequest.setPreparationTime(20);
        updateRequest.setCategory(
                RecipeCategory.ITALIAN
        );
        updateRequest.setIngredients(
                List.of(ingredientRequest)
        );

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/recipes/999999",
                        HttpMethod.PUT,
                        new HttpEntity<>(updateRequest),
                        String.class
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );
    }

    @Test
    void shouldReturn403WhenUserTriesToUpdateSomeoneElsesRecipe() {

        Recipe recipe = new Recipe();

        recipe.setName("Original Recipe");
        recipe.setDescription("Original description");
        recipe.setInstructions("Original instructions");
        recipe.setPreparationTime(20);
        recipe.setCategory(RecipeCategory.ITALIAN);
        recipe.setUserId(2L);

        recipe = recipeRepository.save(recipe);

        RecipeIngredientRequest ingredientRequest =
                createIngredientRequest(
                        "Tomato",
                        "pcs",
                        2.0
                );

        UpdateRecipeRequest updateRequest =
                new UpdateRecipeRequest();

        updateRequest.setName("Hacked Recipe");
        updateRequest.setDescription(
                "Changed description"
        );
        updateRequest.setInstructions(
                "Changed instructions"
        );
        updateRequest.setPreparationTime(25);
        updateRequest.setCategory(
                RecipeCategory.ITALIAN
        );
        updateRequest.setIngredients(
                List.of(ingredientRequest)
        );

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/recipes/" + recipe.getId(),
                        HttpMethod.PUT,
                        new HttpEntity<>(updateRequest),
                        String.class
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                response.getStatusCode()
        );

        Recipe unchanged =
                recipeRepository
                        .findById(recipe.getId())
                        .orElseThrow();

        assertEquals(
                "Original Recipe",
                unchanged.getName()
        );

        assertEquals(
                2L,
                unchanged.getUserId()
        );
    }

    @Test
    void shouldReturn403WhenUserTriesToDeleteSomeoneElsesRecipe() {

        Recipe recipe = new Recipe();

        recipe.setName("Protected Recipe");
        recipe.setDescription("Description");
        recipe.setInstructions("Instructions");
        recipe.setPreparationTime(20);
        recipe.setCategory(RecipeCategory.ITALIAN);
        recipe.setUserId(2L);

        recipe = recipeRepository.save(recipe);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/recipes/" + recipe.getId(),
                        HttpMethod.DELETE,
                        null,
                        String.class
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                response.getStatusCode()
        );

        assertTrue(
                recipeRepository.existsById(
                        recipe.getId()
                )
        );
    }

    @Test
    void shouldAllowAdminToUpdateSomeoneElsesRecipe() {

        Recipe recipe = new Recipe();

        recipe.setName("Original Recipe");
        recipe.setDescription("Original description");
        recipe.setInstructions("Original instructions");
        recipe.setPreparationTime(20);
        recipe.setCategory(RecipeCategory.ITALIAN);
        recipe.setUserId(2L);

        recipe = recipeRepository.save(recipe);

        RecipeIngredientRequest ingredientRequest =
                createIngredientRequest(
                        "Tomato",
                        "pcs",
                        2.0
                );

        UpdateRecipeRequest updateRequest =
                new UpdateRecipeRequest();

        updateRequest.setName(
                "Admin Updated Recipe"
        );
        updateRequest.setDescription(
                "Admin changed description"
        );
        updateRequest.setInstructions(
                "Admin changed instructions"
        );
        updateRequest.setPreparationTime(30);
        updateRequest.setCategory(
                RecipeCategory.ITALIAN
        );
        updateRequest.setIngredients(
                List.of(ingredientRequest)
        );

        HttpEntity<UpdateRecipeRequest> requestEntity =
                new HttpEntity<>(
                        updateRequest,
                        adminHeaders()
                );

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/recipes/" + recipe.getId(),
                        HttpMethod.PUT,
                        requestEntity,
                        String.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertTrue(
                response.getBody().contains(
                        "Admin Updated Recipe"
                )
        );

        Recipe updated =
                recipeRepository
                        .findById(recipe.getId())
                        .orElseThrow();

        assertEquals(
                "Admin Updated Recipe",
                updated.getName()
        );

        assertEquals(
                2L,
                updated.getUserId()
        );
    }

    @Test
    void shouldAllowAdminToDeleteSomeoneElsesRecipe() {

        Recipe recipe = new Recipe();

        recipe.setName("Recipe for Admin Delete");
        recipe.setDescription("Description");
        recipe.setInstructions("Instructions");
        recipe.setPreparationTime(20);
        recipe.setCategory(RecipeCategory.ITALIAN);
        recipe.setUserId(2L);

        recipe = recipeRepository.save(recipe);

        HttpEntity<Void> requestEntity =
                new HttpEntity<>(adminHeaders());

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/recipes/" + recipe.getId(),
                        HttpMethod.DELETE,
                        requestEntity,
                        String.class
                );

        assertEquals(
                HttpStatus.NO_CONTENT,
                response.getStatusCode()
        );

        assertFalse(
                recipeRepository.existsById(
                        recipe.getId()
                )
        );
    }
}