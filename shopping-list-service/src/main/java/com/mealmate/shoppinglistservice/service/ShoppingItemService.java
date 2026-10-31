package com.mealmate.shoppinglistservice.service;

import com.mealmate.shoppinglistservice.dto.AddShoppingItemRequest;
import com.mealmate.shoppinglistservice.dto.ShoppingItemResponse;
import com.mealmate.shoppinglistservice.dto.UpdateShoppingItemRequest;
import com.mealmate.shoppinglistservice.entity.MeasurementUnit;
import com.mealmate.shoppinglistservice.entity.ShoppingItem;
import com.mealmate.shoppinglistservice.entity.ShoppingList;
import com.mealmate.shoppinglistservice.exception.ShoppingItemNotFoundException;
import com.mealmate.shoppinglistservice.exception.ShoppingListNotFoundException;
import com.mealmate.shoppinglistservice.mapper.ShoppingItemMapper;
import com.mealmate.shoppinglistservice.repository.ShoppingItemRepository;
import com.mealmate.shoppinglistservice.repository.ShoppingListRepository;
import com.mealmate.shoppinglistservice.util.MeasurementUnitConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingItemService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingItemMapper shoppingItemMapper;

    public ShoppingItemResponse addShoppingItem(
            Long userId,
            Long shoppingListId,
            AddShoppingItemRequest request
    ) {

        ShoppingList shoppingList =
                findAndValidateShoppingList(userId, shoppingListId);

        var existingItems =
                shoppingItemRepository
                        .findAllByShoppingListShoppingListIdAndName(
                                shoppingListId,
                                request.name()
                        );

        var compatibleItem = existingItems.stream()
                .filter(item ->
                        MeasurementUnitConverter.areCompatible(
                                item.getUnit(),
                                request.unit()
                        )
                )
                .findFirst();

        if (compatibleItem.isPresent()) {

            ShoppingItem item = compatibleItem.get();

            double existingBaseQuantity =
                    MeasurementUnitConverter.toBaseUnit(
                            item.getQuantity(),
                            item.getUnit()
                    );

            double newBaseQuantity =
                    MeasurementUnitConverter.toBaseUnit(
                            request.quantity(),
                            request.unit()
                    );

            double totalBaseQuantity =
                    existingBaseQuantity + newBaseQuantity;

            MeasurementUnit displayUnit =
                    MeasurementUnitConverter.getDisplayUnit(
                            totalBaseQuantity,
                            request.unit()
                    );

            double displayQuantity =
                    MeasurementUnitConverter.fromBaseUnit(
                            totalBaseQuantity,
                            displayUnit
                    );

            item.setQuantity(displayQuantity);
            item.setUnit(displayUnit);
            item.setPurchased(false);

            return shoppingItemMapper.toResponse(
                    shoppingItemRepository.save(item)
            );
        }

        ShoppingItem newItem =
                shoppingItemMapper.toEntity(request);

        newItem.setShoppingList(shoppingList);

        return shoppingItemMapper.toResponse(
                shoppingItemRepository.save(newItem)
        );
    }

    public ShoppingItemResponse updateShoppingItem(
            Long userId,
            Long shoppingListId,
            Long shoppingItemId,
            UpdateShoppingItemRequest request
    ) {

        findAndValidateShoppingList(userId, shoppingListId);

        ShoppingItem item =
                shoppingItemRepository.findById(shoppingItemId)
                        .orElseThrow(() ->
                                new ShoppingItemNotFoundException(
                                        "Shopping item not found with id: "
                                                + shoppingItemId
                                )
                        );

        if (!item.getShoppingList()
                .getShoppingListId()
                .equals(shoppingListId)) {

            throw new ShoppingItemNotFoundException(
                    "Shopping item not found in shopping list with id: "
                            + shoppingListId
            );
        }

        shoppingItemMapper.updateEntity(request, item);

        return shoppingItemMapper.toResponse(
                shoppingItemRepository.save(item)
        );
    }

    public void deleteShoppingItem(
            Long userId,
            Long shoppingListId,
            Long shoppingItemId
    ) {

        findAndValidateShoppingList(userId, shoppingListId);

        ShoppingItem item =
                shoppingItemRepository.findById(shoppingItemId)
                        .orElseThrow(() ->
                                new ShoppingItemNotFoundException(
                                        "Shopping item not found with id: "
                                                + shoppingItemId
                                )
                        );

        if (!item.getShoppingList()
                .getShoppingListId()
                .equals(shoppingListId)) {

            throw new ShoppingItemNotFoundException(
                    "Shopping item not found in shopping list with id: "
                            + shoppingListId
            );
        }

        shoppingItemRepository.delete(item);
    }

    public ShoppingItemResponse updatePurchaseStatus(
            Long userId,
            Long shoppingListId,
            Long shoppingItemId,
            boolean purchased
    ) {

        findAndValidateShoppingList(userId, shoppingListId);

        ShoppingItem item =
                shoppingItemRepository.findById(shoppingItemId)
                        .orElseThrow(() ->
                                new ShoppingItemNotFoundException(
                                        "Shopping item not found with id: "
                                                + shoppingItemId
                                )
                        );

        if (!item.getShoppingList()
                .getShoppingListId()
                .equals(shoppingListId)) {

            throw new ShoppingItemNotFoundException(
                    "Shopping item not found in shopping list with id: "
                            + shoppingListId
            );
        }

        item.setPurchased(purchased);

        return shoppingItemMapper.toResponse(
                shoppingItemRepository.save(item)
        );
    }

    private ShoppingList findAndValidateShoppingList(
            Long userId,
            Long shoppingListId
    ) {

        ShoppingList shoppingList =
                shoppingListRepository.findById(shoppingListId)
                        .orElseThrow(() ->
                                new ShoppingListNotFoundException(
                                        "Shopping list not found with id: "
                                                + shoppingListId
                                )
                        );

        if (!shoppingList.getUserId().equals(userId)) {
            throw new ShoppingListNotFoundException(
                    "Shopping list not found with id: "
                            + shoppingListId
            );
        }

        return shoppingList;
    }
}