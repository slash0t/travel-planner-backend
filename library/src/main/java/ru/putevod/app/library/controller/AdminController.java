package ru.putevod.app.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.library.dto.ReviewDto;
import ru.putevod.app.library.service.ReviewService;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Административная панель", description = "API для администрирования библиотеки маршрутов")
public class AdminController {

    private final ReviewService reviewService;

    @GetMapping("/reviews")
    @Operation(summary = "Получить все отзывы", 
            description = "Возвращает пагинированный список всех отзывов на маршруты (требует прав администратора)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список всех отзывов успешно получен",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "403", description = "Нет прав администратора"),
        @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReviewDto>> getAllReviews(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getAllReviews(pageable));
    }

    @DeleteMapping("/reviews/{reviewId}")
    @Operation(summary = "Удалить отзыв по ID", 
            description = "Удаляет отзыв по его идентификатору (требует прав администратора)",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Отзыв успешно удален"),
        @ApiResponse(responseCode = "404", description = "Отзыв не найден"),
        @ApiResponse(responseCode = "403", description = "Нет прав администратора"),
        @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReviewById(
            @PathVariable @Parameter(description = "ID отзыва для удаления") Long reviewId) {
        reviewService.deleteReviewById(reviewId);
        return ResponseEntity.noContent().build();
    }
}