package ru.putevod.app.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.putevod.app.library.dto.RoutePreviewDto;
import ru.putevod.app.library.dto.PublicRouteDetailDto;
import ru.putevod.app.library.dto.PublicRouteDto;
import ru.putevod.app.library.entity.PublishedRoute;
import ru.putevod.app.library.entity.Trip;
import ru.putevod.app.library.entity.User;
import ru.putevod.app.library.repository.PublishedRouteRepository;
import ru.putevod.app.library.repository.UserRepository;
import ru.putevod.app.library.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock
    private PublishedRouteRepository publishedRouteRepository;

    @Mock
    private UserRepository userRepository; // Assuming this might be needed later based on service code

    @Mock
    private MapperService mapperService;

    @InjectMocks
    private LibraryService libraryService;

    private Pageable pageable;
    private PublishedRoute publishedRoute;
    private RoutePreviewDto routePreviewDto;
    private PublicRouteDetailDto routeDetailDto;
    private PublicRouteDto publicRouteDto;
    private UUID routeUuid;
    private User user;
    private Trip trip;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
        routeUuid = UUID.randomUUID();

        user = new User();
        user.setId(100L);
        user.setUsername("testuser");

        trip = new Trip();
        trip.setId(50L);
        trip.setTitle("Original Trip Title");
        trip.setDescription("Original description");
        trip.setCountry("Testland");
        trip.setCity("Testville");
        trip.setStartDate(LocalDate.now());
        trip.setEndDate(LocalDate.now().plusDays(4));

        publishedRoute = new PublishedRoute();
        publishedRoute.setId(1L);
        publishedRoute.setOriginalRouteId(trip.getId());
        publishedRoute.setUserId(user.getId());
        publishedRoute.setTitle("Test Route");
        publishedRoute.setViewCount(10);
        publishedRoute.setIsApproved(false);

        routePreviewDto = new RoutePreviewDto();
        routePreviewDto.setId(routeUuid);

        routeDetailDto = new PublicRouteDetailDto();
        routeDetailDto.setId(routeUuid);
        routeDetailDto.setTitle("Test Route Detail");

        publicRouteDto = new PublicRouteDto();
        publicRouteDto.setId(routeUuid);
        publicRouteDto.setOriginalRouteId(convertToUuid(trip.getId()));
        publicRouteDto.setTitle(publishedRoute.getTitle());
    }

    private UUID convertToUuid(Long id) {
        if (id == null) return null;
        return UUID.nameUUIDFromBytes(id.toString().getBytes());
    }

    @Test
    @DisplayName("getPublishedRoutes - Success")
    void testGetPublishedRoutes_Success() {
        // Arrange
        Page<PublishedRoute> routePage = new PageImpl<>(Collections.singletonList(publishedRoute), pageable, 1);
        when(publishedRouteRepository.findAllApproved(pageable)).thenReturn(routePage);
        when(mapperService.toRoutePreviewDto(any(PublishedRoute.class))).thenReturn(routePreviewDto);

        // Act
        Page<RoutePreviewDto> result = libraryService.getPublishedRoutes(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(routePreviewDto.getId(), result.getContent().get(0).getId());
        verify(publishedRouteRepository).findAllApproved(pageable);
        verify(mapperService).toRoutePreviewDto(publishedRoute);
    }

    @Test
    @DisplayName("getRouteDetails - Success")
    void testGetRouteDetails_Success() {
        // Arrange
        Long routeId = 1L;
        when(publishedRouteRepository.findByIdAndIsApprovedTrue(routeId)).thenReturn(Optional.of(publishedRoute));
        when(mapperService.toPublicRouteDetailDto(any(PublishedRoute.class))).thenReturn(routeDetailDto);

        // Act
        PublicRouteDetailDto result = libraryService.getRouteDetails(routeId);

        // Assert
        assertNotNull(result);
        assertEquals(routeDetailDto.getId(), result.getId());
        assertEquals(routeDetailDto.getTitle(), result.getTitle());
        verify(publishedRouteRepository).findByIdAndIsApprovedTrue(routeId);
        verify(publishedRouteRepository).save(publishedRoute);
        assertEquals(11, publishedRoute.getViewCount());
        verify(mapperService).toPublicRouteDetailDto(publishedRoute);
    }

    @Test
    @DisplayName("getRouteDetails - Not Found")
    void testGetRouteDetails_NotFound() {
        // Arrange
        Long routeId = 99L;
        when(publishedRouteRepository.findByIdAndIsApprovedTrue(routeId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            libraryService.getRouteDetails(routeId);
        });
        assertEquals("Route not found with id " + routeId, exception.getMessage());
        verify(publishedRouteRepository).findByIdAndIsApprovedTrue(routeId);
    }

    @Test
    @DisplayName("publishRoute - Success")
    void testPublishRoute_Success() {
        // Arrange
        when(publishedRouteRepository.existsByOriginalRouteId(trip.getId())).thenReturn(false);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(publishedRouteRepository.save(any(PublishedRoute.class))).thenAnswer(invocation -> {
            PublishedRoute routeToSave = invocation.getArgument(0);
            routeToSave.setId(2L);
            return routeToSave;
        });
        when(mapperService.toPublicRouteDto(any(PublishedRoute.class))).thenAnswer(invocation -> {
            PublishedRoute savedRoute = invocation.getArgument(0);
            publicRouteDto.setId(convertToUuid(savedRoute.getId()));
            publicRouteDto.setTitle(savedRoute.getTitle());
            return publicRouteDto;
        });

        // Act
        PublicRouteDto result = libraryService.publishRoute(trip, user.getId());

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(trip.getTitle(), result.getTitle());
        assertEquals(convertToUuid(trip.getId()), result.getOriginalRouteId());

        verify(publishedRouteRepository).existsByOriginalRouteId(trip.getId());
        verify(userRepository).findById(user.getId());
        verify(publishedRouteRepository).save(any(PublishedRoute.class));
        verify(mapperService).toPublicRouteDto(any(PublishedRoute.class));
    }

    @Test
    @DisplayName("publishRoute - User Not Found")
    void testPublishRoute_UserNotFound() {
        // Arrange
        Long nonExistentUserId = 999L;
        when(publishedRouteRepository.existsByOriginalRouteId(trip.getId())).thenReturn(false);
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            libraryService.publishRoute(trip, nonExistentUserId);
        });
        assertEquals("User not found with id " + nonExistentUserId, exception.getMessage());
        verify(publishedRouteRepository).existsByOriginalRouteId(trip.getId());
        verify(userRepository).findById(nonExistentUserId);
    }

    @Test
    @DisplayName("publishRoute - Already Exists")
    void testPublishRoute_AlreadyExists() {
        // Arrange
        when(publishedRouteRepository.existsByOriginalRouteId(trip.getId())).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            libraryService.publishRoute(trip, user.getId());
        });
        assertEquals("Route is already published", exception.getMessage());
        verify(publishedRouteRepository).existsByOriginalRouteId(trip.getId());
    }

    @Test
    @DisplayName("approvePublishedRoute - Success")
    void testApprovePublishedRoute_Success() {
        // Arrange
        Long routeId = 1L;
        publishedRoute.setIsApproved(false); // Ensure it's initially not approved
        when(publishedRouteRepository.findById(routeId)).thenReturn(Optional.of(publishedRoute));
        when(publishedRouteRepository.save(any(PublishedRoute.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapperService.toPublicRouteDto(any(PublishedRoute.class))).thenAnswer(invocation -> {
             PublishedRoute savedRoute = invocation.getArgument(0);
             publicRouteDto.setId(convertToUuid(savedRoute.getId())); 
             // Update DTO based on saved route if necessary
             return publicRouteDto;
        });

        // Act
        PublicRouteDto result = libraryService.approvePublishedRoute(routeId);

        // Assert
        assertNotNull(result);
        assertTrue(publishedRoute.getIsApproved()); // Check that the flag is set to true
        verify(publishedRouteRepository).findById(routeId);
        verify(publishedRouteRepository).save(publishedRoute);
        verify(mapperService).toPublicRouteDto(publishedRoute);
    }

    @Test
    @DisplayName("approvePublishedRoute - Not Found")
    void testApprovePublishedRoute_NotFound() {
        // Arrange
        Long routeId = 99L;
        when(publishedRouteRepository.findById(routeId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            libraryService.approvePublishedRoute(routeId);
        });
        assertEquals("Published route not found with id " + routeId, exception.getMessage());
        verify(publishedRouteRepository).findById(routeId);
    }

    @Test
    @DisplayName("deletePublishedRoute - Success")
    void testDeletePublishedRoute_Success() {
        // Arrange
        Long routeId = 1L;
        when(publishedRouteRepository.findById(routeId)).thenReturn(Optional.of(publishedRoute));

        // Act
        libraryService.deletePublishedRoute(routeId);

        // Assert
        verify(publishedRouteRepository).findById(routeId);
        verify(publishedRouteRepository).delete(publishedRoute);
    }

    @Test
    @DisplayName("deletePublishedRoute - Not Found")
    void testDeletePublishedRoute_NotFound() {
        // Arrange
        Long routeId = 99L;
        when(publishedRouteRepository.findById(routeId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            libraryService.deletePublishedRoute(routeId);
        });
        assertEquals("Published route not found with id " + routeId, exception.getMessage());
        verify(publishedRouteRepository).findById(routeId);
    }

    @Test
    @DisplayName("searchRoutes - Success")
    void testSearchRoutes_Success() {
        // Arrange
        String query = "Italy";
        Page<PublishedRoute> routePage = new PageImpl<>(Collections.singletonList(publishedRoute), pageable, 1);
        when(publishedRouteRepository.searchByQuery(query, pageable)).thenReturn(routePage);
        when(mapperService.toRoutePreviewDto(any(PublishedRoute.class))).thenReturn(routePreviewDto);

        // Act
        Page<RoutePreviewDto> result = libraryService.searchRoutes(query, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(routePreviewDto.getId(), result.getContent().get(0).getId());
        verify(publishedRouteRepository).searchByQuery(query, pageable);
        verify(mapperService).toRoutePreviewDto(publishedRoute);
    }

    @Test
    @DisplayName("getFilteredRoutes - Success")
    void testGetFilteredRoutes_Success() {
        // Arrange
        String country = "Italy";
        String city = "Rome";
        Integer durationMin = 3;
        Integer durationMax = 10;
        String tag = "culture";
        Page<PublishedRoute> routePage = new PageImpl<>(Collections.singletonList(publishedRoute), pageable, 1);
        when(publishedRouteRepository.findWithFilters(country, city, durationMin, durationMax, tag, pageable)).thenReturn(routePage);
        when(mapperService.toRoutePreviewDto(any(PublishedRoute.class))).thenReturn(routePreviewDto);

        // Act
        Page<RoutePreviewDto> result = libraryService.getFilteredRoutes(country, city, durationMin, durationMax, tag, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(routePreviewDto.getId(), result.getContent().get(0).getId());
        verify(publishedRouteRepository).findWithFilters(country, city, durationMin, durationMax, tag, pageable);
        verify(mapperService).toRoutePreviewDto(publishedRoute);
    }

    @Test
    @DisplayName("getPopularRoutes - Success")
    void testGetPopularRoutes_Success() {
        // Arrange
        Page<PublishedRoute> routePage = new PageImpl<>(Collections.singletonList(publishedRoute), pageable, 1);
        when(publishedRouteRepository.findPopular(pageable)).thenReturn(routePage);
        when(mapperService.toRoutePreviewDto(any(PublishedRoute.class))).thenReturn(routePreviewDto);

        // Act
        Page<RoutePreviewDto> result = libraryService.getPopularRoutes(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(routePreviewDto.getId(), result.getContent().get(0).getId());
        verify(publishedRouteRepository).findPopular(pageable);
        verify(mapperService).toRoutePreviewDto(publishedRoute);
    }

    @Test
    @DisplayName("getMostRatedRoutes - Success")
    void testGetMostRatedRoutes_Success() {
        // Arrange
        Page<PublishedRoute> routePage = new PageImpl<>(Collections.singletonList(publishedRoute), pageable, 1);
        when(publishedRouteRepository.findMostRated(pageable)).thenReturn(routePage);
        when(mapperService.toRoutePreviewDto(any(PublishedRoute.class))).thenReturn(routePreviewDto);

        // Act
        Page<RoutePreviewDto> result = libraryService.getMostRatedRoutes(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(routePreviewDto.getId(), result.getContent().get(0).getId());
        verify(publishedRouteRepository).findMostRated(pageable);
        verify(mapperService).toRoutePreviewDto(publishedRoute);
    }

    @Test
    @DisplayName("getUserPublishedRoutes - Success")
    void testGetUserPublishedRoutes_Success() {
        // Arrange
        Long userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(publishedRouteRepository.findByUserIdAndIsApprovedTrue(userId)).thenReturn(Collections.singletonList(publishedRoute));
        when(mapperService.toRoutePreviewDto(any(PublishedRoute.class))).thenReturn(routePreviewDto);

        // Act
        List<RoutePreviewDto> result = libraryService.getUserPublishedRoutes(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(routePreviewDto.getId(), result.get(0).getId());
        verify(userRepository).findById(userId);
        verify(publishedRouteRepository).findByUserIdAndIsApprovedTrue(userId);
        verify(mapperService).toRoutePreviewDto(publishedRoute);
    }

    @Test
    @DisplayName("getUserPublishedRoutes - User Not Found")
    void testGetUserPublishedRoutes_UserNotFound() {
        // Arrange
        Long nonExistentUserId = 999L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            libraryService.getUserPublishedRoutes(nonExistentUserId);
        });
        assertEquals("User not found with id " + nonExistentUserId, exception.getMessage());
        verify(userRepository).findById(nonExistentUserId);
    }
} 