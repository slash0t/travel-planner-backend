package ru.putevod.app.planner.service;

import ru.putevod.app.planner.dto.CreateTripDayDto;
import ru.putevod.app.planner.dto.TripDayDto;

import java.time.LocalDate;
import java.util.List;

public interface TripDayService {

    TripDayDto createTripDay(Long userId, Long tripId, TripDayDto tripDayDto);

    TripDayDto updateTripDay(Long userId, Long tripId, Long dayId, TripDayDto tripDayDto);

    TripDayDto getTripDay(Long userId, Long tripId, Long dayId);

    TripDayDto getTripDayByDate(Long userId, Long tripId, LocalDate date);

    List<TripDayDto> getTripDays(Long userId, Long tripId);

    void deleteTripDay(Long userId, Long tripId, Long dayId);
} 