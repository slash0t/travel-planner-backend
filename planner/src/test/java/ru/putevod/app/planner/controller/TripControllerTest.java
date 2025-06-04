package ru.putevod.app.planner.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.putevod.app.planner.dto.CreateTripDto;
import ru.putevod.app.planner.dto.UpdateTripDto;
import ru.putevod.app.planner.dto.CreateTripAccessDto;
import ru.putevod.app.planner.dto.TripDto;
import ru.putevod.app.planner.dto.TripAccessDto;
import ru.putevod.app.planner.dto.UserDto;
import ru.putevod.app.planner.dto.RemoveShareResponseDto;
import ru.putevod.app.planner.service.TripService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TripControllerTest {

    @Mock
    private TripService tripService;

    @InjectMocks
    private TripController tripController;

    private TripDto mockTripDto;
    private CreateTripDto mockCreateTripDto;
    private UpdateTripDto mockUpdateTripDto;
    private TripAccessDto mockTripAccessDto;
    private CreateTripAccessDto mockCreateTripAccessDto;
    private Long userId;
    private Long tripId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = 1L;
        tripId = 1L;

        UserDto userDto = UserDto.builder()
                .id(userId)
                .username("testUser")
                .email("test@example.com")
                .build();

        mockTripDto = TripDto.builder()
                .id(tripId)
                .title("Test Trip")
                .description("Test Description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .country("Russia")
                .city("Moscow")
                .published(false)
                .creator(userDto)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        mockCreateTripDto = CreateTripDto.builder()
                .title("Test Trip")
                .description("Test Description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .country("Russia")
                .city("Moscow")
                .published(false)
                .build();

        mockUpdateTripDto = UpdateTripDto.builder()
                .title("Updated Trip")
                .description("Updated Description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .country("Russia")
                .city("Moscow")
                .published(true)
                .build();

        mockTripAccessDto = TripAccessDto.builder()
                .user(userDto)
                .accessLevel("read")
                .build();

        mockCreateTripAccessDto = CreateTripAccessDto.builder()
                .userId(2L)
                .accessLevel("read")
                .build();
    }

    @Test
    void createTrip_ShouldReturnCreatedTrip() {
        when(tripService.createTrip(eq(userId), any(CreateTripDto.class)))
                .thenReturn(mockTripDto);

        ResponseEntity<TripDto> response = tripController.createTrip(userId, mockCreateTripDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTripDto.getId(), response.getBody().getId());
        assertEquals(mockTripDto.getTitle(), response.getBody().getTitle());
        verify(tripService).createTrip(eq(userId), any(CreateTripDto.class));
    }

    @Test
    void getUserTrips_ShouldReturnTrips() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TripDto> mockPage = new PageImpl<>(Arrays.asList(mockTripDto));
        when(tripService.getUserTrips(eq(userId), eq("all"), any(Pageable.class)))
                .thenReturn(mockPage);

        ResponseEntity<Page<TripDto>> response = tripController.getUserTrips(userId, "all", pageable);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(tripService).getUserTrips(eq(userId), eq("all"), any(Pageable.class));
    }

    @Test
    void getUpcomingTrips_ShouldReturnTrips() {
        when(tripService.getUpcomingTrips(userId))
                .thenReturn(Arrays.asList(mockTripDto));

        ResponseEntity<List<TripDto>> response = tripController.getUpcomingTrips(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(tripService).getUpcomingTrips(userId);
    }

    @Test
    void getOngoingTrips_ShouldReturnTrips() {
        when(tripService.getOngoingTrips(userId))
                .thenReturn(Arrays.asList(mockTripDto));

        ResponseEntity<List<TripDto>> response = tripController.getOngoingTrips(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(tripService).getOngoingTrips(userId);
    }

    @Test
    void getPastTrips_ShouldReturnTrips() {
        when(tripService.getPastTrips(userId))
                .thenReturn(Arrays.asList(mockTripDto));

        ResponseEntity<List<TripDto>> response = tripController.getPastTrips(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(tripService).getPastTrips(userId);
    }

    @Test
    void getTripById_ShouldReturnTrip() {
        when(tripService.getTripById(userId, tripId))
                .thenReturn(mockTripDto);

        ResponseEntity<TripDto> response = tripController.getTripById(userId, tripId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTripDto.getId(), response.getBody().getId());
        verify(tripService).getTripById(userId, tripId);
    }

    @Test
    void updateTrip_ShouldReturnUpdatedTrip() {
        when(tripService.updateTrip(eq(userId), eq(tripId), any(UpdateTripDto.class)))
                .thenReturn(mockTripDto);

        ResponseEntity<TripDto> response = tripController.updateTrip(userId, tripId, mockUpdateTripDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTripDto.getId(), response.getBody().getId());
        verify(tripService).updateTrip(eq(userId), eq(tripId), any(UpdateTripDto.class));
    }

    @Test
    void deleteTrip_ShouldReturnNoContent() {
        doNothing().when(tripService).deleteTrip(userId, tripId);

        ResponseEntity<Void> response = tripController.deleteTrip(userId, tripId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(tripService).deleteTrip(userId, tripId);
    }

    @Test
    void shareTrip_ShouldReturnCreatedAccess() {
        when(tripService.shareTrip(eq(userId), eq(tripId), any(CreateTripAccessDto.class)))
                .thenReturn(mockTripAccessDto);

        ResponseEntity<TripAccessDto> response = tripController.shareTrip(userId, tripId, mockCreateTripAccessDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTripAccessDto.getUser().getId(), response.getBody().getUser().getId());
        verify(tripService).shareTrip(eq(userId), eq(tripId), any(CreateTripAccessDto.class));
    }

    @Test
    void getTripShares_ShouldReturnShares() {
        when(tripService.getTripShares(userId, tripId))
                .thenReturn(Arrays.asList(mockTripAccessDto));

        ResponseEntity<List<TripAccessDto>> response = tripController.getTripShares(userId, tripId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(tripService).getTripShares(userId, tripId);
    }

    @Test
    void removeShare_ShouldReturnRemoveResponse() {
        RemoveShareResponseDto mockResponse = RemoveShareResponseDto.builder()
                .message("Доступ к поездке успешно удален")
                .removedUserId(2L)
                .removedUsername("testuser")
                .previousInvitationStatus("accepted")
                .previousAccessLevel("read")
                .build();
        
        when(tripService.removeShare(userId, tripId, 2L))
                .thenReturn(mockResponse);

        ResponseEntity<RemoveShareResponseDto> response = tripController.removeShare(userId, tripId, 2L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Доступ к поездке успешно удален", response.getBody().getMessage());
        assertEquals(2L, response.getBody().getRemovedUserId());
        assertEquals("testuser", response.getBody().getRemovedUsername());
        verify(tripService).removeShare(userId, tripId, 2L);
    }

    @Test
    void respondToInvitation_ShouldReturnAccess() {
        when(tripService.respondToInvitation(eq(userId), eq(tripId), anyString()))
                .thenReturn(mockTripAccessDto);

        ResponseEntity<TripAccessDto> response = tripController.respondToInvitation(userId, tripId, "accepted");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTripAccessDto.getUser().getId(), response.getBody().getUser().getId());
        verify(tripService).respondToInvitation(eq(userId), eq(tripId), anyString());
    }

    @Test
    void canPublishTrip_ShouldReturnBoolean() {
        when(tripService.canPublishTrip(userId, tripId))
                .thenReturn(true);

        ResponseEntity<Boolean> response = tripController.canPublishTrip(tripId, userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(tripService).canPublishTrip(userId, tripId);
    }

    @Test
    void publishTrip_ShouldReturnUpdatedTrip() {
        when(tripService.publishTrip(eq(userId), eq(tripId), anyBoolean()))
                .thenReturn(mockTripDto);

        ResponseEntity<TripDto> response = tripController.publishTrip(userId, tripId, true);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTripDto.getId(), response.getBody().getId());
        verify(tripService).publishTrip(eq(userId), eq(tripId), anyBoolean());
    }
} 