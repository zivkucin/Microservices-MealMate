package com.mealmate.mealservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "planned_recipes")
@Getter
@Setter
public class PlannedRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long plannedRecipeId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "planned_meal_id",
            nullable = false,
            unique = true
    )
    private PlannedMeal plannedMeal;

    @Column(nullable = false)
    private Long recipeId;

    @Column(nullable = false)
    private Integer quantity;
}
