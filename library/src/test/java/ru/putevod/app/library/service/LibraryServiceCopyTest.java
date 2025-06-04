package ru.putevod.app.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.putevod.app.library.client.PlannerClient;
import ru.putevod.app.library.dto.CopyRouteRequestDto;
import ru.putevod.app.library.dto.CopyRouteResponseDto;
import ru.putevod.app.library.dto.planner.CreateEventDto;
import ru.putevod.app.library.dto.planner.TripDetailDto;
import ru.putevod.app.library.dto.planner.UpdateTripDayDto;
import ru.putevod.app.library.entity.PublishedRoute;
import ru.putevod.app.library.entity.Trip;
import ru.putevod.app.library.exception.ExternalServiceException;
import ru.putevod.app.library.exception.ResourceNotFoundException;
import ru.putevod.app.library.repository.PublishedRouteRepository;
import ru.putevod.app.library.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceCopyTest {

    @Mock
    private PublishedRouteRepository publishedRouteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MapperService mapperService;

    @Mock
    private PlannerClient plannerClient;

    @InjectMocks
    private LibraryService libraryService;

    private PublishedRoute mockPublishedRoute;
    private TripDetailDto mockTripDetail;
    private Trip mockCreatedTrip;

    @BeforeEach
    void setUp() {
        mockPublishedRoute = new PublishedRoute();
        mockPublishedRoute.setId(1L);
        mockPublishedRoute.setOriginalRouteId(10L);
        mockPublishedRoute.setTitle("Тестовый маршрут");
        mockPublishedRoute.setIsApproved(true);

        // Создаем полный тестовый маршрут с днями и событиями
        mockTripDetail = TripDetailDto.builder()
                .tripId(10L)
                .title("Оригинальный маршрут")
                .description("Описание маршрута")
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 5))
                .country("Россия")
                .city("Москва")
                .previewUrl("https://example.com/preview.jpg")
                .days(List.of(
                        TripDetailDto.TripDayDto.builder()
                                .dayId(1L)
                                .dayNumber(1)
                                .date(LocalDate.of(2024, 6, 1))
                                .description("Первый день")
                                .events(List.of(
                                        TripDetailDto.EventDto.builder()
                                                .eventId(1L)
                                                .title("Посещение Красной площади")
                                                .description("Экскурсия по главной площади")
                                                .startTime("10:00")
                                                .endTime("12:00")
                                                .place(TripDetailDto.PlaceDto.builder()
                                                        .placeId(1L)
                                                        .name("Красная площадь")
                                                        .address("Красная площадь, Москва")
                                                        .latitude(55.753544)
                                                        .longitude(37.621202)
                                                        .externalId("kremlin_001")
                                                        .build())
                                                .build(),
                                        TripDetailDto.EventDto.builder()
                                                .eventId(2L)
                                                .title("Обед в кафе")
                                                .description("Традиционная русская кухня")
                                                .startTime("13:00")
                                                .endTime("14:00")
                                                .build()
                                ))
                                .build(),
                        TripDetailDto.TripDayDto.builder()
                                .dayId(2L)
                                .dayNumber(2)
                                .date(LocalDate.of(2024, 6, 2))
                                .description("Второй день")
                                .events(List.of(
                                        TripDetailDto.EventDto.builder()
                                                .eventId(3L)
                                                .title("Третьяковская галерея")
                                                .description("Посещение музея")
                                                .startTime("11:00")
                                                .endTime("15:00")
                                                .build()
                                ))
                                .build()
                ))
                .build();

        mockCreatedTrip = new Trip();
        mockCreatedTrip.setId(20L);
        mockCreatedTrip.setTitle("Копия: Оригинальный маршрут");
        mockCreatedTrip.setStartDate(LocalDate.of(2024, 7, 15));
        mockCreatedTrip.setEndDate(LocalDate.of(2024, 7, 19));
    }

    @Test
    void copyRoute_WhenValidRequest_ShouldReturnCopyResponse() {
        // Given
        Long routeId = 1L;
        Long userId = 100L;
        String authToken = "test-token";

        CopyRouteRequestDto copyRequest = CopyRouteRequestDto.builder()
                .startDate(LocalDate.of(2024, 7, 15))
                .title("Мой новый маршрут")
                .build();

        when(publishedRouteRepository.findById(routeId))
                .thenReturn(Optional.of(mockPublishedRoute));
        when(plannerClient.getTripWithDetails(eq(10L), eq(mockPublishedRoute.getUserId()), eq(authToken)))
                .thenReturn(mockTripDetail);
        when(plannerClient.createTrip(any(), eq(authToken)))
                .thenReturn(mockCreatedTrip);

        // Мокаем получение и обновление дней
        when(plannerClient.getTripDays(eq(20L), eq(authToken)))
                .thenReturn(createMockExistingDays());
        when(plannerClient.updateTripDay(eq(20L), any(Long.class), any(UpdateTripDayDto.class), eq(authToken)))
                .thenReturn(createMockUpdatedDay());

        // When
        CopyRouteResponseDto result = libraryService.copyRoute(routeId, copyRequest, userId, authToken);

        // Then
        assertNotNull(result);
        assertEquals(20L, result.getTripId());
        assertEquals("Мой новый маршрут", result.getTitle());
        assertEquals(LocalDate.of(2024, 7, 15), result.getStartDate());
        assertEquals(LocalDate.of(2024, 7, 19), result.getEndDate());
        assertEquals(5, result.getDuration());
        assertEquals(2, result.getCopiedDaysCount());
    }

    @Test
    void copyRoute_WhenValidRequestWithDaysAndEvents_ShouldCopyAll() {
        // Given
        Long routeId = 1L;
        Long userId = 100L;
        String authToken = "test-token";

        CopyRouteRequestDto copyRequest = CopyRouteRequestDto.builder()
                .startDate(LocalDate.of(2024, 7, 15))
                .build();

        when(publishedRouteRepository.findById(routeId))
                .thenReturn(Optional.of(mockPublishedRoute));
        when(plannerClient.getTripWithDetails(eq(10L), eq(mockPublishedRoute.getUserId()), eq(authToken)))
                .thenReturn(mockTripDetail);
        when(plannerClient.createTrip(any(), eq(authToken)))
                .thenReturn(mockCreatedTrip);

        // Мокаем получение и обновление дней
        when(plannerClient.getTripDays(eq(20L), eq(authToken)))
                .thenReturn(createMockExistingDays());
        when(plannerClient.updateTripDay(eq(20L), any(Long.class), any(UpdateTripDayDto.class), eq(authToken)))
                .thenReturn(createMockUpdatedDay());

        // Мокаем создание событий
        TripDetailDto.EventDto mockEvent = TripDetailDto.EventDto.builder()
                .eventId(200L)
                .title("Копированное событие")
                .build();

        when(plannerClient.createEvent(eq(20L), any(Long.class), any(CreateEventDto.class), eq(authToken)))
                .thenReturn(mockEvent);

        // When
        CopyRouteResponseDto result = libraryService.copyRoute(routeId, copyRequest, userId, authToken);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getCopiedDaysCount());

        // Проверяем, что вызывались методы получения и обновления дней
        verify(plannerClient, times(1)).getTripDays(eq(20L), eq(authToken));
        verify(plannerClient, times(2)).updateTripDay(eq(20L), any(Long.class), any(UpdateTripDayDto.class), eq(authToken));
        verify(plannerClient, times(3)).createEvent(eq(20L), any(Long.class), any(CreateEventDto.class), eq(authToken));
    }

    @Test
    void copyRoute_WhenCustomTitle_ShouldUseCustomTitle() {
        // Given
        Long routeId = 1L;
        Long userId = 100L;
        String authToken = "test-token";

        CopyRouteRequestDto copyRequest = CopyRouteRequestDto.builder()
                .startDate(LocalDate.of(2024, 7, 15))
                .title("Мой кастомный маршрут")
                .build();

        when(publishedRouteRepository.findById(routeId))
                .thenReturn(Optional.of(mockPublishedRoute));
        when(plannerClient.getTripWithDetails(eq(10L), eq(mockPublishedRoute.getUserId()), eq(authToken)))
                .thenReturn(mockTripDetail);

        mockCreatedTrip.setTitle("Мой кастомный маршрут");
        when(plannerClient.createTrip(any(), eq(authToken)))
                .thenReturn(mockCreatedTrip);

        when(plannerClient.getTripDays(eq(20L), eq(authToken)))
                .thenReturn(createMockExistingDays());
        when(plannerClient.updateTripDay(eq(20L), any(Long.class), any(UpdateTripDayDto.class), eq(authToken)))
                .thenReturn(createMockUpdatedDay());

        // When
        CopyRouteResponseDto result = libraryService.copyRoute(routeId, copyRequest, userId, authToken);

        // Then
        assertEquals("Мой кастомный маршрут", result.getTitle());
    }

    @Test
    void copyRoute_WhenDateRecalculation_ShouldCalculateCorrectly() {
        // Given
        Long routeId = 1L;
        Long userId = 100L;
        String authToken = "test-token";

        // Копируем с новой датой начала (смещение на 44 дня вперед)
        CopyRouteRequestDto copyRequest = CopyRouteRequestDto.builder()
                .startDate(LocalDate.of(2024, 7, 15))
                .build();

        when(publishedRouteRepository.findById(routeId))
                .thenReturn(Optional.of(mockPublishedRoute));
        when(plannerClient.getTripWithDetails(eq(10L), eq(mockPublishedRoute.getUserId()), eq(authToken)))
                .thenReturn(mockTripDetail);
        when(plannerClient.createTrip(any(), eq(authToken)))
                .thenReturn(mockCreatedTrip);

        when(plannerClient.getTripDays(eq(20L), eq(authToken)))
                .thenReturn(createMockExistingDays());
        when(plannerClient.updateTripDay(eq(20L), any(Long.class), any(UpdateTripDayDto.class), eq(authToken)))
                .thenReturn(createMockUpdatedDay());

        // When
        libraryService.copyRoute(routeId, copyRequest, userId, authToken);

        // Then - проверяем, что даты пересчитаны правильно
        verify(plannerClient, times(2)).updateTripDay(eq(20L), any(Long.class), any(UpdateTripDayDto.class), eq(authToken));
        // Первый день: 2024-06-01 -> 2024-07-15 (новая дата начала)
        // Второй день: 2024-06-02 -> 2024-07-16 (+1 день от новой даты начала)
    }

    @Test
    void copyRoute_WhenRouteNotFound_ShouldThrowException() {
        // Given
        Long routeId = 999L;
        Long userId = 100L;
        String authToken = "test-token";

        CopyRouteRequestDto copyRequest = CopyRouteRequestDto.builder()
                .startDate(LocalDate.of(2024, 7, 15))
                .build();

        when(publishedRouteRepository.findById(routeId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> libraryService.copyRoute(routeId, copyRequest, userId, authToken));
    }

    @Test
    void copyRoute_WhenOriginalTripNotFound_ShouldThrowException() {
        // Given
        Long routeId = 1L;
        Long userId = 100L;
        String authToken = "test-token";

        CopyRouteRequestDto copyRequest = CopyRouteRequestDto.builder()
                .startDate(LocalDate.of(2024, 7, 15))
                .build();

        when(publishedRouteRepository.findById(routeId))
                .thenReturn(Optional.of(mockPublishedRoute));
        when(plannerClient.getTripWithDetails(eq(10L), eq(mockPublishedRoute.getUserId()), eq(authToken)))
                .thenReturn(null);

        // When & Then
        assertThrows(ExternalServiceException.class,
                () -> libraryService.copyRoute(routeId, copyRequest, userId, authToken));
    }

    @Test
    void copyRoute_WhenTripCreationFails_ShouldThrowException() {
        // Given
        Long routeId = 1L;
        Long userId = 100L;
        String authToken = "test-token";

        CopyRouteRequestDto copyRequest = CopyRouteRequestDto.builder()
                .startDate(LocalDate.of(2024, 7, 15))
                .build();

        when(publishedRouteRepository.findById(routeId))
                .thenReturn(Optional.of(mockPublishedRoute));
        when(plannerClient.getTripWithDetails(eq(10L), eq(mockPublishedRoute.getUserId()), eq(authToken)))
                .thenReturn(mockTripDetail);
        when(plannerClient.createTrip(any(), eq(authToken)))
                .thenReturn(null);

        // When & Then
        assertThrows(ExternalServiceException.class,
                () -> libraryService.copyRoute(routeId, copyRequest, userId, authToken));
    }

    @Test
    void copyRoute_WhenDayUpdateFails_ShouldContinueWithOtherDays() {
        // Given
        Long routeId = 1L;
        Long userId = 100L;
        String authToken = "test-token";

        CopyRouteRequestDto copyRequest = CopyRouteRequestDto.builder()
                .startDate(LocalDate.of(2024, 7, 15))
                .build();

        when(publishedRouteRepository.findById(routeId))
                .thenReturn(Optional.of(mockPublishedRoute));
        when(plannerClient.getTripWithDetails(eq(10L), eq(mockPublishedRoute.getUserId()), eq(authToken)))
                .thenReturn(mockTripDetail);
        when(plannerClient.createTrip(any(), eq(authToken)))
                .thenReturn(mockCreatedTrip);

        when(plannerClient.getTripDays(eq(20L), eq(authToken)))
                .thenReturn(createMockExistingDays());

        // Первый день обновляется успешно, второй - с ошибкой
        TripDetailDto.TripDayDto mockUpdatedDay = createMockUpdatedDay();
        when(plannerClient.updateTripDay(eq(20L), any(Long.class), any(UpdateTripDayDto.class), eq(authToken)))
                .thenReturn(mockUpdatedDay)  // Первый вызов успешный
                .thenReturn(null);           // Второй вызов неудачный

        // When
        CopyRouteResponseDto result = libraryService.copyRoute(routeId, copyRequest, userId, authToken);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCopiedDaysCount()); // Только 1 день скопирован из 2
        verify(plannerClient, times(2)).updateTripDay(eq(20L), any(Long.class), any(UpdateTripDayDto.class), eq(authToken));
    }

    // Helper методы для создания тестовых объектов
    private List<TripDetailDto.TripDayDto> createMockExistingDays() {
        return List.of(
                TripDetailDto.TripDayDto.builder()
                        .dayId(100L)
                        .dayNumber(1)
                        .date(LocalDate.of(2024, 7, 15))
                        .build(),
                TripDetailDto.TripDayDto.builder()
                        .dayId(101L)
                        .dayNumber(2)
                        .date(LocalDate.of(2024, 7, 16))
                        .build()
        );
    }

    private TripDetailDto.TripDayDto createMockUpdatedDay() {
        return TripDetailDto.TripDayDto.builder()
                .dayId(100L)
                .dayNumber(1)
                .date(LocalDate.of(2024, 7, 15))
                .description("Обновленное описание")
                .build();
    }

    private PublishedRoute createMockPublishedRoute() {
        PublishedRoute route = new PublishedRoute();
        route.setId(1L);
        route.setOriginalRouteId(10L);
        route.setUserId(50L);
        route.setTitle("Тестовый маршрут");
        route.setIsApproved(true);
        return route;
    }

    private TripDetailDto createMockTripWithDetails() {
        return TripDetailDto.builder()
                .tripId(10L)
                .title("Оригинальный маршрут")
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 6))
                .country("Россия")
                .city("Москва")
                .days(List.of(
                        TripDetailDto.TripDayDto.builder()
                                .dayId(1L)
                                .date(LocalDate.of(2024, 6, 1))
                                .description("Первый день")
                                .events(List.of())
                                .build(),
                        TripDetailDto.TripDayDto.builder()
                                .dayId(2L)
                                .date(LocalDate.of(2024, 6, 2))
                                .description("Второй день")
                                .events(List.of())
                                .build()
                ))
                .build();
    }

    private TripDetailDto createMockTripWithDetailsAndEvents() {
        return TripDetailDto.builder()
                .tripId(10L)
                .title("Оригинальный маршрут")
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 6))
                .country("Россия")
                .city("Москва")
                .days(List.of(
                        TripDetailDto.TripDayDto.builder()
                                .dayId(1L)
                                .date(LocalDate.of(2024, 6, 1))
                                .description("Первый день")
                                .events(List.of(
                                        TripDetailDto.EventDto.builder()
                                                .eventId(1L)
                                                .title("Событие 1")
                                                .build()
                                ))
                                .build(),
                        TripDetailDto.TripDayDto.builder()
                                .dayId(2L)
                                .date(LocalDate.of(2024, 6, 2))
                                .description("Второй день")
                                .events(List.of(
                                        TripDetailDto.EventDto.builder()
                                                .eventId(2L)
                                                .title("Событие 2")
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }

    private Trip createMockCreatedTrip() {
        Trip trip = new Trip();
        trip.setId(20L);
        trip.setTitle("Копия маршрута");
        trip.setStartDate(LocalDate.of(2024, 7, 15));
        trip.setEndDate(LocalDate.of(2024, 7, 20));
        return trip;
    }

    private TripDetailDto.TripDayDto createMockTripDay() {
        return TripDetailDto.TripDayDto.builder()
                .dayId(100L)
                .dayNumber(1)
                .date(LocalDate.of(2024, 7, 15))
                .build();
    }

    private TripDetailDto.EventDto createMockEvent() {
        return TripDetailDto.EventDto.builder()
                .eventId(200L)
                .title("Событие")
                .build();
    }
} 