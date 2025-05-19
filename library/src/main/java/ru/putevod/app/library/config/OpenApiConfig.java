package ru.putevod.app.library.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Value("${server.servlet.context-path:/library}")
    private String contextPath;
    
    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> servers = new ArrayList<>();
        Server server = new Server();
        server.setUrl(contextPath);
        server.setDescription("Сервер библиотеки маршрутов TravelPlanner");
        servers.add(server);
        
        return new OpenAPI()
                .servers(servers)
                .info(new Info()
                        .title("Library Service API")
                        .version("1.0")
                        .description("API для сервиса публичных маршрутов TravelPlanner")
                        .termsOfService("https://example.com/terms/")
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0"))
                        .contact(new Contact()
                                .name("TravelPlanner Support")
                                .email("support@travelplanner.example")
                                .url("https://travelplanner.example")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
} 