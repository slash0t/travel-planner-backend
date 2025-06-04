package ru.putevod.app.library.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.putevod.app.library.dto.planner.CreateTripDto;
import ru.putevod.app.library.dto.planner.TripDetailDto;
import ru.putevod.app.library.dto.planner.CreateTripDayDto;
import ru.putevod.app.library.dto.planner.UpdateTripDayDto;
import ru.putevod.app.library.dto.planner.CreateEventDto;
import ru.putevod.app.library.entity.Trip;
import java.util.List;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlannerClient {
    @Value("${services.planner.url}")
    private String plannerServiceUrl;

    private final WebClient.Builder webClientBuilder;

    /**
     * Получает детальную информацию о маршруте из сервиса планирования
     *
     * @param tripId id маршрута
     * @param token  токен авторизации из сервиса auth
     * @return данные о маршруте
     */
    public Trip getRouteDetails(Long tripId, String token) {
        WebClient webClient = buildWebClient(token);

        return webClient.get()
                .uri("/trips/{id}", tripId)
                .retrieve()
                .bodyToMono(Trip.class)
                .doOnError(e -> log.error("Error fetching trip details from planner service: {}", e.getMessage()))
                .block();
    }

    /**
     * Получает полную детальную информацию о маршруте включая дни и события
     *
     * @param tripId id маршрута
     * @param userId id пользователя для проверки доступа
     * @param token  токен авторизации из сервиса auth
     * @return полные данные о маршруте
     */
    public TripDetailDto getTripWithDetails(Long tripId, Long userId, String token) {
        WebClient webClient = buildWebClient(token);

        // Получаем TripDto из Planner сервиса используя сервисный эндпоинт
        PlannerTripDto tripDto = webClient.get()
                .uri("/trips/{id}/service-details?userId={userId}", tripId, userId)
                .retrieve()
                .bodyToMono(PlannerTripDto.class)
                .doOnError(e -> log.error("Error fetching trip details with days/events from planner service: {}", e.getMessage()))
                .block();

        if (tripDto == null) {
            return null;
        }

        // Конвертируем PlannerTripDto в TripDetailDto
        return mapToTripDetailDto(tripDto);
    }

    /**
     * Создает новый маршрут
     *
     * @param createTripDto данные для создания маршрута
     * @param token         токен авторизации из сервиса auth
     * @return созданный маршрут
     */
    public Trip createTrip(CreateTripDto createTripDto, String token) {
        WebClient webClient = buildWebClient(token);

        return webClient.post()
                .uri("/trips")
                .bodyValue(createTripDto)
                .retrieve()
                .bodyToMono(Trip.class)
                .doOnError(e -> log.error("Error creating trip in planner service: {}", e.getMessage()))
                .block();
    }

    /**
     * Создает новый день в маршруте используя TripDayDto
     *
     * @param tripId        id маршрута
     * @param createDayDto  данные для создания дня
     * @param token         токен авторизации из сервиса auth
     * @return созданный день
     */
    public TripDetailDto.TripDayDto createTripDayFromDto(Long tripId, CreateTripDayDto createDayDto, String token) {
        WebClient webClient = buildWebClient(token);

        PlannerTripDayDto plannerDay = webClient.post()
                .uri("/trips/{tripId}/days", tripId)
                .bodyValue(createDayDto)
                .retrieve()
                .bodyToMono(PlannerTripDayDto.class)
                .doOnError(e -> log.error("Error creating trip day in planner service: {}", e.getMessage()))
                .block();

        if (plannerDay == null) {
            return null;
        }

        return mapToTripDayDto(plannerDay);
    }

    /**
     * Создает новый день в маршруте
     *
     * @param tripId        id маршрута
     * @param createDayDto  данные для создания дня
     * @param token         токен авторизации из сервиса auth
     * @return созданный день
     */
    public TripDetailDto.TripDayDto createTripDay(Long tripId, CreateTripDayDto createDayDto, String token) {
        return createTripDayFromDto(tripId, createDayDto, token);
    }

    /**
     * Создает новое событие в дне маршрута
     *
     * @param tripId         id маршрута
     * @param dayId          id дня
     * @param createEventDto данные для создания события
     * @param token          токен авторизации из сервиса auth
     * @return созданное событие
     */
    public TripDetailDto.EventDto createEvent(Long tripId, Long dayId, CreateEventDto createEventDto, String token) {
        WebClient webClient = buildWebClient(token);

        PlannerEventDto plannerEvent = webClient.post()
                .uri("/trips/{tripId}/days/{dayId}/events", tripId, dayId)
                .bodyValue(createEventDto)
                .retrieve()
                .bodyToMono(PlannerEventDto.class)
                .doOnError(e -> log.error("Error creating event in planner service: {}", e.getMessage()))
                .block();

        if (plannerEvent == null) {
            return null;
        }

        return mapToEventDto(plannerEvent);
    }

    /**
     * Проверяет, имеет ли пользователь право на публикацию маршрута
     *
     * @param tripId id маршрута
     * @param userId id пользователя
     * @param token  токен авторизации из сервиса auth
     * @return true, если пользователь может публиковать маршрут
     */
    public boolean canPublishRoute(Long tripId, Long userId, String token) {
        WebClient webClient = buildWebClient(token);

        return Boolean.TRUE.equals(webClient.get()
                .uri("/trips/{id}/can-publish?userId={userId}", tripId, userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(false)
                .block());
    }

    /**
     * Публикует маршрут
     *
     * @param tripId  id маршрута
     * @param userId  id пользователя
     * @param token   токен авторизации из сервиса auth
     * @param publish true для публикации, false для снятия с публикации
     * @return данные обновленного маршрута
     */
    public Trip publishRoute(Long tripId, Long userId, String token, boolean publish) {
        WebClient webClient = buildWebClient(token);

        return webClient.post()
                .uri("/trips/{id}/publish?publish={publish}", tripId, publish)
                .retrieve()
                .bodyToMono(Trip.class)
                .doOnError(e -> log.error("Error publishing route in planner service: {}", e.getMessage()))
                .block();
    }

    /**
     * Получает все дни поездки
     *
     * @param tripId id маршрута
     * @param token  токен авторизации из сервиса auth
     * @return список дней поездки
     */
    public List<TripDetailDto.TripDayDto> getTripDays(Long tripId, String token) {
        WebClient webClient = buildWebClient(token);

        PlannerTripDayDto[] days = webClient.get()
                .uri("/trips/{tripId}/days", tripId)
                .retrieve()
                .bodyToMono(PlannerTripDayDto[].class)
                .doOnError(e -> log.error("Error fetching trip days from planner service: {}", e.getMessage()))
                .block();

        if (days == null) {
            return List.of();
        }

        return Arrays.stream(days)
                .map(this::mapToTripDayDto)
                .toList();
    }

    /**
     * Обновляет день в маршруте
     *
     * @param tripId       id маршрута
     * @param dayId        id дня
     * @param updateDayDto данные для обновления дня
     * @param token        токен авторизации из сервиса auth
     * @return обновленный день
     */
    public TripDetailDto.TripDayDto updateTripDay(Long tripId, Long dayId, UpdateTripDayDto updateDayDto, String token) {
        WebClient webClient = buildWebClient(token);

        PlannerTripDayDto plannerDay = webClient.put()
                .uri("/trips/{tripId}/days/{dayId}", tripId, dayId)
                .bodyValue(updateDayDto)
                .retrieve()
                .bodyToMono(PlannerTripDayDto.class)
                .doOnError(e -> log.error("Error updating trip day in planner service: {}", e.getMessage()))
                .block();

        if (plannerDay == null) {
            return null;
        }

        return mapToTripDayDto(plannerDay);
    }

    /**
     * Создает WebClient с настройками для общения с сервисом планирования
     *
     * @param token токен авторизации
     * @return настроенный WebClient
     */
    private WebClient buildWebClient(String token) {
        log.debug("Building WebClient for planner service with baseUrl: {}", plannerServiceUrl);
        
        return webClientBuilder
                .baseUrl(plannerServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }

    // Helper methods for mapping
    private TripDetailDto mapToTripDetailDto(PlannerTripDto tripDto) {
        return TripDetailDto.builder()
                .tripId(tripDto.getId())
                .title(tripDto.getTitle())
                .description(tripDto.getDescription())
                .startDate(tripDto.getStartDate())
                .endDate(tripDto.getEndDate())
                .country(tripDto.getCountry())
                .city(tripDto.getCity())
                .published(tripDto.isPublished())
                .previewUrl(tripDto.getPreviewUrl())
                .createdAt(tripDto.getCreatedAt())
                .updatedAt(tripDto.getUpdatedAt())
                .days(tripDto.getDays() != null ? 
                      tripDto.getDays().stream().map(this::mapToTripDayDto).toList() : 
                      null)
                .build();
    }

    private TripDetailDto.TripDayDto mapToTripDayDto(PlannerTripDayDto dayDto) {
        return TripDetailDto.TripDayDto.builder()
                .dayId(dayDto.getId())
                .dayNumber(dayDto.getDayNumber())
                .date(dayDto.getDate())
                .description(dayDto.getNote())
                .events(dayDto.getEvents() != null ? 
                        dayDto.getEvents().stream().map(this::mapToEventDto).toList() : 
                        null)
                .build();
    }

    private TripDetailDto.EventDto mapToEventDto(PlannerEventDto eventDto) {
        return TripDetailDto.EventDto.builder()
                .eventId(eventDto.getId())
                .title(eventDto.getTitle())
                .description(eventDto.getDescription())
                .startTime(eventDto.getStartTime() != null ? eventDto.getStartTime().toString() : null)
                .endTime(eventDto.getEndTime() != null ? eventDto.getEndTime().toString() : null)
                .place(eventDto.getPlace() != null ? mapToPlaceDto(eventDto.getPlace()) : null)
                .build();
    }

    private TripDetailDto.PlaceDto mapToPlaceDto(PlannerPlaceDto placeDto) {
        return TripDetailDto.PlaceDto.builder()
                .placeId(placeDto.getId())
                .name(placeDto.getName())
                .address(placeDto.getAddress())
                .latitude(placeDto.getLatitude() != null ? placeDto.getLatitude().doubleValue() : null)
                .longitude(placeDto.getLongitude() != null ? placeDto.getLongitude().doubleValue() : null)
                .externalId(placeDto.getExternalId())
                .build();
    }

    // DTOs для взаимодействия с Planner сервисом
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class PlannerTripDto {
        private Long id;
        private String title;
        private String description;
        private java.time.LocalDate startDate;
        private java.time.LocalDate endDate;
        private String country;
        private String city;
        private boolean published;
        private String previewUrl;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        private java.util.List<PlannerTripDayDto> days;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class PlannerTripDayDto {
        private Long id;
        private Integer dayNumber;
        private java.time.LocalDate date;
        private String note;
        private java.util.List<PlannerEventDto> events;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class PlannerEventDto {
        private Long id;
        private String title;
        private String description;
        private java.time.LocalTime startTime;
        private java.time.LocalTime endTime;
        private PlannerPlaceDto place;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class PlannerPlaceDto {
        private Long id;
        private String name;
        private String address;
        private java.math.BigDecimal latitude;
        private java.math.BigDecimal longitude;
        private String externalId;
    }
} 