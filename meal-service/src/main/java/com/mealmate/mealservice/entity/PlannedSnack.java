package com.mealmate.mealservice.entity;

import com.mealmate.mealservice.enums.MeasurementUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "planned_snacks")
@Getter
@Setter
public class PlannedSnack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long plannedSnackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "planned_meal_id",
            nullable = false
    )
    private PlannedMeal plannedMeal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "snack_id",
            nullable = false
    )
    private Snack snack;

    @Column(nullable = false)
    private Double quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeasurementUnit unit;
}