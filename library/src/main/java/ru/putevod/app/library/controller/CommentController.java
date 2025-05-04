package ru.putevod.app.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.library.client.AuthServiceClient;
import ru.putevod.app.library.dto.CommentDto;
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
    @Operation(summary = "Получить комментарии к маршруту")
    public ResponseEntity<Page<CommentDto>> getComments(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(commentService.getRouteComments(routeId, pageable));
    }

    @PostMapping
    @Operation(summary = "Добавить комментарий к маршруту")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @RequestParam @NotBlank @Size(min = 1, max = 1000) String content,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        AuthServiceClient.UserInfo userInfo = authServiceClient.getUserInfo(token);
        Long userId = userInfo.userId();
        
        CommentDto comment = commentService.addComment(routeId, userId, content);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Обновить комментарий")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @PathVariable @Parameter(description = "ID комментария") Long commentId,
            @RequestParam @NotBlank @Size(min = 1, max = 1000) String content,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        AuthServiceClient.UserInfo userInfo = authServiceClient.getUserInfo(token);
        Long userId = userInfo.userId();
        
        CommentDto comment = commentService.updateComment(commentId, userId, content);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Удалить комментарий")
    public ResponseEntity<Void> deleteComment(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @PathVariable @Parameter(description = "ID комментария") Long commentId,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        AuthServiceClient.UserInfo userInfo = authServiceClient.getUserInfo(token);
        Long userId = userInfo.userId();
        
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "Получить комментарий по ID")
    public ResponseEntity<CommentDto> getComment(
            @PathVariable @Parameter(description = "ID маршрута") Long routeId,
            @PathVariable @Parameter(description = "ID комментария") Long commentId) {
        return ResponseEntity.ok(commentService.getComment(commentId));
    }
} 