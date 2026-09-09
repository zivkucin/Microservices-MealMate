package com.mealmate.recipe.repository;

import com.mealmate.recipe.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Ingredient> findByNameContainingIgnoreCase(String name);
}