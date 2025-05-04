package ru.putevod.app.library.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.putevod.app.library.entity.RouteComment;

import java.util.Optional;

@Repository
public interface RouteCommentRepository extends JpaRepository<RouteComment, Long> {
    
    Page<RouteComment> findByPublishedRouteIdAndIsDeletedFalseOrderByCreatedAtDesc(Long publishedRouteId, Pageable pageable);
    
    Optional<RouteComment> findByIdAndUserIdAndIsDeletedFalse(Long id, Long userId);
    
    long countByPublishedRouteIdAndIsDeletedFalse(Long publishedRouteId);
} 