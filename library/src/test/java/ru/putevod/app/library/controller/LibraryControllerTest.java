package ru.putevod.app.library.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // For security mocking
import org.springframework.test.web.servlet.MockMvc;
import ru.putevod.app.library.client.AuthServiceClient;
import ru.putevod.app.library.client.PlannerClient;
import ru.putevod.app.library.dto.RoutePreviewDto;
import ru.putevod.app.library.dto.PublicRouteDetailDto;
import ru.putevod.app.library.dto.PublicRouteDto;
import ru.putevod.app.library.entity.Trip;
import ru.putevod.app.library.security.CurrentUser;
import ru.putevod.app.library.service.LibraryService;
import ru.putevod.app.library.client.AuthServiceClient.UserInfo; // Import UserInfo

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LibraryController.class)
class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LibraryService libraryService;

    @MockBean
    private PlannerClient plannerClient; // Mocking client dependency

    @MockBean
    private AuthServiceClient authServiceClient; // Mocking client dependency

    private RoutePreviewDto routePreviewDto;
    private PublicRouteDetailDto routeDetailDto;
    private PublicRouteDto publicRouteDto;
    private UUID routeUuid;
    private Trip trip;

    @BeforeEach
    void setUp() {
        routeUuid = UUID.randomUUID();
        routePreviewDto = new RoutePreviewDto();
        routePreviewDto.setId(routeUuid);
        routePreviewDto.setTitle("Test Route Preview");

        routeDetailDto = new PublicRouteDetailDto();
        routeDetailDto.setId(routeUuid);
        routeDetailDto.setTitle("Test Route Detail");

        publicRouteDto = new PublicRouteDto();
        publicRouteDto.setId(routeUuid);
        publicRouteDto.setTitle("Published Route Title");

        trip = new Trip();
        trip.setId(1L);
        trip.setTitle("Trip Title");
        // Populate other necessary fields for the DTOs
    }

    @Test
    @DisplayName("GET /api/v1/routes - Success")
    @WithMockUser // Simulate an authenticated user (adjust roles/details if needed)
    void testGetRoutes_Success() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20); // Default pageable from controller
        Page<RoutePreviewDto> routePage = new PageImpl<>(Collections.singletonList(routePreviewDto), pageable, 1);
        when(libraryService.getPublishedRoutes(any(Pageable.class))).thenReturn(routePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/routes")
                .param("page", "0")
                .param("size", "20")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(routeUuid.toString()))
                .andExpect(jsonPath("$.content[0].title").value(routePreviewDto.getTitle()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(20));
    }

    @Test
    @DisplayName("GET /api/v1/routes/{id} - Success")
    @WithMockUser
    void testGetRouteDetails_Success() throws Exception {
        // Arrange
        Long routeId = 1L;
        when(libraryService.getRouteDetails(routeId)).thenReturn(routeDetailDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/routes/{id}", routeId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(routeUuid.toString()))
                .andExpect(jsonPath("$.title").value(routeDetailDto.getTitle()));
                // Add more assertions for detail fields
    }

    @Test
    @DisplayName("POST /api/v1/routes/publish/{tripId} - Success")
    @WithMockUser(username="100", authorities={"ROLE_USER"}, password="mockPassword")
    void testPublishRoute_Success() throws Exception {
        // Arrange
        Long tripId = 50L;
        String mockToken = "mockPassword"; // Token from @WithMockUser password
        Long expectedUserId = 100L; // ID from @WithMockUser username

        // Mock AuthServiceClient
        UserInfo mockUserInfo = new UserInfo(expectedUserId, "user100", "user100@test.com", false, new String[]{"ROLE_USER"});
        when(authServiceClient.getUserInfo(mockToken)).thenReturn(mockUserInfo);

        // Mock PlannerClient and LibraryService (as before)
        when(plannerClient.canPublishRoute(tripId, expectedUserId, mockToken)).thenReturn(true);
        when(plannerClient.getRouteDetails(tripId, mockToken)).thenReturn(trip);
        when(libraryService.publishRoute(any(Trip.class), eq(expectedUserId))).thenReturn(publicRouteDto); // Use eq() for userId

        // Act & Assert
        mockMvc.perform(post("/api/v1/routes/publish/{tripId}", tripId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(routeUuid.toString()))
                .andExpect(jsonPath("$.title").value(publicRouteDto.getTitle()));
    }

    @Test
    @DisplayName("POST /api/v1/routes/publish/{tripId} - Cannot Publish")
    @WithMockUser(username="100", authorities={"ROLE_USER"}, password="mockPassword")
    void testPublishRoute_CannotPublish() throws Exception {
        // Arrange
        Long tripId = 51L;
        String mockToken = "mockPassword";
        Long expectedUserId = 100L;

        // Mock AuthServiceClient (still needed for @CurrentUser resolution)
        UserInfo mockUserInfo = new UserInfo(expectedUserId, "user100", "user100@test.com", false, new String[]{"ROLE_USER"});
        when(authServiceClient.getUserInfo(mockToken)).thenReturn(mockUserInfo);

        // Mock PlannerClient to return false for canPublishRoute
        when(plannerClient.canPublishRoute(tripId, expectedUserId, mockToken)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/v1/routes/publish/{tripId}", tripId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/v1/routes/{id} - Success")
    @WithMockUser // Requires authentication, specific roles might be needed depending on security config
    void testDeleteRoute_Success() throws Exception {
        // Arrange
        Long routeId = 1L;
        // No need to mock libraryService.deletePublishedRoute as it returns void
        // Mockito.doNothing().when(libraryService).deletePublishedRoute(routeId); // Can be used if verification is needed

        // Act & Assert
        mockMvc.perform(delete("/api/v1/routes/{id}", routeId)
                .with(csrf())) // Add csrf if needed
                .andExpect(status().isNoContent()); // Expect 204 No Content
    }

    @Test
    @DisplayName("GET /api/v1/routes/search - Success")
    @WithMockUser
    void testSearchRoutes_Success() throws Exception {
        // Arrange
        String searchQuery = "italy";
        Pageable pageable = PageRequest.of(0, 15); // Example pageable
        Page<RoutePreviewDto> routePage = new PageImpl<>(Collections.singletonList(routePreviewDto), pageable, 1);
        when(libraryService.searchRoutes(eq(searchQuery), any(Pageable.class))).thenReturn(routePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/routes/search")
                .param("query", searchQuery)
                .param("page", "0")
                .param("size", "15") // Match the pageable size
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(routeUuid.toString()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(15));
    }
} 