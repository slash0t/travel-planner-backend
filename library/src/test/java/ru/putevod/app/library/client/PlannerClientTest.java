package ru.putevod.app.library.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.putevod.app.library.dto.planner.CreateTripDto;
import ru.putevod.app.library.dto.planner.TripDetailDto;
import ru.putevod.app.library.dto.planner.CreateTripDayDto;
import ru.putevod.app.library.dto.planner.CreateEventDto;
import ru.putevod.app.library.dto.planner.UpdateTripDayDto;
import ru.putevod.app.library.entity.Trip;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlannerClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private PlannerClient plannerClient;
    private static final String TEST_TOKEN = "test-token";
    private static final Long TEST_TRIP_ID = 1L;
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_DAY_ID = 1L;

    @BeforeEach
    void setUp() {
        plannerClient = new PlannerClient(webClientBuilder);
        ReflectionTestUtils.setField(plannerClient, "plannerServiceUrl", "http://localhost:8082");

        doReturn(webClientBuilder).when(webClientBuilder).baseUrl(anyString());
        doReturn(webClientBuilder).when(webClientBuilder).defaultHeader(anyString(), anyString());
        doReturn(webClient).when(webClientBuilder).build();
    }

    @Test
    @DisplayName("getRouteDetails - Should return trip details on successful response")
    void getRouteDetails_ShouldReturnTripDetailsOnSuccessfulResponse() {
        Trip expectedTrip = new Trip();
        expectedTrip.setId(TEST_TRIP_ID);
        expectedTrip.setTitle("Test Trip");

        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), anyLong());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(Mono.just(expectedTrip)).when(responseSpec).bodyToMono(Trip.class);

        Trip result = plannerClient.getRouteDetails(TEST_TRIP_ID, TEST_TOKEN);

        assertNotNull(result);
        assertEquals(TEST_TRIP_ID, result.getId());
        assertEquals("Test Trip", result.getTitle());
        verify(webClient).get();
    }

    @Test
    @DisplayName("getTripWithDetails - Should return trip details with days and events")
    void getTripWithDetails_ShouldReturnTripDetailsWithDaysAndEvents() {
        Object expectedTrip = createPlannerTripDto();

        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), anyLong(), anyLong());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(Mono.just(expectedTrip)).when(responseSpec).bodyToMono(any(Class.class));

        TripDetailDto result = plannerClient.getTripWithDetails(TEST_TRIP_ID, TEST_USER_ID, TEST_TOKEN);

        assertNotNull(result);
        assertEquals(TEST_TRIP_ID, result.getTripId());
        assertEquals("Test Trip", result.getTitle());
        verify(webClient).get();
    }

    @Test
    @DisplayName("createTrip - Should create new trip")
    void createTrip_ShouldCreateNewTrip() {
        CreateTripDto createTripDto = new CreateTripDto();
        createTripDto.setTitle("New Trip");
        createTripDto.setStartDate(LocalDate.now());

        Trip expectedTrip = new Trip();
        expectedTrip.setId(TEST_TRIP_ID);
        expectedTrip.setTitle("New Trip");

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(Mono.just(expectedTrip)).when(responseSpec).bodyToMono(Trip.class);

        Trip result = plannerClient.createTrip(createTripDto, TEST_TOKEN);

        assertNotNull(result);
        assertEquals(TEST_TRIP_ID, result.getId());
        assertEquals("New Trip", result.getTitle());
        verify(webClient).post();
    }

    @Test
    @DisplayName("createTripDay - Should create new trip day")
    void createTripDay_ShouldCreateNewTripDay() {
        CreateTripDayDto createDayDto = new CreateTripDayDto();
        createDayDto.setDate(LocalDate.now());

        Object expectedDay = createPlannerTripDayDto();

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString(), anyLong());
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(Mono.just(expectedDay)).when(responseSpec).bodyToMono(any(Class.class));

        TripDetailDto.TripDayDto result = plannerClient.createTripDay(TEST_TRIP_ID, createDayDto, TEST_TOKEN);

        assertNotNull(result);
        assertEquals(TEST_DAY_ID, result.getDayId());
        verify(webClient).post();
    }

    @Test
    @DisplayName("createEvent - Should create new event")
    void createEvent_ShouldCreateNewEvent() {
        CreateEventDto createEventDto = new CreateEventDto();
        createEventDto.setTitle("New Event");
        createEventDto.setStartTime(LocalTime.of(10, 0));

        Object expectedEvent = createPlannerEventDto();

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString(), anyLong(), anyLong());
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(Mono.just(expectedEvent)).when(responseSpec).bodyToMono(any(Class.class));

        TripDetailDto.EventDto result = plannerClient.createEvent(TEST_TRIP_ID, TEST_DAY_ID, createEventDto, TEST_TOKEN);

        assertNotNull(result);
        assertEquals(1L, result.getEventId());
        assertEquals("New Event", result.getTitle());
        verify(webClient).post();
    }

    @Test
    @DisplayName("canPublishRoute - Should return true when user has permission")
    void canPublishRoute_ShouldReturnTrueWhenUserHasPermission() {
        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), anyLong(), anyLong());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(Mono.just(true)).when(responseSpec).bodyToMono(Boolean.class);

        boolean result = plannerClient.canPublishRoute(TEST_TRIP_ID, TEST_USER_ID, TEST_TOKEN);

        assertTrue(result);
        verify(webClient).get();
    }

    @Test
    @DisplayName("publishRoute - Should publish route")
    void publishRoute_ShouldPublishRoute() {
        Trip expectedTrip = new Trip();
        expectedTrip.setId(TEST_TRIP_ID);
        expectedTrip.setTitle("Test Trip");
        expectedTrip.setIsPublic(true);

        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString(), anyLong(), anyBoolean());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(Mono.just(expectedTrip)).when(responseSpec).bodyToMono(Trip.class);

        Trip result = plannerClient.publishRoute(TEST_TRIP_ID, TEST_USER_ID, TEST_TOKEN, true);

        assertNotNull(result);
        assertEquals(TEST_TRIP_ID, result.getId());
        assertTrue(result.getIsPublic());
        verify(webClient).post();
    }

    @Test
    @DisplayName("updateTripDay - Should update trip day")
    void updateTripDay_ShouldUpdateTripDay() {
        UpdateTripDayDto updateDayDto = new UpdateTripDayDto();
        updateDayDto.setNote("Updated note");

        Object expectedDay = createPlannerTripDayDto();

        doReturn(requestBodyUriSpec).when(webClient).put();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString(), anyLong(), anyLong());
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(Mono.just(expectedDay)).when(responseSpec).bodyToMono(any(Class.class));

        TripDetailDto.TripDayDto result = plannerClient.updateTripDay(TEST_TRIP_ID, TEST_DAY_ID, updateDayDto, TEST_TOKEN);

        assertNotNull(result);
        assertEquals(TEST_DAY_ID, result.getDayId());
        verify(webClient).put();
    }

    private Object createPlannerTripDto() {
        try {
            Class<?> dtoClass = Class.forName("ru.putevod.app.library.client.PlannerClient$PlannerTripDto");
            Object dto = dtoClass.getDeclaredConstructor().newInstance();
            dtoClass.getMethod("setId", Long.class).invoke(dto, TEST_TRIP_ID);
            dtoClass.getMethod("setTitle", String.class).invoke(dto, "Test Trip");
            dtoClass.getMethod("setStartDate", LocalDate.class).invoke(dto, LocalDate.now());
            dtoClass.getMethod("setEndDate", LocalDate.class).invoke(dto, LocalDate.now().plusDays(5));
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PlannerTripDto", e);
        }
    }

    private Object createPlannerTripDayDto() {
        try {
            Class<?> dtoClass = Class.forName("ru.putevod.app.library.client.PlannerClient$PlannerTripDayDto");
            Object dto = dtoClass.getDeclaredConstructor().newInstance();
            dtoClass.getMethod("setId", Long.class).invoke(dto, TEST_DAY_ID);
            dtoClass.getMethod("setDate", LocalDate.class).invoke(dto, LocalDate.now());
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PlannerTripDayDto", e);
        }
    }

    private Object createPlannerEventDto() {
        try {
            Class<?> dtoClass = Class.forName("ru.putevod.app.library.client.PlannerClient$PlannerEventDto");
            Object dto = dtoClass.getDeclaredConstructor().newInstance();
            dtoClass.getMethod("setId", Long.class).invoke(dto, 1L);
            dtoClass.getMethod("setTitle", String.class).invoke(dto, "New Event");
            dtoClass.getMethod("setStartTime", LocalTime.class).invoke(dto, LocalTime.of(10, 0));
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PlannerEventDto", e);
        }
    }

    private Object[] createPlannerTripDayDtoArray() {
        try {
            Class<?> dtoClass = Class.forName("ru.putevod.app.library.client.PlannerClient$PlannerTripDayDto");
            Object[] array = (Object[]) java.lang.reflect.Array.newInstance(dtoClass, 2);
            array[0] = createPlannerTripDayDto();
            array[1] = createPlannerTripDayDto();
            return array;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PlannerTripDayDto array", e);
        }
    }
} 