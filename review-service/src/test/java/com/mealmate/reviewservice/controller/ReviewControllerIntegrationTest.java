package com.mealmate.reviewservice.controller;

import com.mealmate.reviewservice.entity.Review;
import com.mealmate.reviewservice.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void cleanDatabase() {
        reviewRepository.deleteAll();
    }


    // =========================================================
    // CREATE
    // =========================================================

    @Test
    void createReview_shouldReturnOk() throws Exception {

        String requestBody = """
                {
                    "userId": 2,
                    "rating": 5,
                    "comment": "Odlican recept!"
                }
                """;

        mockMvc.perform(
                        post("/recipes/{recipeId}/reviews", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").isNumber())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.recipeId").value(1))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment")
                        .value("Odlican recept!"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }


    @Test
    void createReview_withInvalidRating_shouldReturnBadRequest()
            throws Exception {

        String requestBody = """
                {
                    "userId": 2,
                    "rating": 6,
                    "comment": "Neispravna ocena"
                }
                """;

        mockMvc.perform(
                        post("/recipes/{recipeId}/reviews", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }


    @Test
    void createReview_withDuplicateReview_shouldReturnConflict()
            throws Exception {

        Review existingReview = createReview(
                2L,
                1L,
                5,
                "Prvi komentar"
        );

        reviewRepository.save(existingReview);

        String requestBody = """
                {
                    "userId": 2,
                    "rating": 4,
                    "comment": "Drugi komentar"
                }
                """;

        mockMvc.perform(
                        post("/recipes/{recipeId}/reviews", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict());
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    @Test
    void getReviewById_shouldReturnOk() throws Exception {

        Review review = reviewRepository.save(
                createReview(
                        2L,
                        1L,
                        5,
                        "Odlican recept!"
                )
        );

        mockMvc.perform(
                        get("/reviews/{reviewId}", review.getReviewId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId")
                        .value(review.getReviewId().intValue()))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.recipeId").value(1))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment")
                        .value("Odlican recept!"));
    }


    // =========================================================
    // GET BY RECIPE
    // =========================================================

    @Test
    void getReviewsByRecipeId_shouldReturnOk()
            throws Exception {

        reviewRepository.save(
                createReview(
                        2L,
                        1L,
                        5,
                        "Odlican!"
                )
        );

        reviewRepository.save(
                createReview(
                        3L,
                        1L,
                        4,
                        "Dobar!"
                )
        );

        reviewRepository.save(
                createReview(
                        4L,
                        2L,
                        3,
                        "Drugi recept"
                )
        );

        mockMvc.perform(
                        get("/recipes/{recipeId}/reviews", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }


    // =========================================================
    // SUMMARY
    // =========================================================

    @Test
    void getReviewSummary_shouldReturnOk()
            throws Exception {

        reviewRepository.save(
                createReview(
                        2L,
                        1L,
                        5,
                        "Odlican!"
                )
        );

        reviewRepository.save(
                createReview(
                        3L,
                        1L,
                        4,
                        "Dobar!"
                )
        );

        reviewRepository.save(
                createReview(
                        4L,
                        1L,
                        5,
                        "Sjajan!"
                )
        );

        mockMvc.perform(
                        get("/recipes/{recipeId}/reviews/summary", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeId").value(1))
                .andExpect(jsonPath("$.averageRating").value(4.666666666666667))
                .andExpect(jsonPath("$.reviewCount").value(3))
                .andExpect(jsonPath("$.fiveStars").value(2))
                .andExpect(jsonPath("$.fourStars").value(1))
                .andExpect(jsonPath("$.threeStars").value(0))
                .andExpect(jsonPath("$.twoStars").value(0))
                .andExpect(jsonPath("$.oneStar").value(0));
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void updateReview_shouldReturnOk() throws Exception {

        Review review = reviewRepository.save(
                createReview(
                        2L,
                        1L,
                        5,
                        "Stari komentar"
                )
        );

        String requestBody = """
                {
                    "rating": 4,
                    "comment": "Novi komentar"
                }
                """;

        mockMvc.perform(
                        put("/reviews/{reviewId}", review.getReviewId())
                                .param("userId", "2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId")
                        .value(review.getReviewId().intValue()))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.recipeId").value(1))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment")
                        .value("Novi komentar"));
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void deleteReview_shouldReturnNoContent()
            throws Exception {

        Review review = reviewRepository.save(
                createReview(
                        2L,
                        1L,
                        5,
                        "Za brisanje"
                )
        );

        mockMvc.perform(
                        delete("/reviews/{reviewId}",
                                review.getReviewId())
                                .param("userId", "2")
                )
                .andExpect(status().isNoContent());
    }


    // =========================================================
    // HELPER
    // =========================================================

    private Review createReview(
            Long userId,
            Long recipeId,
            Integer rating,
            String comment) {

        Review review = new Review();

        review.setUserId(userId);
        review.setRecipeId(recipeId);
        review.setRating(rating);
        review.setComment(comment);

        return review;
    }
}