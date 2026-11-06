package com.mealmate.reviewservice;

import com.mealmate.reviewservice.client.RecipeClient;
import com.mealmate.reviewservice.client.UserClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.service.registry.ImportHttpServices;

@SpringBootApplication
@ImportHttpServices(
        group = "recipeClient",
        types = RecipeClient.class
)
@ImportHttpServices(
        group = "userClient",
        types = UserClient.class
)
public class ReviewServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
    }
}