package com.mealmate.mealservice.repository;

import com.mealmate.mealservice.entity.Snack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SnackRepository extends JpaRepository<Snack, Long> {

    Optional<Snack> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
