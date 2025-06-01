package ru.putevod.app.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.putevod.app.library.dto.*;
import ru.putevod.app.library.entity.PublishedRoute;
import ru.putevod.app.library.entity.RouteRating;
import ru.putevod.app.library.entity.User;
import ru.putevod.app.library.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapperServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MapperService mapperService;

    private User mockUser;
    private PublishedRoute mockPublishedRoute;
    private RouteRating mockRouteRating;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setProfilePictureUrl("https://example.com/avatar.jpg");

        mockPublishedRoute = new PublishedRoute();
        mockPublishedRoute.setId(1L);
        mockPublishedRoute.setUserId(1L);
        mockPublishedRoute.setTitle("Test Route");
        mockPublishedRoute.setDescription("Test Description");
        mockPublishedRoute.setCountry("Test Country");
        mockPublishedRoute.setCity("Test City");
        mockPublishedRoute.setDuration(5);
        mockPublishedRoute.setAverageRating(4.5);
        mockPublishedRoute.setTags(new String[]{"tag1", "tag2"});
        mockPublishedRoute.setCreatedAt(LocalDateTime.now());
        mockPublishedRoute.setUpdatedAt(LocalDateTime.now());

        mockRouteRating = new RouteRating();
        mockRouteRating.setId(1L);
        mockRouteRating.setUserId(1L);
        mockRouteRating.setPublishedRoute(mockPublishedRoute);
        mockRouteRating.setRating(5);
        mockRouteRating.setComment("Great route!");
        mockRouteRating.setCreatedAt(LocalDateTime.now());
        mockRouteRating.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void toRoutePreviewDto_ShouldMapCorrectly() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        RoutePreviewDto dto = mapperService.toRoutePreviewDto(mockPublishedRoute);

        assertNotNull(dto);
        assertEquals(mockPublishedRoute.getId(), dto.getId());
        assertEquals(mockPublishedRoute.getTitle(), dto.getTitle());
        assertEquals(mockPublishedRoute.getDescription(), dto.getDescription());
        assertEquals(mockPublishedRoute.getDuration(), dto.getDuration());
        assertEquals(mockPublishedRoute.getAverageRating(), dto.getRating());
        assertNotNull(dto.getAuthor());
        assertEquals(mockUser.getId(), dto.getAuthor().getId());
        assertEquals(mockUser.getUsername(), dto.getAuthor().getUsername());
        assertEquals(mockUser.getProfilePictureUrl(), dto.getAuthor().getAvatarUrl());
        assertEquals(1, dto.getCountries().size());
        assertEquals(1, dto.getCities().size());
        assertEquals(2, dto.getTags().size());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getPreviewImageUrl());
    }

    @Test
    void toPublicRouteDto_ShouldMapCorrectly() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        PublicRouteDto dto = mapperService.toPublicRouteDto(mockPublishedRoute);

        assertNotNull(dto);
        assertEquals(mockPublishedRoute.getId(), dto.getId());
        assertEquals(mockPublishedRoute.getTitle(), dto.getTitle());
        assertEquals(mockPublishedRoute.getDescription(), dto.getDescription());
        assertEquals(mockPublishedRoute.getDuration(), dto.getDuration());
        assertEquals(mockPublishedRoute.getAverageRating(), dto.getRating());
        assertNotNull(dto.getAuthor());
        assertEquals(mockUser.getId(), dto.getAuthor().getId());
        assertEquals(mockUser.getUsername(), dto.getAuthor().getUsername());
        assertEquals(mockUser.getProfilePictureUrl(), dto.getAuthor().getAvatarUrl());
        assertEquals(1, dto.getCountries().size());
        assertEquals(1, dto.getCities().size());
        assertEquals(2, dto.getTags().size());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getUpdatedAt());
        assertNotNull(dto.getPreviewImageUrl());
    }

    @Test
    void toPublicRouteDetailDto_ShouldMapCorrectly() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        PublicRouteDetailDto dto = mapperService.toPublicRouteDetailDto(mockPublishedRoute);

        assertNotNull(dto);
        assertEquals(mockPublishedRoute.getId(), dto.getId());
        assertEquals(mockPublishedRoute.getTitle(), dto.getTitle());
        assertEquals(mockPublishedRoute.getDescription(), dto.getDescription());
        assertEquals(mockPublishedRoute.getDuration(), dto.getDuration());
        assertEquals(mockPublishedRoute.getAverageRating(), dto.getRating());
        assertNotNull(dto.getAuthor());
        assertEquals(mockUser.getId(), dto.getAuthor().getId());
        assertEquals(mockUser.getUsername(), dto.getAuthor().getUsername());
        assertEquals(mockUser.getProfilePictureUrl(), dto.getAuthor().getAvatarUrl());
        assertEquals(1, dto.getCountries().size());
        assertEquals(1, dto.getCities().size());
        assertEquals(2, dto.getTags().size());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getUpdatedAt());
        assertNotNull(dto.getPreviewImageUrl());
        assertNotNull(dto.getDays());
        assertTrue(dto.getDays().isEmpty());
    }

    @Test
    void toReviewDto_ShouldMapCorrectly() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        ReviewDto dto = mapperService.toReviewDto(mockRouteRating);

        assertNotNull(dto);
        assertEquals(mockRouteRating.getId(), dto.getId());
        assertEquals(mockRouteRating.getPublishedRoute().getId(), dto.getRouteId());
        assertEquals(mockRouteRating.getRating(), dto.getRating());
        assertEquals(mockRouteRating.getComment(), dto.getComment());
        assertNotNull(dto.getAuthor());
        assertEquals(mockUser.getId(), dto.getAuthor().getId());
        assertEquals(mockUser.getUsername(), dto.getAuthor().getUsername());
        assertEquals(mockUser.getProfilePictureUrl(), dto.getAuthor().getAvatarUrl());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getUpdatedAt());
    }
} 