package ru.putevod.app.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import ru.putevod.app.library.dto.ReviewDto;
import ru.putevod.app.library.entity.PublishedRoute;
import ru.putevod.app.library.entity.RouteRating;
import ru.putevod.app.library.entity.User;
import ru.putevod.app.library.exception.ResourceNotFoundException;
import ru.putevod.app.library.repository.PublishedRouteRepository;
import ru.putevod.app.library.repository.RouteRatingRepository;
import ru.putevod.app.library.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private RouteRatingRepository ratingRepository;

    @Mock
    private PublishedRouteRepository publishedRouteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MapperService mapperService;

    @InjectMocks
    private ReviewService reviewService;

    private PublishedRoute publishedRoute;
    private User user;
    private RouteRating routeRating;
    private ReviewDto reviewDto;
    private Long routeId;
    private Long userId;
    private UUID reviewUuid;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        routeId = 1L;
        userId = 100L;
        reviewUuid = UUID.randomUUID();
        pageable = PageRequest.of(0, 10);

        publishedRoute = new PublishedRoute();
        publishedRoute.setId(routeId);
        publishedRoute.setIsApproved(true);

        user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        routeRating = new RouteRating();
        routeRating.setId(500L);
        routeRating.setPublishedRoute(publishedRoute);
        routeRating.setUserId(userId);
        routeRating.setRating(4);

        reviewDto = new ReviewDto();
        reviewDto.setId(reviewUuid);
        reviewDto.setRouteId(convertToUuid(routeId));
        reviewDto.setRating(4);
        reviewDto.setComment("DTO comment");
    }

    private UUID convertToUuid(Long id) {
        if (id == null) return null;
        return UUID.nameUUIDFromBytes(id.toString().getBytes());
    }

    @Test
    @DisplayName("addOrUpdateReview - Add New Review - Success")
    void testAddOrUpdateReview_AddNew_Success() {
        Integer newRating = 5;
        String newComment = "Excellent!";

        when(publishedRouteRepository.findByIdAndIsApprovedTrue(routeId)).thenReturn(Optional.of(publishedRoute));
        when(userRepository.existsById(userId)).thenReturn(true);
        when(ratingRepository.findByPublishedRouteIdAndUserId(routeId, userId)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(RouteRating.class))).thenAnswer(invocation -> {
            RouteRating saved = invocation.getArgument(0);
            saved.setId(501L);
            return saved;
        });
        when(mapperService.toReviewDto(any(RouteRating.class))).thenAnswer(invocation -> {
             RouteRating saved = invocation.getArgument(0);
             reviewDto.setId(convertToUuid(saved.getId()));
             reviewDto.setRating(saved.getRating());
             return reviewDto;
        });

        ReviewDto result = reviewService.addOrUpdateReview(routeId, userId, newRating, newComment);

        assertNotNull(result);
        assertEquals(newRating, result.getRating());
        assertNotEquals(reviewUuid, result.getId());

        ArgumentCaptor<RouteRating> ratingCaptor = ArgumentCaptor.forClass(RouteRating.class);
        verify(publishedRouteRepository).findByIdAndIsApprovedTrue(routeId);
        verify(userRepository).existsById(userId);
        verify(ratingRepository).findByPublishedRouteIdAndUserId(routeId, userId);
        verify(ratingRepository).save(ratingCaptor.capture());
        verify(mapperService).toReviewDto(any(RouteRating.class));

        RouteRating capturedRating = ratingCaptor.getValue();
        assertNotNull(capturedRating);
        assertEquals(newRating, capturedRating.getRating());
        assertEquals(publishedRoute, capturedRating.getPublishedRoute());
        assertEquals(userId, capturedRating.getUserId());
    }

    @Test
    @DisplayName("addOrUpdateReview - Update Existing Review - Success")
    void testAddOrUpdateReview_UpdateExisting_Success() {
        when(publishedRouteRepository.findByIdAndIsApprovedTrue(routeId)).thenReturn(Optional.of(publishedRoute));
        when(userRepository.existsById(userId)).thenReturn(true);
        when(ratingRepository.findByPublishedRouteIdAndUserId(routeId, userId)).thenReturn(Optional.of(routeRating));
        when(ratingRepository.save(any(RouteRating.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapperService.toReviewDto(any(RouteRating.class))).thenAnswer(invocation -> {
             RouteRating saved = invocation.getArgument(0);
             reviewDto.setRating(saved.getRating());
             reviewDto.setId(reviewUuid);
             return reviewDto;
        });

        ReviewDto result = reviewService.addOrUpdateReview(routeId, userId, 3, "It was okay.");

        assertNotNull(result);
        assertEquals(3, result.getRating());
        assertEquals(reviewUuid, result.getId());
        assertEquals(3, routeRating.getRating());

        verify(publishedRouteRepository).findByIdAndIsApprovedTrue(routeId);
        verify(userRepository).existsById(userId);
        verify(ratingRepository).findByPublishedRouteIdAndUserId(routeId, userId);
        verify(ratingRepository).save(routeRating);
        verify(mapperService).toReviewDto(routeRating);
    }

    @Test
    @DisplayName("addOrUpdateReview - Route Not Found")
    void testAddOrUpdateReview_RouteNotFound() {
        when(publishedRouteRepository.findByIdAndIsApprovedTrue(routeId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.addOrUpdateReview(routeId, userId, 5, "comment");
        });
        assertEquals("Route not found with id " + routeId, exception.getMessage());
        verify(publishedRouteRepository).findByIdAndIsApprovedTrue(routeId);
        verify(userRepository, never()).existsById(anyLong());
        verify(ratingRepository, never()).findByPublishedRouteIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("addOrUpdateReview - User Not Found")
    void testAddOrUpdateReview_UserNotFound() {
        when(publishedRouteRepository.findByIdAndIsApprovedTrue(routeId)).thenReturn(Optional.of(publishedRoute));
        when(userRepository.existsById(userId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.addOrUpdateReview(routeId, userId, 5, "comment");
        });
        assertEquals("User not found with id " + userId, exception.getMessage());
        verify(publishedRouteRepository).findByIdAndIsApprovedTrue(routeId);
        verify(userRepository).existsById(userId);
        verify(ratingRepository, never()).findByPublishedRouteIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("deleteReview - Success")
    void testDeleteReview_Success() {
        // Arrange
        when(publishedRouteRepository.existsById(routeId)).thenReturn(true);
        when(ratingRepository.existsByPublishedRouteIdAndUserId(routeId, userId)).thenReturn(true);
        doNothing().when(ratingRepository).deleteByPublishedRouteIdAndUserId(routeId, userId);

        // Act
        assertDoesNotThrow(() -> {
            reviewService.deleteReview(routeId, userId);
        });

        // Assert
        verify(publishedRouteRepository).existsById(routeId);
        verify(ratingRepository).existsByPublishedRouteIdAndUserId(routeId, userId);
        verify(ratingRepository).deleteByPublishedRouteIdAndUserId(routeId, userId);
    }

    @Test
    @DisplayName("deleteReview - Route Not Found")
    void testDeleteReview_RouteNotFound() {
        // Arrange
        when(publishedRouteRepository.existsById(routeId)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.deleteReview(routeId, userId);
        });
        assertEquals("Route not found with id " + routeId, exception.getMessage());
        verify(publishedRouteRepository).existsById(routeId);
        verify(ratingRepository, never()).existsByPublishedRouteIdAndUserId(anyLong(), anyLong());
        verify(ratingRepository, never()).deleteByPublishedRouteIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("deleteReview - Review Not Found")
    void testDeleteReview_ReviewNotFound() {
        // Arrange
        when(publishedRouteRepository.existsById(routeId)).thenReturn(true);
        when(ratingRepository.existsByPublishedRouteIdAndUserId(routeId, userId)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.deleteReview(routeId, userId);
        });
        assertEquals("Review not found for route " + routeId + " and user " + userId, exception.getMessage());
        verify(publishedRouteRepository).existsById(routeId);
        verify(ratingRepository).existsByPublishedRouteIdAndUserId(routeId, userId);
        verify(ratingRepository, never()).deleteByPublishedRouteIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("getRouteReviews - Success")
    void testGetRouteReviews_Success() {
        // Arrange
        Page<RouteRating> ratingPage = new PageImpl<>(Collections.singletonList(routeRating), pageable, 1);
        when(publishedRouteRepository.existsById(routeId)).thenReturn(true);
        when(ratingRepository.findByPublishedRouteId(routeId, pageable)).thenReturn(ratingPage);
        when(mapperService.toReviewDto(any(RouteRating.class))).thenReturn(reviewDto);

        // Act
        Page<ReviewDto> result = reviewService.getRouteReviews(routeId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(reviewDto.getId(), result.getContent().get(0).getId());
        verify(publishedRouteRepository).existsById(routeId);
        verify(ratingRepository).findByPublishedRouteId(routeId, pageable);
        verify(mapperService).toReviewDto(routeRating);
    }

    @Test
    @DisplayName("getRouteReviews - Route Not Found")
    void testGetRouteReviews_RouteNotFound() {
        // Arrange
        when(publishedRouteRepository.existsById(routeId)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.getRouteReviews(routeId, pageable);
        });
        assertEquals("Route not found with id " + routeId, exception.getMessage());
        verify(publishedRouteRepository).existsById(routeId);
        verify(ratingRepository, never()).findByPublishedRouteId(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("getUserReview - Success")
    void testGetUserReview_Success() {
        // Arrange
        when(ratingRepository.findByPublishedRouteIdAndUserId(routeId, userId)).thenReturn(Optional.of(routeRating));
        when(mapperService.toReviewDto(routeRating)).thenReturn(reviewDto);

        // Act
        ReviewDto result = reviewService.getUserReview(routeId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(reviewDto.getId(), result.getId());
        assertEquals(reviewDto.getRating(), result.getRating());
        verify(ratingRepository).findByPublishedRouteIdAndUserId(routeId, userId);
        verify(mapperService).toReviewDto(routeRating);
    }

    @Test
    @DisplayName("getUserReview - Review Not Found")
    void testGetUserReview_ReviewNotFound() {
        // Arrange
        when(ratingRepository.findByPublishedRouteIdAndUserId(routeId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.getUserReview(routeId, userId);
        });
        assertEquals("Review not found for route " + routeId + " and user " + userId, exception.getMessage());
        verify(ratingRepository).findByPublishedRouteIdAndUserId(routeId, userId);
        verify(mapperService, never()).toReviewDto(any());
    }
} 