package ru.putevod.app.library.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.putevod.app.library.entity.RouteRating;

import java.util.Optional;

@Repository
public interface RouteRatingRepository extends JpaRepository<RouteRating, Long> {

    Page<RouteRating> findByPublishedRouteId(Long publishedRouteId, Pageable pageable);

    Page<RouteRating> findByPublishedRouteIdAndIsDeletedFalse(Long publishedRouteId, Pageable pageable);

    Optional<RouteRating> findByPublishedRouteIdAndUserId(Long publishedRouteId, Long userId);

    Optional<RouteRating> findByPublishedRouteIdAndUserIdAndIsDeletedFalse(Long publishedRouteId, Long userId);

    boolean existsByPublishedRouteIdAndUserId(Long publishedRouteId, Long userId);

    boolean existsByPublishedRouteIdAndUserIdAndIsDeletedFalse(Long publishedRouteId, Long userId);

    Page<RouteRating> findByIsDeletedFalse(Pageable pageable);
} 