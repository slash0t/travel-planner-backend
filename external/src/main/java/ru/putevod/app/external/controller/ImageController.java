package ru.putevod.app.external.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.putevod.app.external.dto.response.UnsplashResponse;
import ru.putevod.app.external.service.ImageService;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Изображения", description = "API для работы с изображениями")
@SecurityRequirement(name = "bearerAuth")
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "Получение изображений для города",
            description = "Возвращает список качественных изображений для указанного города из Unsplash")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Изображения найдены",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnsplashResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Неавторизованный запрос"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Изображения не найдены"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/city")
    public ResponseEntity<UnsplashResponse> getCityImages(
            @Parameter(description = "Название города для поиска изображений", required = true)
            @RequestParam String city) {

        log.info("Получен запрос на поиск изображений для города: {}", city);
        UnsplashResponse response = imageService.getCityImages(city);

        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            log.info("Для города {} не найдено изображений", city);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
} 