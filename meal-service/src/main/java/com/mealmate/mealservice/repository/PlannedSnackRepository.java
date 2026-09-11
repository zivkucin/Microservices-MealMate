package com.mealmate.mealservice.repository;

import com.mealmate.mealservice.entity.PlannedSnack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlannedSnackRepository extends JpaRepository<PlannedSnack, Long> {

    List<PlannedSnack> findAllByPlannedMealPlannedMealId(
            Long plannedMealId
    );

    void deleteAllByPlannedMealPlannedMealId(Long plannedMealId);
}