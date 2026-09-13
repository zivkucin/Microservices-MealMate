package com.mealmate.shoppinglistservice.repository;

import com.mealmate.shoppinglistservice.entity.MeasurementUnit;
import com.mealmate.shoppinglistservice.entity.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import java.util.Optional;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {

    List<ShoppingItem> findAllByShoppingListShoppingListIdAndName(
            Long shoppingListId,
            String name
    );
}