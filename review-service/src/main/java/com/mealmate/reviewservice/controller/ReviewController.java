package com.mealmate.reviewservice.controller;

import com.mealmate.reviewservice.dto.CreateReviewRequest;
import com.mealmate.reviewservice.dto.ReviewResponse;
import com.mealmate.reviewservice.dto.ReviewSummaryResponse;
import com.mealmate.reviewservice.dto.UpdateReviewRequest;
import com.mealmate.reviewservice.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(
        name = "Reviews",
        description = "Operations for creating, viewing, updating and deleting recipe reviews"
)
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "Get review by ID",
            description = "Returns a single review by its ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Review found",
                    content = @Content(
                            schema = @Schema(implementation = ReviewResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Review not found"
            )
    })
    @GetMapping("/reviews/{reviewId}")
    public ReviewResponse getReviewById(
            @Parameter(
                    description = "ID of the review",
                    example = "1"
            )
            @PathVariable Long reviewId
    ) {
        return reviewService.getReviewById(reviewId);
    }


    @Operation(
            summary = "Get reviews for a recipe",
            description = "Returns all reviews for the specified recipe, ordered from newest to oldest."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reviews successfully returned",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ReviewResponse.class
                            )
                    )
            )
    })
    @GetMapping("/recipes/{recipeId}/reviews")
    public List<ReviewResponse> getReviewsByRecipeId(
            @Parameter(
                    description = "ID of the recipe",
                    example = "1"
            )
            @PathVariable Long recipeId
    ) {
        return reviewService.getReviewsByRecipeId(recipeId);
    }


    @Operation(
            summary = "Create a review",
            description = "Creates a new review for a recipe. A user can submit only one review for the same recipe."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Review successfully created",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ReviewResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid review data"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User has already reviewed this recipe"
            )
    })
    @PostMapping("/recipes/{recipeId}/reviews")
    public ReviewResponse createReview(
            @Parameter(
                    description = "ID of the recipe",
                    example = "1"
            )
            @PathVariable Long recipeId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Review data",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = CreateReviewRequest.class
                            )
                    )
            )
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return reviewService.createReview(recipeId, request);
    }


    @Operation(
            summary = "Get review summary",
            description = "Returns the average rating, total number of reviews and rating distribution for a recipe."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Review summary successfully returned",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ReviewSummaryResponse.class
                            )
                    )
            )
    })
    @GetMapping("/recipes/{recipeId}/reviews/summary")
    public ReviewSummaryResponse getReviewSummary(
            @Parameter(
                    description = "ID of the recipe",
                    example = "1"
            )
            @PathVariable Long recipeId
    ) {
        return reviewService.getReviewSummary(recipeId);
    }


    @Operation(
            summary = "Update a review",
            description = "Updates the rating and comment of an existing review. Only the user who created the review can update it."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Review successfully updated",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ReviewResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid review data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Review not found for the specified user"
            )
    })
    @PutMapping("/reviews/{reviewId}")
    public ReviewResponse updateReview(
            @Parameter(
                    description = "ID of the review",
                    example = "1"
            )
            @PathVariable Long reviewId,

            @Parameter(
                    description = "ID of the user who owns the review",
                    example = "2"
            )
            @RequestParam Long userId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated review data",
                    required = true,
                    content = @Content(
                            schema = @Schema(
                                    implementation = UpdateReviewRequest.class
                            )
                    )
            )
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        return reviewService.updateReview(
                reviewId,
                userId,
                request
        );
    }


    @Operation(
            summary = "Delete a review",
            description = "Deletes an existing review. Only the user who created the review can delete it."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Review successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Review not found for the specified user"
            )
    })
    @DeleteMapping("/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(
            @Parameter(
                    description = "ID of the review",
                    example = "1"
            )
            @PathVariable Long reviewId,

            @Parameter(
                    description = "ID of the user who owns the review",
                    example = "2"
            )
            @RequestParam Long userId
    ) {
        reviewService.deleteReview(reviewId, userId);
    }
}