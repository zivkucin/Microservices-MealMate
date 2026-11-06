package com.mealmate.reviewservice.client;

import com.mealmate.reviewservice.dto.UserResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface UserClient {

    @GetExchange("/users/{id}/public-profile")
    UserResponse getUserById(@PathVariable("id") Long id);
}