package com.mealmate.reviewservice.mapper;

import com.mealmate.reviewservice.dto.CreateReviewRequest;
import com.mealmate.reviewservice.dto.ReviewResponse;
import com.mealmate.reviewservice.dto.UpdateReviewRequest;
import com.mealmate.reviewservice.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "reviewId", ignore = true)
    @Mapping(target = "recipeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Review toEntity(CreateReviewRequest request);

    ReviewResponse toResponse(Review review);

    @Mapping(target = "reviewId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "recipeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(
            UpdateReviewRequest request,
            @MappingTarget Review review
    );
}