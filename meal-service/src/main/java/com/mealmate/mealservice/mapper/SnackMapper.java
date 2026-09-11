package com.mealmate.mealservice.mapper;

import com.mealmate.mealservice.dto.SnackResponse;
import com.mealmate.mealservice.entity.Snack;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SnackMapper {

    SnackResponse toResponse(Snack snack);
}