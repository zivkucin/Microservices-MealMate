package com.mealmate.reviewservice.service;

import com.mealmate.reviewservice.client.RecipeClient;
import com.mealmate.reviewservice.client.UserClient;
import com.mealmate.reviewservice.dto.CreateReviewRequest;
import com.mealmate.reviewservice.dto.RecipeResponse;
import com.mealmate.reviewservice.dto.ReviewResponse;
import com.mealmate.reviewservice.dto.ReviewSummaryResponse;
import com.mealmate.reviewservice.dto.UpdateReviewRequest;
import com.mealmate.reviewservice.dto.UserResponse;
import com.mealmate.reviewservice.entity.Review;
import com.mealmate.reviewservice.exception.OwnRecipeReviewException;
import com.mealmate.reviewservice.exception.ReviewAlreadyExistsException;
import com.mealmate.reviewservice.exception.ReviewNotFoundException;
import com.mealmate.reviewservice.mapper.ReviewMapper;
import com.mealmate.reviewservice.repository.ReviewRepository;
import com.mealmate.reviewservice.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final RecipeClient recipeClient;
    private final UserClient userClient;

    public ReviewResponse createReview(
            Long recipeId,
            CreateReviewRequest request
    ) {

        // Korisnik se uzima iz JWT tokena
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 1. Dohvati recept iz Recipe Service-a
        RecipeResponse recipe = recipeClient.getRecipeById(recipeId);

        // 2. Korisnik ne može da ocenjuje svoj recept
        if (recipe.userId().equals(currentUserId)) {
            throw new OwnRecipeReviewException(
                    "User cannot review their own recipe"
            );
        }

        // 3. Provera da korisnik već nije ocenio ovaj recept
        if (reviewRepository.existsByUserIdAndRecipeId(
                currentUserId,
                recipeId
        )) {
            throw new ReviewAlreadyExistsException(
                    "User has already reviewed this recipe"
            );
        }

        // 4. Kreiranje review-a
        Review review = reviewMapper.toEntity(request);

        // userId dolazi iz JWT-a, ne iz requesta
        review.setUserId(currentUserId);
        review.setRecipeId(recipeId);

        Review savedReview = reviewRepository.save(review);

        return toReviewResponse(savedReview);
    }

    public ReviewResponse getReviewById(Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(
                        "Review not found with id: " + reviewId
                ));

        return toReviewResponse(review);
    }

    public List<ReviewResponse> getReviewsByRecipeId(Long recipeId) {

        return reviewRepository
                .findAllByRecipeIdOrderByCreatedAtDesc(recipeId)
                .stream()
                .map(this::toReviewResponse)
                .toList();
    }

    public ReviewSummaryResponse getReviewSummary(Long recipeId) {

        List<Review> reviews =
                reviewRepository.findAllByRecipeIdOrderByCreatedAtDesc(recipeId);

        long reviewCount = reviews.size();

        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        long fiveStars = reviews.stream()
                .filter(review -> review.getRating() == 5)
                .count();

        long fourStars = reviews.stream()
                .filter(review -> review.getRating() == 4)
                .count();

        long threeStars = reviews.stream()
                .filter(review -> review.getRating() == 3)
                .count();

        long twoStars = reviews.stream()
                .filter(review -> review.getRating() == 2)
                .count();

        long oneStar = reviews.stream()
                .filter(review -> review.getRating() == 1)
                .count();

        return new ReviewSummaryResponse(
                recipeId,
                averageRating,
                reviewCount,
                fiveStars,
                fourStars,
                threeStars,
                twoStars,
                oneStar
        );
    }

    public ReviewResponse updateReview(
            Long reviewId,
            UpdateReviewRequest request
    ) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        Review review = reviewRepository
                .findByReviewIdAndUserId(reviewId, currentUserId)
                .orElseThrow(() -> new ReviewNotFoundException(
                        "Review not found with id: " + reviewId
                ));

        reviewMapper.updateEntity(request, review);

        Review updatedReview = reviewRepository.save(review);

        return toReviewResponse(updatedReview);
    }

    public void deleteReview(Long reviewId) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        Review review = reviewRepository
                .findByReviewIdAndUserId(reviewId, currentUserId)
                .orElseThrow(() -> new ReviewNotFoundException(
                        "Review not found with id: " + reviewId
                ));

        reviewRepository.delete(review);
    }

    private ReviewResponse toReviewResponse(Review review) {

        ReviewResponse response = reviewMapper.toResponse(review);

        UserResponse user = userClient.getUserById(review.getUserId());

        return new ReviewResponse(
                response.reviewId(),
                response.userId(),
                user.firstName(),
                response.recipeId(),
                response.rating(),
                response.comment(),
                response.createdAt(),
                response.updatedAt()
        );
    }
}