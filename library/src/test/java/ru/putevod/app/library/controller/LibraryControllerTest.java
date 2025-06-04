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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.putevod.app.library.client.AuthServiceClient;
import ru.putevod.app.library.client.PlannerClient;
import ru.putevod.app.library.dto.PublicRouteDetailDto;
import ru.putevod.app.library.dto.PublicRouteDto;
import ru.putevod.app.library.dto.RoutePreviewDto;
import ru.putevod.app.library.entity.PublishedRoute;
import ru.putevod.app.library.entity.Trip;
import ru.putevod.app.library.service.LibraryService;
import ru.putevod.app.library.dto.CopyRouteRequestDto;
import ru.putevod.app.library.dto.CopyRouteResponseDto;
import ru.putevod.app.library.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryControllerTest {

    @Mock
    private LibraryService libraryService;

    @Mock
    private PlannerClient plannerClient;

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private LibraryController libraryController;

    private Pageable pageable;
    private RoutePreviewDto routePreviewDto;
    private PublicRouteDetailDto routeDetailDto;
    private PublicRouteDto publicRouteDto;
    private Trip trip;
    private Authentication adminAuth;
    private Authentication userAuth;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 20);
        routePreviewDto = new RoutePreviewDto();
        routeDetailDto = new PublicRouteDetailDto();
        publicRouteDto = new PublicRouteDto();
        trip = new Trip();

        adminAuth = new UsernamePasswordAuthenticationToken(
            "admin", "token", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        userAuth = new UsernamePasswordAuthenticationToken(
            "user", "token", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void getRoutes_ShouldReturnPageOfRoutes() {
        Page<RoutePreviewDto> expectedPage = new PageImpl<>(Collections.singletonList(routePreviewDto));
        when(libraryService.getPublishedRoutes(any(Pageable.class))).thenReturn(expectedPage);

        ResponseEntity<Page<RoutePreviewDto>> response = libraryController.getRoutes(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPage, response.getBody());
        verify(libraryService).getPublishedRoutes(pageable);
    }

    @Test
    void getPendingRoutes_WithAdminRole_ShouldReturnPageOfRoutes() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(adminAuth);
        
        Page<RoutePreviewDto> expectedPage = new PageImpl<>(Collections.singletonList(routePreviewDto));
        when(libraryService.getPendingRoutes(any(Pageable.class))).thenReturn(expectedPage);

        ResponseEntity<Page<RoutePreviewDto>> response = libraryController.getPendingRoutes(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPage, response.getBody());
        verify(libraryService).getPendingRoutes(pageable);
    }

    @Test
    void getPendingRoutes_WithoutAdminRole_ShouldReturnForbidden() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(userAuth);

        ResponseEntity<Page<RoutePreviewDto>> response = libraryController.getPendingRoutes(pageable);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(libraryService, never()).getPendingRoutes(any(Pageable.class));
    }

    @Test
    void searchRoutes_ShouldReturnPageOfRoutes() {
        String query = "test";
        Page<RoutePreviewDto> expectedPage = new PageImpl<>(Collections.singletonList(routePreviewDto));
        when(libraryService.searchRoutes(eq(query), any(Pageable.class))).thenReturn(expectedPage);

        ResponseEntity<Page<RoutePreviewDto>> response = libraryController.searchRoutes(query, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPage, response.getBody());
        verify(libraryService).searchRoutes(query, pageable);
    }

    @Test
    void filterRoutes_ShouldReturnPageOfRoutes() {
        Page<RoutePreviewDto> expectedPage = new PageImpl<>(Collections.singletonList(routePreviewDto));
        when(libraryService.getFilteredRoutes(any(), any(), any(), any(), any(), any(Pageable.class)))
            .thenReturn(expectedPage);

        ResponseEntity<Page<RoutePreviewDto>> response = libraryController.filterRoutes(
            "country", "city", 1, 10, "tag", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPage, response.getBody());
        verify(libraryService).getFilteredRoutes("country", "city", 1, 10, "tag", pageable);
    }

    @Test
    void getPopularRoutes_ShouldReturnPageOfRoutes() {
        Page<RoutePreviewDto> expectedPage = new PageImpl<>(Collections.singletonList(routePreviewDto));
        when(libraryService.getPopularRoutes(any(Pageable.class))).thenReturn(expectedPage);

        ResponseEntity<Page<RoutePreviewDto>> response = libraryController.getPopularRoutes(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPage, response.getBody());
        verify(libraryService).getPopularRoutes(pageable);
    }

    @Test
    void getTopRatedRoutes_ShouldReturnPageOfRoutes() {
        Page<RoutePreviewDto> expectedPage = new PageImpl<>(Collections.singletonList(routePreviewDto));
        when(libraryService.getMostRatedRoutes(any(Pageable.class))).thenReturn(expectedPage);

        ResponseEntity<Page<RoutePreviewDto>> response = libraryController.getTopRatedRoutes(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPage, response.getBody());
        verify(libraryService).getMostRatedRoutes(pageable);
    }

    @Test
    void getRouteDetails_ShouldReturnRouteDetails() {
        Long routeId = 1L;
        when(libraryService.getRouteDetails(routeId)).thenReturn(routeDetailDto);

        ResponseEntity<PublicRouteDetailDto> response = libraryController.getRouteDetails(routeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(routeDetailDto, response.getBody());
        verify(libraryService).getRouteDetails(routeId);
    }

    @Test
    void getUserRoutes_ShouldReturnListOfRoutes() {
        Long userId = 1L;
        List<RoutePreviewDto> expectedRoutes = Collections.singletonList(routePreviewDto);
        when(libraryService.getUserPublishedRoutes(userId)).thenReturn(expectedRoutes);

        ResponseEntity<List<RoutePreviewDto>> response = libraryController.getUserRoutes(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedRoutes, response.getBody());
        verify(libraryService).getUserPublishedRoutes(userId);
    }

    @Test
    void publishRoute_WithValidData_ShouldPublishRoute() {
        Long tripId = 1L;
        Long userId = 1L;
        when(plannerClient.canPublishRoute(eq(tripId), eq(userId), anyString())).thenReturn(true);
        when(plannerClient.getRouteDetails(eq(tripId), anyString())).thenReturn(trip);
        when(libraryService.publishRoute(eq(trip), eq(userId))).thenReturn(publicRouteDto);

        ResponseEntity<PublicRouteDto> response = libraryController.publishRoute(tripId, userId, userAuth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(publicRouteDto, response.getBody());
        verify(plannerClient).canPublishRoute(tripId, userId, "token");
        verify(plannerClient).getRouteDetails(tripId, "token");
        verify(plannerClient).publishRoute(tripId, userId, "token", true);
        verify(libraryService).publishRoute(trip, userId);
    }

    @Test
    void publishRoute_WithInvalidTripId_ShouldReturnBadRequest() {
        Long tripId = 0L;
        Long userId = 1L;

        ResponseEntity<PublicRouteDto> response = libraryController.publishRoute(tripId, userId, userAuth);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(plannerClient, never()).canPublishRoute(any(), any(), any());
    }

    @Test
    void publishRoute_WithoutPermission_ShouldReturnBadRequest() {
        Long tripId = 1L;
        Long userId = 1L;
        when(plannerClient.canPublishRoute(eq(tripId), eq(userId), anyString())).thenReturn(false);

        ResponseEntity<PublicRouteDto> response = libraryController.publishRoute(tripId, userId, userAuth);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(plannerClient).canPublishRoute(tripId, userId, "token");
        verify(plannerClient, never()).getRouteDetails(any(), any());
    }

    @Test
    void approveRoute_WithAdminRole_ShouldApproveRoute() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(adminAuth);
        
        Long routeId = 1L;
        when(libraryService.approvePublishedRoute(routeId)).thenReturn(publicRouteDto);

        ResponseEntity<PublicRouteDto> response = libraryController.approveRoute(routeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(publicRouteDto, response.getBody());
        verify(libraryService).approvePublishedRoute(routeId);
    }

    @Test
    void approveRoute_WithoutAdminRole_ShouldReturnForbidden() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(userAuth);

        Long routeId = 1L;

        ResponseEntity<PublicRouteDto> response = libraryController.approveRoute(routeId);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(libraryService, never()).approvePublishedRoute(any());
    }

    @Test
    void deleteRoute_ShouldDeleteRoute() {
        Long routeId = 1L;
        Long userId = 1L;
        Long originalRouteId = 2L;
        PublishedRoute publishedRoute = new PublishedRoute();
        publishedRoute.setOriginalRouteId(originalRouteId);

        when(libraryService.getPublishedRouteById(routeId)).thenReturn(publishedRoute);

        ResponseEntity<Void> response = libraryController.deleteRoute(routeId, userId, userAuth);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(libraryService).deletePublishedRoute(routeId);
        verify(plannerClient).publishRoute(originalRouteId, userId, "token", false);
    }

    @Test
    void copyRoute_WithValidData_ShouldCopyRoute() {
        Long routeId = 1L;
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2024, 7, 15);
        
        CopyRouteRequestDto copyRequest = CopyRouteRequestDto.builder()
                .startDate(startDate)
                .title("Мой новый маршрут")
                .build();

        CopyRouteResponseDto expectedResponse = CopyRouteResponseDto.builder()
                .tripId(20L)
                .title("Мой новый маршрут")
                .startDate(startDate)
                .endDate(startDate.plusDays(4))
                .duration(5)
                .copiedDaysCount(2)
                .build();

        when(libraryService.copyRoute(any(), any(), any(), any()))
                .thenReturn(expectedResponse);

        ResponseEntity<CopyRouteResponseDto> response = libraryController.copyRoute(routeId, copyRequest, userId, userAuth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(libraryService).copyRoute(routeId, copyRequest, userId, "token");
    }

    @Test
    void copyRoute_WhenServiceThrowsException_ShouldPropagateException() {
        Long routeId = 1L;
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2024, 7, 15);
        
        CopyRouteRequestDto copyRequest = CopyRouteRequestDto.builder()
                .startDate(startDate)
                .title("Мой новый маршрут")
                .build();

        when(libraryService.copyRoute(any(), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Route not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> libraryController.copyRoute(routeId, copyRequest, userId, userAuth));
        verify(libraryService).copyRoute(routeId, copyRequest, userId, "token");
    }
} 
 