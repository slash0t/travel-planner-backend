package ru.putevod.app.library.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.library.exception.ResourceNotFoundException;
import ru.putevod.app.library.dto.CommentDto;
import ru.putevod.app.library.entity.PublishedRoute;
import ru.putevod.app.library.entity.RouteComment;
import ru.putevod.app.library.repository.PublishedRouteRepository;
import ru.putevod.app.library.repository.RouteCommentRepository;
import ru.putevod.app.library.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final PublishedRouteRepository publishedRouteRepository;
    private final RouteCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final MapperService mapperService;

    @Transactional(readOnly = true)
    public Page<CommentDto> getRouteComments(Long routeId, Pageable pageable) {
        if (!publishedRouteRepository.existsById(routeId)) {
            throw new ResourceNotFoundException("Route not found with id " + routeId);
        }
        
        return commentRepository.findByPublishedRouteIdAndIsDeletedFalseOrderByCreatedAtDesc(routeId, pageable)
                .map(mapperService::toCommentDto);
    }

    @Transactional
    public CommentDto addComment(Long routeId, Long userId, String content) {
        // Проверка существования маршрута
        PublishedRoute publishedRoute = publishedRouteRepository.findByIdAndIsApprovedTrue(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id " + routeId));
        
        // Проверка что пользователь существует
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }
        
        // Создаем новый комментарий
        RouteComment comment = RouteComment.builder()
                .publishedRoute(publishedRoute)
                .userId(userId)
                .content(content)
                .isDeleted(false)
                .build();
        
        RouteComment savedComment = commentRepository.save(comment);
        return mapperService.toCommentDto(savedComment);
    }
    
    @Transactional
    public CommentDto updateComment(Long commentId, Long userId, String content) {
        // Находим комментарий, проверяя, что он принадлежит пользователю и не удален
        RouteComment comment = commentRepository.findByIdAndUserIdAndIsDeletedFalse(commentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));
        
        // Обновляем содержимое
        comment.setContent(content);
        
        RouteComment savedComment = commentRepository.save(comment);
        return mapperService.toCommentDto(savedComment);
    }
    
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        // Находим комментарий, проверяя, что он принадлежит пользователю и не удален
        RouteComment comment = commentRepository.findByIdAndUserIdAndIsDeletedFalse(commentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));
        
        // Помечаем комментарий как удаленный (soft delete)
        comment.setIsDeleted(true);
        commentRepository.save(comment);
    }
    
    @Transactional(readOnly = true)
    public CommentDto getComment(Long commentId) {
        RouteComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));
        
        if (comment.getIsDeleted()) {
            throw new ResourceNotFoundException("Comment not found with id " + commentId);
        }
        
        return mapperService.toCommentDto(comment);
    }
} 