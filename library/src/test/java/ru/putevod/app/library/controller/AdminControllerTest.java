package ru.putevod.app.library.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.putevod.app.library.dto.ReviewDto;
import ru.putevod.app.library.exception.ResourceNotFoundException;
import ru.putevod.app.library.service.ReviewService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private AdminController adminController;

    private Pageable pageable;
    private ReviewDto reviewDto;
    private Page<ReviewDto> reviewPage;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 20);
        reviewDto = ReviewDto.builder()
                .id(1L)
                .routeId(1L)
                .rating(5)
                .comment("Great route!")
                .build();
        reviewPage = new PageImpl<>(Collections.singletonList(reviewDto), pageable, 1);
    }

    @Test
    void getAllReviews_ShouldReturnPageOfReviews() {
        when(reviewService.getAllReviews(any(Pageable.class))).thenReturn(reviewPage);

        ResponseEntity<Page<ReviewDto>> response = adminController.getAllReviews(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(reviewDto, response.getBody().getContent().get(0));
        verify(reviewService).getAllReviews(pageable);
    }

    @Test
    void getAllReviews_WithEmptyPage_ShouldReturnEmptyPage() {
        Page<ReviewDto> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(reviewService.getAllReviews(any(Pageable.class))).thenReturn(emptyPage);

        ResponseEntity<Page<ReviewDto>> response = adminController.getAllReviews(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalElements());
        assertTrue(response.getBody().getContent().isEmpty());
        verify(reviewService).getAllReviews(pageable);
    }

    @Test
    void deleteReviewById_ShouldDeleteReview() {
        Long reviewId = 1L;
        doNothing().when(reviewService).deleteReviewById(reviewId);

        ResponseEntity<Void> response = adminController.deleteReviewById(reviewId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reviewService).deleteReviewById(reviewId);
    }

    @Test
    void deleteReviewById_WhenReviewNotFound_ShouldThrowException() {
        Long reviewId = 1L;
        doThrow(new ResourceNotFoundException("Review not found with id " + reviewId))
                .when(reviewService).deleteReviewById(reviewId);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            adminController.deleteReviewById(reviewId);
        });
        assertEquals("Review not found with id " + reviewId, exception.getMessage());
        verify(reviewService).deleteReviewById(reviewId);
    }
}