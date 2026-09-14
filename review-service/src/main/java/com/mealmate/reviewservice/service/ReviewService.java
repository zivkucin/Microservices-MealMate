package com.mealmate.reviewservice.service;

import com.mealmate.reviewservice.dto.CreateReviewRequest;
import com.mealmate.reviewservice.dto.ReviewResponse;
import com.mealmate.reviewservice.dto.UpdateReviewRequest;
import com.mealmate.reviewservice.exception.ReviewAlreadyExistsException;
import com.mealmate.reviewservice.entity.Review;
import com.mealmate.reviewservice.exception.ReviewNotFoundException;
import com.mealmate.reviewservice.mapper.ReviewMapper;
import com.mealmate.reviewservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import com.mealmate.reviewservice.dto.ReviewSummaryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    public ReviewResponse createReview(
            Long recipeId,
            CreateReviewRequest request
    ) {

        if (reviewRepository.existsByUserIdAndRecipeId(
                request.userId(),
                recipeId
        )) {
            throw new ReviewAlreadyExistsException(
                    "User has already reviewed this recipe"
            );
        }

        Review review = reviewMapper.toEntity(request);
        review.setRecipeId(recipeId);

        Review savedReview = reviewRepository.save(review);

        return reviewMapper.toResponse(savedReview);
    }

    public ReviewResponse getReviewById(Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(
                        "Review not found with id: " + reviewId
                ));

        return reviewMapper.toResponse(review);
    }

    public List<ReviewResponse> getReviewsByRecipeId(Long recipeId) {

        return reviewRepository
                .findAllByRecipeIdOrderByCreatedAtDesc(recipeId)
                .stream()
                .map(reviewMapper::toResponse)
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
            Long userId,
            UpdateReviewRequest request
    ) {

        Review review = reviewRepository
                .findByReviewIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ReviewNotFoundException(
                        "Review not found with id: " + reviewId
                ));

        reviewMapper.updateEntity(request, review);

        Review updatedReview = reviewRepository.save(review);

        return reviewMapper.toResponse(updatedReview);
    }
    public void deleteReview(
            Long reviewId,
            Long userId
    ) {

        Review review = reviewRepository
                .findByReviewIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ReviewNotFoundException(
                        "Review not found with id: " + reviewId
                ));

        reviewRepository.delete(review);
    }
}

