package ru.putevod.app.planner.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.putevod.app.planner.dto.CreateTripDto;
import ru.putevod.app.planner.dto.TripAccessDto;
import ru.putevod.app.planner.dto.TripDto;
import ru.putevod.app.planner.dto.CreateTripAccessDto;
import ru.putevod.app.planner.dto.UpdateTripDto;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.User;

import java.util.List;

public interface TripService {

    TripDto createTrip(Long userId, TripDto tripDto);

    TripDto createTrip(Long userId, CreateTripDto createTripDto);

    TripDto updateTrip(Long userId, Long tripId, UpdateTripDto updateTripDto);

    TripDto getTripById(Long userId, Long tripId);

    Trip getTripEntityById(Long tripId);

    Trip getTripEntityWithAccessCheck(Long userId, Long tripId);

    Page<TripDto> getUserTrips(Long userId, String filter, Pageable pageable);

    void deleteTrip(Long userId, Long tripId);

    TripAccessDto shareTrip(Long userId, Long tripId, CreateTripAccessDto accessDto);

    List<TripAccessDto> getTripShares(Long userId, Long tripId);

    void removeShare(Long userId, Long tripId, Long shareUserId);

    TripAccessDto respondToInvitation(Long userId, Long tripId, String status);

    List<TripDto> getUpcomingTrips(Long userId);

    List<TripDto> getOngoingTrips(Long userId);

    List<TripDto> getPastTrips(Long userId);

    boolean hasAccessToTrip(User user, Trip trip, String... requiredLevels);

    boolean canPublishTrip(Long userId, Long tripId);

    TripDto publishTrip(Long userId, Long tripId, boolean publish);

    /**
     * Получить общее количество путешествий пользователя
     *
     * @param userId ID пользователя
     * @return общее количество путешествий
     */
    Long getTotalTripsCount(Long userId);

    /**
     * Получить общее количество событий во всех путешествиях пользователя
     *
     * @param userId ID пользователя
     * @return общее количество событий
     */
    Long getTotalPlacesCount(Long userId);
} 