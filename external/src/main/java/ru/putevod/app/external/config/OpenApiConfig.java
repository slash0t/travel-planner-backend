package ru.putevod.app.external.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("External API Service")
                        .version("1.0")
                        .description("API для взаимодействия с внешними сервисами")
                        .contact(new Contact()
                                .name("TravelPlanner Team")
                                .email("support@travelplanner.example")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT токен авторизации. Формат: Bearer [token]")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
} 