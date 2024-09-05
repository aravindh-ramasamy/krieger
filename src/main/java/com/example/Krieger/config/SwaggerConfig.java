package com.example.Krieger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customSwaggerOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Document and Author Management Web Application").version("1.0"));
    }
}