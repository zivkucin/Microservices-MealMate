package com.mealmate.reviewservice.service;

import com.mealmate.reviewservice.dto.CreateReviewRequest;
import com.mealmate.reviewservice.dto.ReviewResponse;
import com.mealmate.reviewservice.dto.ReviewSummaryResponse;
import com.mealmate.reviewservice.dto.UpdateReviewRequest;
import com.mealmate.reviewservice.entity.Review;
import com.mealmate.reviewservice.exception.ReviewAlreadyExistsException;
import com.mealmate.reviewservice.exception.ReviewNotFoundException;
import com.mealmate.reviewservice.mapper.ReviewMapper;
import com.mealmate.reviewservice.repository.ReviewRepository;
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
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void shouldCreateReview() {

        // Arrange

        CreateReviewRequest request =
                new CreateReviewRequest(
                        2L,
                        5,
                        "Odlican recept!"
                );

        Review review = new Review();
        review.setReviewId(1L);
        review.setUserId(2L);
        review.setRecipeId(1L);
        review.setRating(5);
        review.setComment("Odlican recept!");

        ReviewResponse response =
                new ReviewResponse(
                        1L,
                        2L,
                        1L,
                        5,
                        "Odlican recept!",
                        null,
                        null
                );

        when(reviewRepository.existsByUserIdAndRecipeId(2L, 1L))
                .thenReturn(false);

        when(reviewMapper.toEntity(request))
                .thenReturn(review);

        when(reviewRepository.save(review))
                .thenReturn(review);

        when(reviewMapper.toResponse(review))
                .thenReturn(response);

        // Act

        ReviewResponse result =
                reviewService.createReview(1L, request);

        // Assert

        assertEquals(1L, result.reviewId());
        assertEquals(2L, result.userId());
        assertEquals(1L, result.recipeId());
        assertEquals(5, result.rating());
        assertEquals("Odlican recept!", result.comment());

        verify(reviewRepository).save(review);
        verify(reviewMapper).toResponse(review);
    }

    @Test
    void shouldThrowExceptionWhenReviewAlreadyExists() {

        // Arrange

        CreateReviewRequest request =
                new CreateReviewRequest(
                        2L,
                        5,
                        "Odlican recept!"
                );

        when(reviewRepository.existsByUserIdAndRecipeId(2L, 1L))
                .thenReturn(true);

        // Act & Assert

        assertThrows(
                ReviewAlreadyExistsException.class,
                () -> reviewService.createReview(1L, request)
        );

        verify(reviewRepository, never())
                .save(any(Review.class));
    }

    @Test
    void shouldFindReviewById() {

        // Arrange

        Review review = new Review();
        review.setReviewId(1L);
        review.setUserId(2L);
        review.setRecipeId(1L);
        review.setRating(5);
        review.setComment("Odlican recept!");

        ReviewResponse response =
                new ReviewResponse(
                        1L,
                        2L,
                        1L,
                        5,
                        "Odlican recept!",
                        null,
                        null
                );

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(review));

        when(reviewMapper.toResponse(review))
                .thenReturn(response);

        // Act

        ReviewResponse result =
                reviewService.getReviewById(1L);

        // Assert

        assertEquals(1L, result.reviewId());
        assertEquals(5, result.rating());
        assertEquals("Odlican recept!", result.comment());

        verify(reviewMapper).toResponse(review);
    }

    @Test
    void shouldThrowExceptionWhenReviewDoesNotExist() {

        // Arrange

        when(reviewRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert

        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.getReviewById(999L)
        );
    }

    @Test
    void shouldGetReviewsByRecipeId() {

        // Arrange

        Review review1 = new Review();
        review1.setReviewId(1L);
        review1.setUserId(2L);
        review1.setRecipeId(1L);
        review1.setRating(5);

        Review review2 = new Review();
        review2.setReviewId(2L);
        review2.setUserId(3L);
        review2.setRecipeId(1L);
        review2.setRating(4);

        ReviewResponse response1 =
                new ReviewResponse(
                        1L,
                        2L,
                        1L,
                        5,
                        "Odlican recept!",
                        null,
                        null
                );

        ReviewResponse response2 =
                new ReviewResponse(
                        2L,
                        3L,
                        1L,
                        4,
                        "Dobar recept!",
                        null,
                        null
                );

        when(reviewRepository
                .findAllByRecipeIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(review1, review2));

        when(reviewMapper.toResponse(review1))
                .thenReturn(response1);

        when(reviewMapper.toResponse(review2))
                .thenReturn(response2);

        // Act

        List<ReviewResponse> result =
                reviewService.getReviewsByRecipeId(1L);

        // Assert

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).reviewId());
        assertEquals(2L, result.get(1).reviewId());

        verify(reviewRepository)
                .findAllByRecipeIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void shouldGetReviewSummary() {

        // Arrange

        Review review1 = new Review();
        review1.setRating(5);

        Review review2 = new Review();
        review2.setRating(4);

        Review review3 = new Review();
        review3.setRating(5);

        Review review4 = new Review();
        review4.setRating(3);

        when(reviewRepository
                .findAllByRecipeIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(
                        review1,
                        review2,
                        review3,
                        review4
                ));

        // Act

        ReviewSummaryResponse result =
                reviewService.getReviewSummary(1L);

        // Assert

        assertEquals(1L, result.recipeId());
        assertEquals(4.25, result.averageRating());
        assertEquals(4L, result.reviewCount());
        assertEquals(2L, result.fiveStars());
        assertEquals(1L, result.fourStars());
        assertEquals(1L, result.threeStars());
        assertEquals(0L, result.twoStars());
        assertEquals(0L, result.oneStar());
    }

    @Test
    void shouldUpdateReview() {

        // Arrange

        Review existingReview = new Review();
        existingReview.setReviewId(1L);
        existingReview.setUserId(2L);
        existingReview.setRecipeId(1L);
        existingReview.setRating(5);
        existingReview.setComment("Stari komentar");

        UpdateReviewRequest request =
                new UpdateReviewRequest(
                        4,
                        "Novi komentar"
                );

        ReviewResponse response =
                new ReviewResponse(
                        1L,
                        2L,
                        1L,
                        4,
                        "Novi komentar",
                        null,
                        null
                );

        when(reviewRepository
                .findByReviewIdAndUserId(1L, 2L))
                .thenReturn(Optional.of(existingReview));

        when(reviewRepository.save(existingReview))
                .thenReturn(existingReview);

        when(reviewMapper.toResponse(existingReview))
                .thenReturn(response);

        // Act

        ReviewResponse result =
                reviewService.updateReview(
                        1L,
                        2L,
                        request
                );

        // Assert

        verify(reviewMapper)
                .updateEntity(request, existingReview);

        verify(reviewRepository)
                .save(existingReview);

        assertEquals(1L, result.reviewId());
        assertEquals(4, result.rating());
        assertEquals("Novi komentar", result.comment());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingReviewOfAnotherUser() {

        // Arrange

        UpdateReviewRequest request =
                new UpdateReviewRequest(
                        4,
                        "Novi komentar"
                );

        when(reviewRepository
                .findByReviewIdAndUserId(1L, 3L))
                .thenReturn(Optional.empty());

        // Act & Assert

        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.updateReview(
                        1L,
                        3L,
                        request
                )
        );

        verify(reviewRepository, never())
                .save(any(Review.class));
    }

    @Test
    void shouldDeleteReview() {

        // Arrange

        Review review = new Review();
        review.setReviewId(1L);
        review.setUserId(2L);
        review.setRecipeId(1L);

        when(reviewRepository
                .findByReviewIdAndUserId(1L, 2L))
                .thenReturn(Optional.of(review));

        // Act

        reviewService.deleteReview(1L, 2L);

        // Assert

        verify(reviewRepository).delete(review);
    }

    @Test
    void shouldThrowExceptionWhenDeletingReviewOfAnotherUser() {

        // Arrange

        when(reviewRepository
                .findByReviewIdAndUserId(1L, 3L))
                .thenReturn(Optional.empty());

        // Act & Assert

        assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.deleteReview(1L, 3L)
        );

        verify(reviewRepository, never())
                .delete(any(Review.class));
    }
}