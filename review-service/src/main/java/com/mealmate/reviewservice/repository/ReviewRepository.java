package com.mealmate.reviewservice.repository;

import com.mealmate.reviewservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndRecipeId(
            Long userId,
            Long recipeId
    );

    Optional<Review> findByReviewIdAndUserId(
            Long reviewId,
            Long userId
    );

    List<Review> findAllByRecipeIdOrderByCreatedAtDesc(
            Long recipeId
    );
}