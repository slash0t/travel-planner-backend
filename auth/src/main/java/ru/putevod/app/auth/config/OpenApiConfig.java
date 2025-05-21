package ru.putevod.app.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service API")
                        .version("1.0")
                        .description("API для сервиса аутентификации и авторизации TravelPlanner")
                        .termsOfService("https://example.com/terms/")
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0"))
                        .contact(new Contact()
                                .name("TravelPlanner Support")
                                .email("support@travelplanner.example")
                                .url("https://travelplanner.example")))
                .addServersItem(new Server().url("/").description("Базовый путь"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
    
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth-api")
                .pathsToMatch("/api/v1/**")
                .packagesToScan("ru.putevod.app.auth.controller")
                .addOpenApiCustomizer(openApi -> {
                    Paths newPaths = new Paths();
                    
                    if (openApi.getPaths() != null) {
                        for (Map.Entry<String, PathItem> entry : openApi.getPaths().entrySet()) {
                            String path = entry.getKey();
                            PathItem pathItem = entry.getValue();
                            
                            if (path.startsWith("/api/v1")) {
                                String newPath = path.replaceFirst("^/api/v1", "");
                                if (newPath.isEmpty()) {
                                    newPath = "/";
                                }
                                newPaths.addPathItem(newPath, pathItem);
                            } else {
                                newPaths.addPathItem(path, pathItem);
                            }
                        }

                        openApi.setPaths(newPaths);
                    }
                })
                .build();
    }
} 