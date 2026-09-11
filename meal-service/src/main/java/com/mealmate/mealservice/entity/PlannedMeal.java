package com.mealmate.mealservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "planned_meals")
@Getter
@Setter
public class PlannedMeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long plannedMealId;

    @Column(nullable = false)
    private Integer mealSlotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "weekly_meal_plan_id",
            nullable = false
    )
    private WeeklyMealPlan weeklyMealPlan;
}