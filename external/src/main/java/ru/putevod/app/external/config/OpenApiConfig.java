package ru.putevod.app.external.config;

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
        prodServer.setUrl("https://www.putevod-app.ru/external/api/v1");
        prodServer.setDescription("Производственный сервер External Service");
        servers.add(prodServer);

        Server localServer = new Server();
        localServer.setUrl("http://localhost:8082/api/v1");
        localServer.setDescription("Локальный сервер для разработки");
        servers.add(localServer);

        return new OpenAPI()
                .servers(servers)
                .info(new Info()
                        .title("Putevod External API Service")
                        .version("1.0")
                        .description("API для взаимодействия с внешними сервисами Putevod. " +
                                "Включает интеграции с картографическими сервисами, " +
                                "AI-сервисами для генерации маршрутов и другими внешними API.")
                        .termsOfService("https://www.putevod-app.ru/terms/")
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0"))
                        .contact(new Contact()
                                .name("Putevod Support Team")
                                .email("support@putevod-app.ru")
                                .url("https://www.putevod-app.ru")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT токен авторизации. Формат: Bearer [token]")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
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