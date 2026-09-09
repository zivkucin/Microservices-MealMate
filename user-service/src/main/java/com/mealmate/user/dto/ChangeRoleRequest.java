package com.mealmate.user.dto;

import com.mealmate.user.entity.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeRoleRequest {

    @NotNull(message = "Role is required")
    private UserRole role;
}