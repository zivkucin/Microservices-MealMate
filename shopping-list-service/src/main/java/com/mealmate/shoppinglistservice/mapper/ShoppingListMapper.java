package com.mealmate.shoppinglistservice.mapper;

import com.mealmate.shoppinglistservice.dto.CreateShoppingListRequest;
import com.mealmate.shoppinglistservice.dto.ShoppingListResponse;
import com.mealmate.shoppinglistservice.entity.ShoppingList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ShoppingItemMapper.class)
public interface ShoppingListMapper {

    @Mapping(target = "shoppingListId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    ShoppingList toEntity(CreateShoppingListRequest request);

    ShoppingListResponse toResponse(ShoppingList shoppingList);
}