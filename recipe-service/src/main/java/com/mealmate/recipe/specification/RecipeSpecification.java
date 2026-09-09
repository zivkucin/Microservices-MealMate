package com.mealmate.recipe.specification;

import com.mealmate.recipe.entity.Recipe;
import com.mealmate.recipe.enums.RecipeCategory;
import org.springframework.data.jpa.domain.Specification;

public class RecipeSpecification {

    public static Specification<Recipe> nameContains(String name) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                );
    }

    public static Specification<Recipe> categoryEquals(RecipeCategory category) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("category"),
                        category
                );
    }

    public static Specification<Recipe> maxPreparationTime(Integer maxPreparationTime) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("preparationTime"),
                        maxPreparationTime
                );
    }
}