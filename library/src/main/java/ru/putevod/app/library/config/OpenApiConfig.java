package ru.putevod.app.library.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> servers = new ArrayList<>();
        
        Server prodServer = new Server();
        prodServer.setUrl("https://www.putevod-app.ru/library/api/v1");
        prodServer.setDescription("Производственный сервер Library Service");
        servers.add(prodServer);
        
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8084/api/v1");
        localServer.setDescription("Локальный сервер для разработки");
        servers.add(localServer);
        
        return new OpenAPI()
                .servers(servers)
                .info(new Info()
                        .title("Putevod Library Service API")
                        .version("1.0")
                        .description("API для сервиса публичной библиотеки маршрутов Putevod. " +
                                    "Обеспечивает управление публичными маршрутами, отзывами " +
                                    "и административными функциями.")
                        .termsOfService("https://www.putevod-app.ru/terms/")
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0"))
                        .contact(new Contact()
                                .name("Putevod Support Team")
                                .email("support@putevod-app.ru")
                                .url("https://www.putevod-app.ru")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT токен авторизации. Формат: Bearer [token]")));
    }
    
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            if (openApi.getPaths() != null) {
                var newPaths = new io.swagger.v3.oas.models.Paths();
                openApi.getPaths().forEach((path, pathItem) -> {
                    String newPath = path.replaceFirst("^/api/v1", "");
                    if (newPath.isEmpty()) {
                        newPath = "/";
                    }
                    newPaths.addPathItem(newPath, pathItem);
                });
                openApi.setPaths(newPaths);
            }
        };
    }
    
    @RestController
    public static class SwaggerRedirectController {
        
        @GetMapping("/swagger-ui/")
        public RedirectView redirectSwaggerUiSlash() {
            return new RedirectView("/swagger-ui.html", true);
        }
        
        @GetMapping("/swagger-ui")
        public RedirectView redirectSwaggerUi() {
            return new RedirectView("/swagger-ui.html", true);
        }
    }
} 