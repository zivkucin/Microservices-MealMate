package com.mealmate.reviewservice.repository;

import com.mealmate.reviewservice.entity.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class ReviewRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void cleanDatabase() {
        reviewRepository.deleteAll();
    }


    // =========================================================
    // SAVE
    // =========================================================

    @Test
    void saveReview_shouldSaveReviewSuccessfully() {

        Review review = createReview(
                2L,
                1L,
                5,
                "Odlican recept!"
        );

        Review savedReview =
                reviewRepository.save(review);

        assertNotNull(savedReview.getReviewId());

        Optional<Review> result =
                reviewRepository.findById(
                        savedReview.getReviewId()
                );

        assertTrue(result.isPresent());

        Review saved = result.get();

        assertEquals(2L, saved.getUserId());
        assertEquals(1L, saved.getRecipeId());
        assertEquals(5, saved.getRating());
        assertEquals(
                "Odlican recept!",
                saved.getComment()
        );
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }


    // =========================================================
    // EXISTS BY USER AND RECIPE
    // =========================================================

    @Test
    void existsByUserIdAndRecipeId_shouldReturnTrueForExistingReview() {

        Review review = createReview(
                2L,
                1L,
                5,
                "Odlican recept!"
        );

        reviewRepository.save(review);

        boolean exists =
                reviewRepository.existsByUserIdAndRecipeId(
                        2L,
                        1L
                );

        assertTrue(exists);
    }


    @Test
    void existsByUserIdAndRecipeId_shouldReturnFalseForUnknownReview() {

        boolean exists =
                reviewRepository.existsByUserIdAndRecipeId(
                        99L,
                        99L
                );

        assertFalse(exists);
    }


    // =========================================================
    // FIND BY REVIEW ID AND USER ID
    // =========================================================

    @Test
    void findByReviewIdAndUserId_shouldFindReviewForCorrectUser() {

        Review review = createReview(
                2L,
                1L,
                5,
                "Odlican recept!"
        );

        Review savedReview =
                reviewRepository.save(review);

        Optional<Review> result =
                reviewRepository.findByReviewIdAndUserId(
                        savedReview.getReviewId(),
                        2L
                );

        assertTrue(result.isPresent());

        assertEquals(
                savedReview.getReviewId(),
                result.get().getReviewId()
        );

        assertEquals(
                2L,
                result.get().getUserId()
        );

        assertEquals(
                1L,
                result.get().getRecipeId()
        );
    }


    @Test
    void findByReviewIdAndUserId_shouldReturnEmptyForWrongUser() {

        Review review = createReview(
                2L,
                1L,
                5,
                "Odlican recept!"
        );

        Review savedReview =
                reviewRepository.save(review);

        Optional<Review> result =
                reviewRepository.findByReviewIdAndUserId(
                        savedReview.getReviewId(),
                        99L
                );

        assertTrue(result.isEmpty());
    }


    // =========================================================
    // FIND ALL BY RECIPE ID
    // =========================================================

    @Test
    void findAllByRecipeIdOrderByCreatedAtDesc_shouldReturnReviewsForRecipe() {

        Review firstReview = createReview(
                2L,
                1L,
                5,
                "Odlican recept!"
        );

        Review secondReview = createReview(
                3L,
                1L,
                4,
                "Dobar recept!"
        );

        Review otherRecipeReview = createReview(
                4L,
                2L,
                3,
                "Drugi recept."
        );

        reviewRepository.save(firstReview);
        reviewRepository.save(secondReview);
        reviewRepository.save(otherRecipeReview);

        List<Review> reviews =
                reviewRepository
                        .findAllByRecipeIdOrderByCreatedAtDesc(1L);

        assertEquals(2, reviews.size());

        assertTrue(
                reviews.stream()
                        .allMatch(review ->
                                review.getRecipeId().equals(1L))
        );
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