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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.library.client.AuthServiceClient;
import ru.putevod.app.library.dto.CommentDto;
import ru.putevod.app.library.security.CurrentUser;
import ru.putevod.app.library.service.CommentService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/routes/{routeId}/comments")
@RequiredArgsConstructor
@Tag(name = "Комментарии к маршрутам", description = "API для работы с комментариями к опубликованным маршрутам")
public class CommentController {

    private final CommentService commentService;
    private final AuthServiceClient authServiceClient;

    @GetMapping
    @Operation(summary = "Получить комментарии к маршруту", description = "Возвращает список комментариев к указанному маршруту")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Комментарии успешно получены",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "404", description = "Маршрут не найден")
    })
    public ResponseEntity<Page<CommentDto>> getComments(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(commentService.getRouteComments(routeId, pageable));
    }

    @PostMapping
    @Operation(summary = "Добавить комментарий к маршруту", description = "Добавляет новый комментарий к указанному маршруту",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Комментарий успешно добавлен",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentDto.class))),
        @ApiResponse(responseCode = "400", description = "Неверные данные"),
        @ApiResponse(responseCode = "404", description = "Маршрут не найден"),
        @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public ResponseEntity<CommentDto> addComment(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @RequestParam @NotBlank @Size(min = 1, max = 1000) String content,
            @CurrentUser Long userId) {
        
        CommentDto comment = commentService.addComment(routeId, userId, content);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Обновить комментарий", description = "Обновляет существующий комментарий",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Комментарий успешно обновлен",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentDto.class))),
        @ApiResponse(responseCode = "400", description = "Неверные данные"),
        @ApiResponse(responseCode = "403", description = "Нет прав на обновление"),
        @ApiResponse(responseCode = "404", description = "Комментарий не найден"),
        @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @PathVariable @Parameter(description = "ID комментария") Long commentId,
            @RequestParam @NotBlank @Size(min = 1, max = 1000) String content,
            @CurrentUser Long userId) {
        
        CommentDto comment = commentService.updateComment(commentId, userId, content);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Удалить комментарий", description = "Удаляет комментарий к маршруту",
            security = { @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Комментарий успешно удален"),
        @ApiResponse(responseCode = "403", description = "Нет прав на удаление"),
        @ApiResponse(responseCode = "404", description = "Комментарий не найден"),
        @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public ResponseEntity<Void> deleteComment(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @PathVariable @Parameter(description = "ID комментария") Long commentId,
            @CurrentUser Long userId) {
        
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "Получить комментарий по ID", description = "Возвращает информацию о комментарии по его ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Комментарий успешно получен",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentDto.class))),
        @ApiResponse(responseCode = "404", description = "Комментарий не найден")
    })
    public ResponseEntity<CommentDto> getComment(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @PathVariable @Parameter(description = "ID комментария") Long commentId) {
        return ResponseEntity.ok(commentService.getComment(commentId));
    }
} 