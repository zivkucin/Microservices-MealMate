package com.mealmate.recipe.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI recipeServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MealMate Recipe Service API")
                        .version("1.0")
                        .description("REST API for recipe management."));
    }
}
