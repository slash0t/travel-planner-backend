package ru.putevod.app.library.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.library.client.PlannerClient;
import ru.putevod.app.library.dto.CopyRouteRequestDto;
import ru.putevod.app.library.dto.CopyRouteResponseDto;
import ru.putevod.app.library.dto.PublicRouteDetailDto;
import ru.putevod.app.library.dto.PublicRouteDto;
import ru.putevod.app.library.dto.RoutePreviewDto;
import ru.putevod.app.library.dto.planner.CreateTripDto;
import ru.putevod.app.library.dto.planner.TripDetailDto;
import ru.putevod.app.library.dto.planner.CreateTripDayDto;
import ru.putevod.app.library.dto.planner.CreateEventDto;
import ru.putevod.app.library.dto.planner.UpdateTripDayDto;
import ru.putevod.app.library.entity.PublishedRoute;
import ru.putevod.app.library.entity.Trip;
import ru.putevod.app.library.entity.User;
import ru.putevod.app.library.exception.ResourceNotFoundException;
import ru.putevod.app.library.exception.ExternalServiceException;
import ru.putevod.app.library.repository.PublishedRouteRepository;
import ru.putevod.app.library.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryService {

    private final PublishedRouteRepository publishedRouteRepository;
    private final UserRepository userRepository;
    private final MapperService mapperService;
    private final PlannerClient plannerClient;

    @Transactional(readOnly = true)
    public Page<RoutePreviewDto> getPublishedRoutes(Pageable pageable) {
        return publishedRouteRepository.findAllApproved(pageable)
                .map(mapperService::toRoutePreviewDto);
    }

    @Transactional(readOnly = true)
    public Page<RoutePreviewDto> getPendingRoutes(Pageable pageable) {
        return publishedRouteRepository.findAllPendingApproval(pageable)
                .map(mapperService::toRoutePreviewDto);
    }

    @Transactional(readOnly = true)
    public Page<RoutePreviewDto> searchRoutes(String query, Pageable pageable) {
        return publishedRouteRepository.searchByQuery(query, pageable)
                .map(mapperService::toRoutePreviewDto);
    }

    @Transactional(readOnly = true)
    public Page<RoutePreviewDto> getFilteredRoutes(String country, String city, Integer durationMin,
                                                   Integer durationMax, String tag, Pageable pageable) {
        return publishedRouteRepository.findWithFilters(country, city, durationMin, durationMax, tag, pageable)
                .map(mapperService::toRoutePreviewDto);
    }

    @Transactional(readOnly = true)
    public Page<RoutePreviewDto> getPopularRoutes(Pageable pageable) {
        return publishedRouteRepository.findPopular(pageable)
                .map(mapperService::toRoutePreviewDto);
    }

    @Transactional(readOnly = true)
    public Page<RoutePreviewDto> getMostRatedRoutes(Pageable pageable) {
        return publishedRouteRepository.findMostRated(pageable)
                .map(mapperService::toRoutePreviewDto);
    }

    @Transactional(readOnly = true)
    public PublicRouteDetailDto getRouteDetails(Long routeId) {
        PublishedRoute publishedRoute = publishedRouteRepository.findByIdAndIsApprovedTrue(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id " + routeId));

        incrementViewCount(publishedRoute);

        return mapperService.toPublicRouteDetailDto(publishedRoute);
    }

    @Transactional
    protected void incrementViewCount(PublishedRoute publishedRoute) {
        publishedRoute.setViewCount(publishedRoute.getViewCount() + 1);
        publishedRouteRepository.save(publishedRoute);
    }

    @Transactional(readOnly = true)
    public List<RoutePreviewDto> getUserPublishedRoutes(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        return publishedRouteRepository.findByUserIdAndIsApprovedTrue(userId).stream()
                .map(mapperService::toRoutePreviewDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PublicRouteDto publishRoute(Trip trip, Long userId) {
        if (publishedRouteRepository.existsByOriginalRouteId(trip.getId())) {
            throw new IllegalStateException("Route is already published");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        PublishedRoute publishedRoute = PublishedRoute.builder()
                .originalRouteId(trip.getId())
                .userId(userId)
                .title(trip.getTitle())
                .description(trip.getDescription())
                .country(trip.getCountry())
                .city(trip.getCity())
                .duration(trip.getDuration())
                .previewUrl(trip.getPreviewUrl())
                .isApproved(false)
                .viewCount(0)
                .build();

        PublishedRoute saved = publishedRouteRepository.save(publishedRoute);
        return mapperService.toPublicRouteDto(saved);
    }

    @Transactional
    public PublicRouteDto approvePublishedRoute(Long routeId) {
        PublishedRoute publishedRoute = publishedRouteRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Published route not found with id " + routeId));

        publishedRoute.setIsApproved(true);
        PublishedRoute saved = publishedRouteRepository.save(publishedRoute);

        return mapperService.toPublicRouteDto(saved);
    }

    @Transactional
    public void deletePublishedRoute(Long routeId) {
        PublishedRoute publishedRoute = publishedRouteRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Published route not found with id " + routeId));

        publishedRouteRepository.delete(publishedRoute);
    }

    @Transactional(readOnly = true)
    public PublishedRoute getPublishedRouteById(Long routeId) {
        return publishedRouteRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Published route not found with id " + routeId));
    }

    @Transactional
    public CopyRouteResponseDto copyRoute(Long routeId, CopyRouteRequestDto request, Long userId, String authToken) {
        PublishedRoute publishedRoute = publishedRouteRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Опубликованный маршрут не найден"));

        log.info("Начинаем копирование маршрута {} для пользователя {}", routeId, userId);

        try {
            TripDetailDto originalTrip = plannerClient.getTripWithDetails(
                publishedRoute.getOriginalRouteId(),  // правильное имя поля
                publishedRoute.getUserId(),          // правильное имя поля для создателя
                authToken
            );

            if (originalTrip == null) {
                throw new ExternalServiceException("Не удалось получить данные о маршруте из сервиса планирования");
            }

            long duration = ChronoUnit.DAYS.between(originalTrip.getStartDate(), originalTrip.getEndDate()) + 1;
            LocalDate newEndDate = request.getStartDate().plusDays(duration - 1);

            String newTitle = (request.getTitle() != null && !request.getTitle().trim().isEmpty())
                ? request.getTitle().trim() 
                : "Копия: " + originalTrip.getTitle();

            CreateTripDto createTripDto = CreateTripDto.builder()
                    .title(newTitle)
                    .description(originalTrip.getDescription())
                    .startDate(request.getStartDate())
                    .endDate(newEndDate)
                    .country(originalTrip.getCountry())
                    .city(originalTrip.getCity())
                    .build();

            Trip newTrip = plannerClient.createTrip(createTripDto, authToken);
            if (newTrip == null) {
                throw new ExternalServiceException("Не удалось создать новый маршрут в сервисе планирования");
            }

            int copiedDaysCount = copyTripDays(newTrip.getId(), originalTrip.getDays(),
                                             request.getStartDate(), authToken);

            log.info("Маршрут {} успешно скопирован для пользователя {}. Новый ID: {}, скопировано дней: {}", 
                    routeId, userId, newTrip.getId(), copiedDaysCount);

            return CopyRouteResponseDto.builder()
                    .tripId(newTrip.getId())
                    .title(newTitle)
                    .startDate(request.getStartDate())
                    .endDate(newEndDate)
                    .duration((int) duration)
                    .copiedDaysCount(copiedDaysCount)
                    .build();

        } catch (Exception e) {
            log.error("Ошибка при копировании маршрута {} для пользователя {}: {}", routeId, userId, e.getMessage(), e);
            throw new ExternalServiceException("Не удалось скопировать маршрут: " + e.getMessage());
        }
    }

    /**
     * Вычисляет длительность маршрута в днях
     */
    private int calculateDuration(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 1;
        }
        return (int) (endDate.toEpochDay() - startDate.toEpochDay() + 1);
    }

    /**
     * Копирует дни маршрута с пересчетом дат и события
     */
    private int copyTripDays(Long newTripId, List<TripDetailDto.TripDayDto> originalDays, 
                            LocalDate newStartDate, String authToken) {
        
        List<TripDetailDto.TripDayDto> existingDays = plannerClient.getTripDays(newTripId, authToken);
        
        if (existingDays == null || existingDays.isEmpty()) {
            log.warn("Не удалось получить дни для нового маршрута {}", newTripId);
            return 0;
        }
        
        log.info("Найдено {} существующих дней для маршрута {}", existingDays.size(), newTripId);
        
        int copiedDaysCount = 0;
        
        for (int i = 0; i < originalDays.size() && i < existingDays.size(); i++) {
            TripDetailDto.TripDayDto originalDay = originalDays.get(i);
            TripDetailDto.TripDayDto existingDay = existingDays.get(i);
            
            try {
                LocalDate originalDayDate = originalDay.getDate();
                LocalDate originalStartDate = originalDays.get(0).getDate();
                LocalDate newDayDate = calculateNewDayDate(originalDayDate, originalStartDate, newStartDate);
                
                UpdateTripDayDto updateDayDto = UpdateTripDayDto.builder()
                        .date(newDayDate)
                        .note(originalDay.getDescription())
                        .dayNumber(i + 1)
                        .build();
                
                TripDetailDto.TripDayDto updatedDay = plannerClient.updateTripDay(
                    newTripId, existingDay.getDayId(), updateDayDto, authToken);
                
                if (updatedDay != null) {
                    copiedDaysCount++;
                    log.info("Обновлен день {} для маршрута {} с датой {}", 
                            updatedDay.getDayId(), newTripId, newDayDate);
                    
                    if (originalDay.getEvents() != null && !originalDay.getEvents().isEmpty()) {
                        copyDayEvents(newTripId, updatedDay.getDayId(), originalDay.getEvents(), authToken);
                    }
                } else {
                    log.warn("Не удалось обновить день {} для маршрута {}", existingDay.getDayId(), newTripId);
                }
                
            } catch (Exception e) {
                log.error("Ошибка при обновлении дня {} для маршрута {}: {}", 
                         existingDay.getDayId(), newTripId, e.getMessage());
            }
        }
        
        log.info("Успешно обновлено {} дней для маршрута {}", copiedDaysCount, newTripId);
        return copiedDaysCount;
    }
    
    /**
     * Копирует события дня
     */
    private void copyDayEvents(Long tripId, Long dayId, List<TripDetailDto.EventDto> originalEvents, String authToken) {
        for (TripDetailDto.EventDto originalEvent : originalEvents) {
            try {
                CreateEventDto createEventDto = CreateEventDto.builder()
                        .title(originalEvent.getTitle())
                        .description(originalEvent.getDescription())
                        .startTime(parseTime(originalEvent.getStartTime()))
                        .endTime(parseTime(originalEvent.getEndTime()))
                        .hasSpecificTime(originalEvent.getStartTime() != null)
                        .notes(originalEvent.getDescription()) // Используем description как notes
                        .orderPosition(1) // Порядок будет установлен автоматически
                        .place(mapPlaceInfo(originalEvent.getPlace()))
                        .build();
                
                TripDetailDto.EventDto newEvent = plannerClient.createEvent(tripId, dayId, createEventDto, authToken);
                if (newEvent != null) {
                    log.debug("Created event {} for day {} in trip {}", newEvent.getEventId(), dayId, tripId);
                } else {
                    log.warn("Failed to create event '{}' for day {} in trip {}", originalEvent.getTitle(), dayId, tripId);
                }
                
            } catch (Exception e) {
                log.error("Error copying event {} for day {} in trip {}: {}", 
                         originalEvent.getEventId(), dayId, tripId, e.getMessage());
            }
        }
    }
    
    /**
     * Пересчитывает дату дня относительно новой даты начала
     */
    private LocalDate calculateNewDayDate(LocalDate originalDayDate, LocalDate originalStartDate, LocalDate newStartDate) {
        if (originalDayDate == null || originalStartDate == null) {
            return newStartDate;
        }
        
        long daysDifference = originalDayDate.toEpochDay() - originalStartDate.toEpochDay();
        return newStartDate.plusDays(daysDifference);
    }
    
    /**
     * Парсит время из строки
     */
    private java.time.LocalTime parseTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        try {
            return java.time.LocalTime.parse(timeString);
        } catch (Exception e) {
            log.warn("Failed to parse time '{}': {}", timeString, e.getMessage());
            return null;
        }
    }
    
    /**
     * Маппит информацию о месте
     */
    private CreateEventDto.PlaceInfo mapPlaceInfo(TripDetailDto.PlaceDto originalPlace) {
        if (originalPlace == null) {
            return null;
        }
        
        return CreateEventDto.PlaceInfo.builder()
                .name(originalPlace.getName())
                .latitude(originalPlace.getLatitude() != null ? 
                         java.math.BigDecimal.valueOf(originalPlace.getLatitude()) : null)
                .longitude(originalPlace.getLongitude() != null ? 
                          java.math.BigDecimal.valueOf(originalPlace.getLongitude()) : null)
                .address(originalPlace.getAddress())
                .externalId(originalPlace.getExternalId())
                .build();
    }
} 