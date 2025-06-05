package ru.putevod.app.library.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PublishedRouteTest {

    private PublishedRoute publishedRoute;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        publishedRoute = PublishedRoute.builder()
                .id(1L)
                .originalRouteId(100L)
                .userId(200L)
                .title("Test Route")
                .description("Test Description")
                .country("Test Country")
                .city("Test City")
                .duration(5)
                .coverPhotoId(300L)
                .tags(new String[]{"tag1", "tag2"})
                .isApproved(false)
                .viewCount(0)
                .createdAt(now)
                .updatedAt(now)
                .ratings(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Builder pattern creates valid PublishedRoute")
    void testBuilder() {
        assertNotNull(publishedRoute);
        assertEquals(1L, publishedRoute.getId());
        assertEquals(100L, publishedRoute.getOriginalRouteId());
        assertEquals(200L, publishedRoute.getUserId());
        assertEquals("Test Route", publishedRoute.getTitle());
        assertEquals("Test Description", publishedRoute.getDescription());
        assertEquals("Test Country", publishedRoute.getCountry());
        assertEquals("Test City", publishedRoute.getCity());
        assertEquals(5, publishedRoute.getDuration());
        assertEquals(300L, publishedRoute.getCoverPhotoId());
        assertArrayEquals(new String[]{"tag1", "tag2"}, publishedRoute.getTags());
        assertFalse(publishedRoute.getIsApproved());
        assertEquals(0, publishedRoute.getViewCount());
        assertEquals(now, publishedRoute.getCreatedAt());
        assertEquals(now, publishedRoute.getUpdatedAt());
        assertNotNull(publishedRoute.getRatings());
        assertTrue(publishedRoute.getRatings().isEmpty());
    }

    @Test
    @DisplayName("Setters and getters work correctly")
    void testSettersAndGetters() {
        publishedRoute.setId(2L);
        publishedRoute.setTitle("Updated Title");
        publishedRoute.setDescription("Updated Description");
        publishedRoute.setIsApproved(true);
        publishedRoute.setViewCount(10);

        assertEquals(2L, publishedRoute.getId());
        assertEquals("Updated Title", publishedRoute.getTitle());
        assertEquals("Updated Description", publishedRoute.getDescription());
        assertTrue(publishedRoute.getIsApproved());
        assertEquals(10, publishedRoute.getViewCount());
    }

    @Test
    @DisplayName("getAverageRating returns correct average when ratings exist")
    void testGetAverageRating_WithRatings() {
        RouteRating rating1 = new RouteRating();
        rating1.setRating(4);
        RouteRating rating2 = new RouteRating();
        rating2.setRating(5);
        RouteRating rating3 = new RouteRating();
        rating3.setRating(3);

        List<RouteRating> ratings = new ArrayList<>();
        ratings.add(rating1);
        ratings.add(rating2);
        ratings.add(rating3);
        publishedRoute.setRatings(ratings);

        assertEquals(4.0, publishedRoute.getAverageRating());
    }

    @Test
    @DisplayName("getAverageRating returns 0.0 when no ratings exist")
    void testGetAverageRating_NoRatings() {
        publishedRoute.setRatings(new ArrayList<>());
        assertEquals(0.0, publishedRoute.getAverageRating());

        publishedRoute.setRatings(null);
        assertEquals(0.0, publishedRoute.getAverageRating());
    }

    @Test
    @DisplayName("getReviewsCount returns correct count")
    void testGetReviewsCount() {
        List<RouteRating> ratings = new ArrayList<>();
        ratings.add(new RouteRating());
        ratings.add(new RouteRating());
        publishedRoute.setRatings(ratings);
        assertEquals(2, publishedRoute.getReviewsCount());

        publishedRoute.setRatings(new ArrayList<>());
        assertEquals(0, publishedRoute.getReviewsCount());

        publishedRoute.setRatings(null);
        assertEquals(0, publishedRoute.getReviewsCount());
    }

    @Test
    @DisplayName("NoArgsConstructor creates valid PublishedRoute")
    void testNoArgsConstructor() {
        PublishedRoute emptyRoute = new PublishedRoute();
        assertNotNull(emptyRoute);
        assertNull(emptyRoute.getId());
        assertNull(emptyRoute.getTitle());
        assertNull(emptyRoute.getDescription());
        assertNull(emptyRoute.getCountry());
        assertNull(emptyRoute.getCity());
        assertNull(emptyRoute.getDuration());
        assertNull(emptyRoute.getCoverPhotoId());
        assertNull(emptyRoute.getTags());
        assertNull(emptyRoute.getIsApproved());
        assertNull(emptyRoute.getViewCount());
        assertNull(emptyRoute.getCreatedAt());
        assertNull(emptyRoute.getUpdatedAt());
        assertNotNull(emptyRoute.getRatings());
        assertTrue(emptyRoute.getRatings().isEmpty());
    }
} 