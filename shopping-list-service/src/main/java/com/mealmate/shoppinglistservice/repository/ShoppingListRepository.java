package com.mealmate.shoppinglistservice.repository;

import com.mealmate.shoppinglistservice.entity.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {

    Optional<ShoppingList> findByUserIdAndWeekStartDate(
            Long userId,
            LocalDate weekStartDate
    );

    boolean existsByUserIdAndWeekStartDate(
            Long userId,
            LocalDate weekStartDate
    );
}