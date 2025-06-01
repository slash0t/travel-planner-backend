package ru.putevod.app.external.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public GroupedOpenApi placesApi() {
        return GroupedOpenApi.builder()
                .group("places-api")
                .pathsToMatch("/api/v1/places/**")
                .displayName("Places API")
                .build();
    }

    @Bean
    public GroupedOpenApi aiApi() {
        return GroupedOpenApi.builder()
                .group("ai-api")
                .pathsToMatch("/api/v1/ai/**")
                .displayName("AI Services API")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> servers = new ArrayList<>();
        Server server = new Server();
        server.setUrl(contextPath);
        server.setDescription("Сервер внешних интеграций TravelPlanner");
        servers.add(server);

        return new OpenAPI()
                .servers(servers)
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