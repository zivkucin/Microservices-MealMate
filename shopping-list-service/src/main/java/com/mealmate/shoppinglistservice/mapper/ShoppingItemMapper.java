package com.mealmate.shoppinglistservice.mapper;

import com.mealmate.shoppinglistservice.dto.AddShoppingItemRequest;
import com.mealmate.shoppinglistservice.dto.ShoppingItemResponse;
import com.mealmate.shoppinglistservice.dto.UpdateShoppingItemRequest;
import com.mealmate.shoppinglistservice.entity.ShoppingItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShoppingItemMapper {

    @Mapping(target = "shoppingItemId", ignore = true)
    @Mapping(target = "shoppingList", ignore = true)
    @Mapping(target = "purchased", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ShoppingItem toEntity(AddShoppingItemRequest request);

    @Mapping(
            target = "shoppingListId",
            source = "shoppingList.shoppingListId"
    )
    ShoppingItemResponse toResponse(ShoppingItem item);

    @Mapping(target = "shoppingItemId", ignore = true)
    @Mapping(target = "shoppingList", ignore = true)
    @Mapping(target = "purchased", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(
            UpdateShoppingItemRequest request,
            @org.mapstruct.MappingTarget ShoppingItem item
    );
}