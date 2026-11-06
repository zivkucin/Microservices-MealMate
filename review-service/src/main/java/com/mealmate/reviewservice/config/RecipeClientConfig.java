package com.mealmate.reviewservice.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class RecipeClientConfig {

    @Bean
    public RestClientHttpServiceGroupConfigurer httpServiceGroupConfigurer() {

        return groups -> groups
                .filterByName("recipeClient", "userClient")
                .forEachClient((group, clientBuilder) ->
                        clientBuilder.requestInterceptor(
                                (request, body, execution) -> {

                                    ServletRequestAttributes attributes =
                                            (ServletRequestAttributes)
                                                    RequestContextHolder.getRequestAttributes();

                                    if (attributes != null) {
                                        HttpServletRequest currentRequest =
                                                attributes.getRequest();

                                        String authorization =
                                                currentRequest.getHeader(
                                                        HttpHeaders.AUTHORIZATION
                                                );

                                        if (authorization != null) {
                                            request.getHeaders().set(
                                                    HttpHeaders.AUTHORIZATION,
                                                    authorization
                                            );
                                        }
                                    }

                                    return execution.execute(request, body);
                                }
                        )
                );
    }
}