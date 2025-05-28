package ru.putevod.app.library.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.library.exception.ResourceNotFoundException;
import ru.putevod.app.library.dto.ReviewDto;
import ru.putevod.app.library.entity.PublishedRoute;
import ru.putevod.app.library.entity.RouteRating;
import ru.putevod.app.library.repository.PublishedRouteRepository;
import ru.putevod.app.library.repository.RouteRatingRepository;
import ru.putevod.app.library.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final PublishedRouteRepository publishedRouteRepository;
    private final RouteRatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final MapperService mapperService;

    @Transactional(readOnly = true)
    public Page<ReviewDto> getRouteReviews(Long routeId, Pageable pageable) {
        // Проверка, что маршрут существует и одобрен
        if (!publishedRouteRepository.existsById(routeId)) {
            throw new ResourceNotFoundException("Route not found with id " + routeId);
        }
        
        return ratingRepository.findByPublishedRouteIdAndIsDeletedFalse(routeId, pageable)
                .map(mapperService::toReviewDto);
    }

    @Transactional
    public ReviewDto addOrUpdateReview(Long routeId, Long userId, Integer rating, String comment) {
        PublishedRoute publishedRoute = publishedRouteRepository.findByIdAndIsApprovedTrue(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id " + routeId));

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }

        Optional<RouteRating> existingRating = ratingRepository.findByPublishedRouteIdAndUserId(routeId, userId);
        
        RouteRating ratingEntity;
        if (existingRating.isPresent()) {
            ratingEntity = existingRating.get();
            ratingEntity.setRating(rating);
            ratingEntity.setComment(comment);
        } else {
            ratingEntity = RouteRating.builder()
                    .publishedRoute(publishedRoute)
                    .userId(userId)
                    .rating(rating)
                    .comment(comment)
                    .isDeleted(false)
                    .build();
        }
        
        RouteRating savedRating = ratingRepository.save(ratingEntity);
        return mapperService.toReviewDto(savedRating);
    }
    
    @Transactional
    public void deleteReview(Long routeId, Long userId) {
        if (!publishedRouteRepository.existsById(routeId)) {
            throw new ResourceNotFoundException("Route not found with id " + routeId);
        }

        RouteRating rating = ratingRepository.findByPublishedRouteIdAndUserId(routeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for route " + routeId + " and user " + userId));
        
        rating.setIsDeleted(true);
        ratingRepository.save(rating);
    }

    @Transactional(readOnly = true)
    public ReviewDto getUserReview(Long routeId, Long userId) {
        RouteRating rating = ratingRepository.findByPublishedRouteIdAndUserIdAndIsDeletedFalse(routeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for route " + routeId + " and user " + userId));
        
        return mapperService.toReviewDto(rating);
    }
    
    @Transactional(readOnly = true)
    public Page<ReviewDto> getAllReviews(Pageable pageable) {
        return ratingRepository.findByIsDeletedFalse(pageable)
                .map(mapperService::toReviewDto);
    }
    
    @Transactional
    public void deleteReviewById(Long reviewId) {
        RouteRating rating = ratingRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id " + reviewId));
        
        rating.setIsDeleted(true);
        ratingRepository.save(rating);
        log.info("Review with id {} marked as deleted by admin", reviewId);
    }
} 