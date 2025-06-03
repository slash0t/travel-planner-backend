package ru.putevod.app.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.library.annotation.TrackMetrics;
import ru.putevod.app.library.client.AuthServiceClient;
import ru.putevod.app.library.dto.ReviewDto;
import ru.putevod.app.library.security.CurrentUser;
import ru.putevod.app.library.service.ReviewService;

@RestController
@RequestMapping("/api/v1/routes/{routeId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Отзывы на маршруты", description = "API для работы с отзывами на опубликованные маршруты")
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthServiceClient authServiceClient;

    @GetMapping
    @TrackMetrics(value = "get_route_reviews", type = TrackMetrics.EventType.CUSTOM)
    @Operation(summary = "Получить отзывы на маршрут", description = "Возвращает список отзывов на указанный маршрут")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отзывы успешно получены",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Маршрут не найден")
    })
    public ResponseEntity<Page<ReviewDto>> getReviews(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getRouteReviews(routeId, pageable));
    }

    @PostMapping
    @TrackMetrics(value = "add_route_review", type = TrackMetrics.EventType.CUSTOM)
    @Operation(summary = "Добавить отзыв к маршруту", description = "Добавляет новый отзыв к указанному маршруту",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отзыв успешно добавлен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные отзыва"),
            @ApiResponse(responseCode = "401", description = "Неавторизованный запрос"),
            @ApiResponse(responseCode = "404", description = "Маршрут не найден"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<ReviewDto> addReview(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @RequestParam @NotNull @Min(1) @Max(5) Integer rating,
            @RequestParam(required = false) String comment,
            @CurrentUser Long userId) {

        ReviewDto review = reviewService.addOrUpdateReview(routeId, userId, rating, comment);
        return ResponseEntity.ok(review);
    }

    @PutMapping
    @TrackMetrics(value = "update_route_review", type = TrackMetrics.EventType.CUSTOM)
    @Operation(summary = "Обновить отзыв к маршруту", description = "Обновляет существующий отзыв к указанному маршруту",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отзыв успешно обновлен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные отзыва"),
            @ApiResponse(responseCode = "401", description = "Неавторизованный запрос"),
            @ApiResponse(responseCode = "403", description = "Нет прав на обновление отзыва"),
            @ApiResponse(responseCode = "404", description = "Отзыв не найден"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @RequestParam @NotNull @Min(1) @Max(5) Integer rating,
            @RequestParam(required = false) String comment,
            @CurrentUser Long userId) {

        ReviewDto review = reviewService.addOrUpdateReview(routeId, userId, rating, comment);
        return ResponseEntity.ok(review);
    }

    @DeleteMapping
    @TrackMetrics(value = "delete_route_review", type = TrackMetrics.EventType.CUSTOM)
    @Operation(summary = "Удалить отзыв", description = "Удаляет отзыв на маршрут",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Отзыв успешно удален"),
            @ApiResponse(responseCode = "401", description = "Неавторизованный запрос"),
            @ApiResponse(responseCode = "403", description = "Нет прав на удаление отзыва"),
            @ApiResponse(responseCode = "404", description = "Отзыв не найден"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<Void> deleteReview(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @CurrentUser Long userId) {

        reviewService.deleteReview(routeId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @TrackMetrics(value = "get_my_review", type = TrackMetrics.EventType.CUSTOM)
    @Operation(summary = "Получить мой отзыв на маршрут", description = "Возвращает отзыв текущего пользователя на указанный маршрут",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отзыв успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewDto.class))),
            @ApiResponse(responseCode = "401", description = "Неавторизованный запрос"),
            @ApiResponse(responseCode = "404", description = "Отзыв не найден"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    public ResponseEntity<ReviewDto> getMyReview(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @CurrentUser Long userId) {

        return ResponseEntity.ok(reviewService.getUserReview(routeId, userId));
    }


} 