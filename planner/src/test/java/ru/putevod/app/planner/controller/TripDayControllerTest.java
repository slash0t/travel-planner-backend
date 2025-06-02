package ru.putevod.app.planner.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.putevod.app.planner.dto.TripDayDto;
import ru.putevod.app.planner.service.TripDayService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TripDayControllerTest {

    @Mock
    private TripDayService tripDayService;

    @InjectMocks
    private TripDayController tripDayController;

    private Long userId;
    private Long tripId;
    private Long dayId;
    private TripDayDto mockTripDayDto;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = 1L;
        tripId = 1L;
        dayId = 1L;
        testDate = LocalDate.now();

        mockTripDayDto = TripDayDto.builder()
                .id(dayId)
                .tripId(tripId)
                .dayNumber(1)
                .date(testDate)
                .note("Test day")
                .build();
    }

    @Test
    void createTripDay_ShouldReturnCreatedDay() {
        when(tripDayService.createTripDay(eq(userId), eq(tripId), any(TripDayDto.class)))
                .thenReturn(mockTripDayDto);

        ResponseEntity<TripDayDto> response = tripDayController.createTripDay(userId, tripId, mockTripDayDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTripDayDto.getId(), response.getBody().getId());
        assertEquals(mockTripDayDto.getDayNumber(), response.getBody().getDayNumber());
        assertEquals(mockTripDayDto.getDate(), response.getBody().getDate());
        verify(tripDayService).createTripDay(eq(userId), eq(tripId), any(TripDayDto.class));
    }

    @Test
    void getTripDays_ShouldReturnDays() {
        List<TripDayDto> days = Arrays.asList(mockTripDayDto);
        when(tripDayService.getTripDays(userId, tripId))
                .thenReturn(days);

        ResponseEntity<List<TripDayDto>> response = tripDayController.getTripDays(userId, tripId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(mockTripDayDto.getId(), response.getBody().get(0).getId());
        verify(tripDayService).getTripDays(userId, tripId);
    }

    @Test
    void getTripDay_ShouldReturnDay() {
        when(tripDayService.getTripDay(userId, tripId, dayId))
                .thenReturn(mockTripDayDto);

        ResponseEntity<TripDayDto> response = tripDayController.getTripDay(userId, tripId, dayId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTripDayDto.getId(), response.getBody().getId());
        assertEquals(mockTripDayDto.getDayNumber(), response.getBody().getDayNumber());
        verify(tripDayService).getTripDay(userId, tripId, dayId);
    }

    @Test
    void getTripDayByDate_ShouldReturnDay() {
        when(tripDayService.getTripDayByDate(userId, tripId, testDate))
                .thenReturn(mockTripDayDto);

        ResponseEntity<TripDayDto> response = tripDayController.getTripDayByDate(userId, tripId, testDate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTripDayDto.getId(), response.getBody().getId());
        assertEquals(mockTripDayDto.getDate(), response.getBody().getDate());
        verify(tripDayService).getTripDayByDate(userId, tripId, testDate);
    }

    @Test
    void getTripDayByNumber_ShouldReturnDay() {
        List<TripDayDto> days = Arrays.asList(mockTripDayDto);
        when(tripDayService.getTripDays(userId, tripId))
                .thenReturn(days);

        ResponseEntity<TripDayDto> response = tripDayController.getTripDayByNumber(userId, tripId, 1);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTripDayDto.getId(), response.getBody().getId());
        assertEquals(mockTripDayDto.getDayNumber(), response.getBody().getDayNumber());
        verify(tripDayService).getTripDays(userId, tripId);
    }

    @Test
    void getCurrentTripDay_ShouldReturnCurrentDay() {
        when(tripDayService.getTripDayByDate(userId, tripId, LocalDate.now()))
                .thenReturn(mockTripDayDto);

        ResponseEntity<TripDayDto> response = tripDayController.getCurrentTripDay(userId, tripId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTripDayDto.getId(), response.getBody().getId());
        verify(tripDayService).getTripDayByDate(userId, tripId, LocalDate.now());
    }

    @Test
    void getCurrentTripDay_ShouldReturnFirstDay_WhenCurrentDayNotFound() {
        List<TripDayDto> days = Arrays.asList(mockTripDayDto);
        when(tripDayService.getTripDayByDate(userId, tripId, LocalDate.now()))
                .thenThrow(new RuntimeException("Day not found"));
        when(tripDayService.getTripDays(userId, tripId))
                .thenReturn(days);

        ResponseEntity<TripDayDto> response = tripDayController.getCurrentTripDay(userId, tripId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTripDayDto.getId(), response.getBody().getId());
        verify(tripDayService).getTripDayByDate(userId, tripId, LocalDate.now());
        verify(tripDayService).getTripDays(userId, tripId);
    }

    @Test
    void updateTripDay_ShouldReturnUpdatedDay() {
        when(tripDayService.updateTripDay(eq(userId), eq(tripId), eq(dayId), any(TripDayDto.class)))
                .thenReturn(mockTripDayDto);

        ResponseEntity<TripDayDto> response = tripDayController.updateTripDay(userId, tripId, dayId, mockTripDayDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTripDayDto.getId(), response.getBody().getId());
        assertEquals(mockTripDayDto.getDayNumber(), response.getBody().getDayNumber());
        verify(tripDayService).updateTripDay(eq(userId), eq(tripId), eq(dayId), any(TripDayDto.class));
    }

    @Test
    void deleteTripDay_ShouldReturnNoContent() {
        doNothing().when(tripDayService).deleteTripDay(userId, tripId, dayId);

        ResponseEntity<Void> response = tripDayController.deleteTripDay(userId, tripId, dayId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(tripDayService).deleteTripDay(userId, tripId, dayId);
    }
} 