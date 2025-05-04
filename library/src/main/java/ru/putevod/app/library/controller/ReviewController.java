package ru.putevod.app.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.library.client.AuthServiceClient;
import ru.putevod.app.library.dto.ReviewDto;
import ru.putevod.app.library.security.CurrentUser;
import ru.putevod.app.library.service.ReviewService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/v1/routes/{routeId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Отзывы на маршруты", description = "API для работы с отзывами на опубликованные маршруты")
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthServiceClient authServiceClient;

    @GetMapping
    @Operation(summary = "Получить отзывы на маршрут")
    public ResponseEntity<Page<ReviewDto>> getReviews(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getRouteReviews(routeId, pageable));
    }

    @PostMapping
    @Operation(summary = "Добавить отзыв к маршруту")
    public ResponseEntity<ReviewDto> addReview(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @RequestParam @NotNull @Min(1) @Max(5) Integer rating,
            @RequestParam(required = false) String comment,
            @CurrentUser Long userId) {
        
        ReviewDto review = reviewService.addOrUpdateReview(routeId, userId, rating, comment);
        return ResponseEntity.ok(review);
    }

    @PutMapping
    @Operation(summary = "Обновить отзыв к маршруту")
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @RequestParam @NotNull @Min(1) @Max(5) Integer rating,
            @RequestParam(required = false) String comment,
            @CurrentUser Long userId) {
        
        ReviewDto review = reviewService.addOrUpdateReview(routeId, userId, rating, comment);
        return ResponseEntity.ok(review);
    }

    @DeleteMapping
    @Operation(summary = "Удалить отзыв")
    public ResponseEntity<Void> deleteReview(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @CurrentUser Long userId) {
        
        reviewService.deleteReview(routeId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @Operation(summary = "Получить мой отзыв на маршрут")
    public ResponseEntity<ReviewDto> getMyReview(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @CurrentUser Long userId) {
        
        return ResponseEntity.ok(reviewService.getUserReview(routeId, userId));
    }
} 