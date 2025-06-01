package ru.putevod.app.planner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.putevod.app.planner.dto.TripDayDto;
import ru.putevod.app.planner.exception.BadRequestException;
import ru.putevod.app.planner.exception.ResourceNotFoundException;
import ru.putevod.app.planner.mapper.TripDayMapper;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.TripDay;
import ru.putevod.app.planner.repository.TripDayRepository;
import ru.putevod.app.planner.service.TripService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripDayServiceImplTest {

    @Mock
    private TripDayRepository tripDayRepository;
    @Mock
    private TripService tripService;
    @Mock
    private TripDayMapper tripDayMapper;

    @InjectMocks
    private TripDayServiceImpl tripDayService;

    private Trip trip;
    private TripDay tripDay;
    private TripDayDto tripDayDto;

    @BeforeEach
    void setUp() {
        trip = new Trip();
        trip.setTripId(1L);

        tripDay = TripDay.builder()
                .dayId(1L)
                .trip(trip)
                .dayNumber(1)
                .date(LocalDate.now())
                .note("Test note")
                .build();

        tripDayDto = TripDayDto.builder()
                .id(1L)
                .tripId(1L)
                .dayNumber(1)
                .date(LocalDate.now())
                .note("Test note")
                .build();
    }

    @Test
    void createTripDay_Success() {
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(List.of()).when(tripDayRepository).findByTripOrderByDayNumberAsc(any());
        doReturn(Optional.empty()).when(tripDayRepository).findByTripAndDate(any(), any());
        doReturn(tripDay).when(tripDayMapper).fromDto(any(), any());
        doReturn(tripDay).when(tripDayRepository).save(any());
        doReturn(tripDayDto).when(tripDayMapper).toDto(any());

        TripDayDto result = tripDayService.createTripDay(1L, 1L, tripDayDto);

        assertNotNull(result);
        assertEquals(tripDayDto.getId(), result.getId());
        verify(tripDayRepository).save(any());
    }

    @Test
    void createTripDay_DayNumberExists() {
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(List.of(tripDay)).when(tripDayRepository).findByTripOrderByDayNumberAsc(any());

        assertThrows(BadRequestException.class, () ->
                tripDayService.createTripDay(1L, 1L, tripDayDto));
    }

    @Test
    void createTripDay_DateExists() {
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(List.of()).when(tripDayRepository).findByTripOrderByDayNumberAsc(any());
        doReturn(Optional.of(tripDay)).when(tripDayRepository).findByTripAndDate(any(), any());

        assertThrows(BadRequestException.class, () ->
                tripDayService.createTripDay(1L, 1L, tripDayDto));
    }

    @Test
    void createTripDay_AutoDayNumber() {
        TripDayDto dtoWithoutDayNumber = TripDayDto.builder()
                .tripId(1L)
                .date(LocalDate.now())
                .note("Test note")
                .build();

        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(List.of(tripDay)).when(tripDayRepository).findByTripOrderByDayNumberAsc(any());
        doReturn(Optional.empty()).when(tripDayRepository).findByTripAndDate(any(), any());
        doReturn(tripDay).when(tripDayMapper).fromDto(any(), any());
        doReturn(tripDay).when(tripDayRepository).save(any());
        doReturn(tripDayDto).when(tripDayMapper).toDto(any());

        TripDayDto result = tripDayService.createTripDay(1L, 1L, dtoWithoutDayNumber);

        assertNotNull(result);
        verify(tripDayRepository).save(any());
    }

    @Test
    void updateTripDay_Success() {
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(Optional.of(tripDay)).when(tripDayRepository).findByTripAndDayId(any(), anyLong());
        doReturn(tripDay).when(tripDayRepository).save(any());
        doReturn(tripDayDto).when(tripDayMapper).toDto(any());

        TripDayDto result = tripDayService.updateTripDay(1L, 1L, 1L, tripDayDto);

        assertNotNull(result);
        assertEquals(tripDayDto.getId(), result.getId());
        verify(tripDayRepository).save(any());
    }

    @Test
    void updateTripDay_NotFound() {
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(Optional.empty()).when(tripDayRepository).findByTripAndDayId(any(), anyLong());

        assertThrows(ResourceNotFoundException.class, () ->
                tripDayService.updateTripDay(1L, 1L, 1L, tripDayDto));
    }

    @Test
    void updateTripDay_DayNumberExists() {
        TripDay existingDay = TripDay.builder()
                .dayId(2L)
                .trip(trip)
                .dayNumber(2)
                .build();

        tripDayDto.setDayNumber(2);

        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(Optional.of(tripDay)).when(tripDayRepository).findByTripAndDayId(any(), anyLong());
        doReturn(List.of(existingDay)).when(tripDayRepository).findByTripOrderByDayNumberAsc(any());

        assertThrows(BadRequestException.class, () ->
                tripDayService.updateTripDay(1L, 1L, 1L, tripDayDto));
    }

    @Test
    void getTripDay_Success() {
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(Optional.of(tripDay)).when(tripDayRepository).findByTripAndDayId(any(), anyLong());
        doReturn(tripDayDto).when(tripDayMapper).toDto(any());

        TripDayDto result = tripDayService.getTripDay(1L, 1L, 1L);

        assertNotNull(result);
        assertEquals(tripDayDto.getId(), result.getId());
    }

    @Test
    void getTripDay_NotFound() {
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(Optional.empty()).when(tripDayRepository).findByTripAndDayId(any(), anyLong());

        assertThrows(ResourceNotFoundException.class, () ->
                tripDayService.getTripDay(1L, 1L, 1L));
    }

    @Test
    void getTripDayByDate_Success() {
        LocalDate date = LocalDate.now();
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(Optional.of(tripDay)).when(tripDayRepository).findByTripAndDate(any(), any());
        doReturn(tripDayDto).when(tripDayMapper).toDto(any());

        TripDayDto result = tripDayService.getTripDayByDate(1L, 1L, date);

        assertNotNull(result);
        assertEquals(tripDayDto.getId(), result.getId());
    }

    @Test
    void getTripDayByDate_NotFound() {
        LocalDate date = LocalDate.now();
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(Optional.empty()).when(tripDayRepository).findByTripAndDate(any(), any());

        assertThrows(ResourceNotFoundException.class, () ->
                tripDayService.getTripDayByDate(1L, 1L, date));
    }

    @Test
    void getTripDays_Success() {
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(List.of(tripDay)).when(tripDayRepository).findByTripOrderByDayNumberAsc(any());
        doReturn(tripDayDto).when(tripDayMapper).toDto(any());

        List<TripDayDto> result = tripDayService.getTripDays(1L, 1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void deleteTripDay_Success() {
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(Optional.of(tripDay)).when(tripDayRepository).findByTripAndDayId(any(), anyLong());

        tripDayService.deleteTripDay(1L, 1L, 1L);

        verify(tripDayRepository).deleteByTripAndDayId(any(), anyLong());
    }

    @Test
    void deleteTripDay_NotFound() {
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(Optional.empty()).when(tripDayRepository).findByTripAndDayId(any(), anyLong());

        assertThrows(ResourceNotFoundException.class, () ->
                tripDayService.deleteTripDay(1L, 1L, 1L));
    }
} 