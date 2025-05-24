package ru.putevod.app.planner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.putevod.app.planner.dto.TripDto;
import ru.putevod.app.planner.dto.UserDto;
import ru.putevod.app.planner.mapper.TripMapper;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.TripAccess;
import ru.putevod.app.planner.model.User;
import ru.putevod.app.planner.repository.TripAccessRepository;
import ru.putevod.app.planner.repository.TripDayRepository;
import ru.putevod.app.planner.repository.TripRepository;
import ru.putevod.app.planner.service.TripPreviewService;
import ru.putevod.app.planner.service.UserService;
import ru.putevod.app.planner.exception.ResourceNotFoundException;
import ru.putevod.app.planner.exception.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class TripServiceImplTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripAccessRepository tripAccessRepository;

    @Mock
    private TripDayRepository tripDayRepository;

    @Mock
    private UserService userService;

    @Mock
    private TripMapper tripMapper;

    @Mock
    private TripPreviewService tripPreviewService;

    @InjectMocks
    private TripServiceImpl tripService;

    private User currentUser;
    private UserDto currentUserDto;
    private TripDto tripDtoToCreate;
    private Trip tripEntityFromMapper;
    private Trip savedTripEntity;
    private TripDto createdTripDto;
    private TripDto tripDtoToUpdate;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setUserId(1L);
        currentUser.setUsername("testuser");

        currentUserDto = new UserDto();
        currentUserDto.setId(1L);
        currentUserDto.setUsername("testuser");

        tripDtoToCreate = new TripDto();
        tripDtoToCreate.setTitle("Test Trip");
        tripDtoToCreate.setStartDate(LocalDate.now().plusDays(10));
        tripDtoToCreate.setEndDate(LocalDate.now().plusDays(17));
        tripDtoToCreate.setCountry("Россия");
        tripDtoToCreate.setCity("Москва");

        tripEntityFromMapper = new Trip();
        tripEntityFromMapper.setTitle("Test Trip");
        tripEntityFromMapper.setStartDate(LocalDate.now().plusDays(10));
        tripEntityFromMapper.setEndDate(LocalDate.now().plusDays(17));
        tripEntityFromMapper.setCountry("Россия");
        tripEntityFromMapper.setCity("Москва");

        Trip tripEntityToSave = new Trip();
        tripEntityToSave.setTitle("Test Trip");
        tripEntityToSave.setStartDate(LocalDate.now().plusDays(10));
        tripEntityToSave.setEndDate(LocalDate.now().plusDays(17));
        tripEntityToSave.setCreator(currentUser);
        tripEntityToSave.setDeleted(false);
        tripEntityToSave.setCountry("Россия");
        tripEntityToSave.setCity("Москва");

        savedTripEntity = new Trip();
        savedTripEntity.setTripId(100L);
        savedTripEntity.setTitle("Test Trip");
        savedTripEntity.setStartDate(LocalDate.now().plusDays(10));
        savedTripEntity.setEndDate(LocalDate.now().plusDays(17));
        savedTripEntity.setCreator(currentUser);
        savedTripEntity.setDeleted(false);
        savedTripEntity.setCountry("Россия");
        savedTripEntity.setCity("Москва");

        createdTripDto = new TripDto();
        createdTripDto.setId(100L);
        createdTripDto.setTitle("Test Trip");
        createdTripDto.setStartDate(LocalDate.now().plusDays(10));
        createdTripDto.setEndDate(LocalDate.now().plusDays(17));
        createdTripDto.setCreator(currentUserDto);
        createdTripDto.setCountry("Россия");
        createdTripDto.setCity("Москва");

        tripDtoToUpdate = new TripDto();
        tripDtoToUpdate.setTitle("Updated Test Trip");
        tripDtoToUpdate.setDescription("Updated Description");
        tripDtoToUpdate.setCountry("Россия");
        tripDtoToUpdate.setCity("Москва");
    }

    @Test
    @DisplayName("Should create a trip and associated access successfully")
    void createTrip_Success() {
        Long currentUserId = currentUser.getUserId();

        when(userService.getUserEntityById(currentUserId)).thenReturn(currentUser);
        when(tripMapper.toEntity(any(TripDto.class))).thenReturn(tripEntityFromMapper);
        when(tripRepository.save(any(Trip.class))).thenReturn(savedTripEntity);
        when(tripAccessRepository.save(any(TripAccess.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tripMapper.toDto(any(Trip.class))).thenReturn(createdTripDto);
        when(tripPreviewService.generatePreviewForCity(anyString())).thenReturn("http://example.com/preview.jpg");

        TripDto result = tripService.createTrip(currentUserId, tripDtoToCreate);

        assertNotNull(result);
        assertEquals(createdTripDto.getId(), result.getId());
        assertEquals(createdTripDto.getTitle(), result.getTitle());
        assertNotNull(result.getCreator());
        assertEquals(createdTripDto.getCreator().getId(), result.getCreator().getId());

        verify(userService, times(1)).getUserEntityById(currentUserId);
        verify(tripMapper, times(1)).toEntity(tripDtoToCreate);
        verify(tripRepository, times(1)).save(any(Trip.class));
        verify(tripAccessRepository, times(1)).save(any(TripAccess.class));
        verify(tripMapper, times(1)).toDto(savedTripEntity);
        verify(tripPreviewService, times(1)).generatePreviewForCity(anyString());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user is not found during trip creation")
    void createTrip_UserNotFound() {
        Long nonExistentUserId = 999L;

        when(userService.getUserEntityById(nonExistentUserId))
                .thenThrow(new ResourceNotFoundException("Пользователь", "ID", nonExistentUserId));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            tripService.createTrip(nonExistentUserId, tripDtoToCreate);
        });

        verify(tripMapper, never()).toEntity(any(TripDto.class));
        verify(tripRepository, never()).save(any(Trip.class));
        verify(tripAccessRepository, never()).save(any(TripAccess.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when trip repository fails during trip creation")
    void createTrip_RepositoryError() {
        Long currentUserId = currentUser.getUserId();
        String expectedErrorMessage = "Database error";

        when(userService.getUserEntityById(currentUserId)).thenReturn(currentUser);
        when(tripMapper.toEntity(any(TripDto.class))).thenReturn(tripEntityFromMapper);
        when(tripPreviewService.generatePreviewForCity(anyString())).thenReturn("http://example.com/preview.jpg");
        when(tripRepository.save(any(Trip.class)))
                .thenThrow(new RuntimeException(expectedErrorMessage));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tripService.createTrip(currentUserId, tripDtoToCreate);
        });

        verify(userService, times(1)).getUserEntityById(currentUserId);
        verify(tripMapper, times(1)).toEntity(tripDtoToCreate);
        verify(tripRepository, times(1)).save(any(Trip.class));
        verify(tripAccessRepository, never()).save(any(TripAccess.class));
        verify(tripMapper, never()).toDto(any(Trip.class));
    }

    @Test
    @DisplayName("Should return TripDto when trip exists and user has access")
    void getTripById_Success() {
        Long userId = currentUser.getUserId();
        Long tripId = savedTripEntity.getTripId();

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(savedTripEntity));
        when(tripMapper.toDto(savedTripEntity)).thenReturn(createdTripDto);

        TripDto result = tripService.getTripById(userId, tripId);

        assertNotNull(result);
        assertEquals(createdTripDto.getId(), result.getId());
        assertEquals(createdTripDto.getTitle(), result.getTitle());

        verify(userService, times(2)).getUserEntityById(userId);
        verify(tripRepository, times(1)).findById(tripId);
        verify(tripMapper, times(1)).toDto(savedTripEntity);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when trip does not exist")
    void getTripById_TripNotFound() {
        Long userId = currentUser.getUserId();
        Long nonExistentTripId = 999L;

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        when(tripRepository.findById(nonExistentTripId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            tripService.getTripById(userId, nonExistentTripId);
        });

        verify(tripMapper, never()).toDto(any(Trip.class));
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when user does not have access")
    void getTripById_AccessDenied() {
        Long userId = currentUser.getUserId();
        Long tripId = savedTripEntity.getTripId();

        User anotherUser = new User();
        anotherUser.setUserId(2L);
        anotherUser.setUsername("anotheruser");

        when(userService.getUserEntityById(anotherUser.getUserId())).thenReturn(anotherUser);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(savedTripEntity));
        when(tripAccessRepository.findByTripAndUser(savedTripEntity, anotherUser)).thenReturn(Optional.empty());

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            tripService.getTripById(anotherUser.getUserId(), tripId);
        });

        assertEquals("У вас нет доступа к этой поездке", exception.getMessage());

        verify(tripMapper, never()).toDto(any(Trip.class));
        verify(tripAccessRepository, times(1)).findByTripAndUser(savedTripEntity, anotherUser);
    }

    @Test
    @DisplayName("Should update trip successfully when user has access")
    void updateTrip_Success() {
        Long userId = currentUser.getUserId();
        Long tripId = savedTripEntity.getTripId();
        String updatedTitle = "Updated Test Trip";
        tripDtoToUpdate.setTitle(updatedTitle);

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(savedTripEntity));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TripDto updatedTripDto = new TripDto();
        updatedTripDto.setId(tripId);
        updatedTripDto.setTitle(updatedTitle);
        updatedTripDto.setCreator(currentUserDto);

        when(tripMapper.toDto(any(Trip.class))).thenReturn(updatedTripDto);

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);

        TripDto result = tripService.updateTrip(userId, tripId, tripDtoToUpdate);

        assertNotNull(result);
        assertEquals(tripId, result.getId());
        assertEquals(updatedTitle, result.getTitle());

        verify(userService, times(1)).getUserEntityById(userId);
        verify(tripRepository, times(1)).findById(tripId);
        verify(tripMapper, times(1)).updateEntityFromDto(eq(tripDtoToUpdate), tripCaptor.capture());
        assertEquals(savedTripEntity, tripCaptor.getValue());
        verify(tripRepository, times(1)).save(tripCaptor.getValue());
        verify(tripMapper, times(1)).toDto(tripCaptor.getValue());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent trip")
    void updateTrip_TripNotFound() {
        Long userId = currentUser.getUserId();
        Long nonExistentTripId = 999L;

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        when(tripRepository.findById(nonExistentTripId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            tripService.updateTrip(userId, nonExistentTripId, tripDtoToUpdate);
        });

        verify(tripMapper, never()).updateEntityFromDto(any(), any());
        verify(tripRepository, never()).save(any());
        verify(tripMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when updating trip without access")
    void updateTrip_AccessDenied() {
        Long tripId = savedTripEntity.getTripId();
        User anotherUser = new User();
        anotherUser.setUserId(2L);
        anotherUser.setUsername("anotheruser");
        Long anotherUserId = anotherUser.getUserId();

        when(userService.getUserEntityById(anotherUserId)).thenReturn(anotherUser);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(savedTripEntity));
        when(tripAccessRepository.findByTripAndUser(savedTripEntity, anotherUser)).thenReturn(Optional.empty());

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            tripService.updateTrip(anotherUserId, tripId, tripDtoToUpdate);
        });

        assertEquals("У вас нет доступа к этой поездке", exception.getMessage());

        verify(tripMapper, never()).updateEntityFromDto(any(), any());
        verify(tripRepository, never()).save(any());
        verify(tripMapper, never()).toDto(any());
        verify(tripAccessRepository, times(1)).findByTripAndUser(savedTripEntity, anotherUser);
    }

    @Test
    @DisplayName("Should soft delete trip successfully when user is admin")
    void deleteTrip_Success() {
        Long userId = currentUser.getUserId();
        Long tripId = savedTripEntity.getTripId();

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(savedTripEntity));

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        when(tripRepository.save(tripCaptor.capture())).thenReturn(null);

        assertDoesNotThrow(() -> {
            tripService.deleteTrip(userId, tripId);
        });

        assertNotNull(tripCaptor.getValue());
        assertTrue(tripCaptor.getValue().isDeleted());
        assertEquals(tripId, tripCaptor.getValue().getTripId());

        verify(userService, times(1)).getUserEntityById(userId);
        verify(tripRepository, times(1)).findById(tripId);
        verify(tripRepository, times(1)).save(any(Trip.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent trip")
    void deleteTrip_TripNotFound() {
        Long userId = currentUser.getUserId();
        Long nonExistentTripId = 999L;

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        when(tripRepository.findById(nonExistentTripId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            tripService.deleteTrip(userId, nonExistentTripId);
        });

        verify(tripRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when deleting trip without admin access")
    void deleteTrip_AccessDenied() {
        Long tripId = savedTripEntity.getTripId();
        User anotherUser = new User();
        anotherUser.setUserId(2L);
        anotherUser.setUsername("anotheruser");
        Long anotherUserId = anotherUser.getUserId();

        when(userService.getUserEntityById(anotherUserId)).thenReturn(anotherUser);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(savedTripEntity));
        when(tripAccessRepository.findByTripAndUser(savedTripEntity, anotherUser)).thenReturn(Optional.empty());

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            tripService.deleteTrip(anotherUserId, tripId);
        });

        assertEquals("У вас нет прав на удаление этой поездки", exception.getMessage());

        verify(tripRepository, never()).save(any());
        verify(tripAccessRepository, times(1)).findByTripAndUser(savedTripEntity, anotherUser);
    }

    @Test
    @DisplayName("Should return page of created trips for filter 'created'")
    void getUserTrips_FilterCreated_Success() {
        Long userId = currentUser.getUserId();
        Pageable pageable = PageRequest.of(0, 10);
        List<Trip> createdTrips = List.of(savedTripEntity);
        Page<Trip> tripPage = new PageImpl<>(createdTrips, pageable, createdTrips.size());

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        when(tripRepository.findAllByCreator(currentUser, pageable)).thenReturn(tripPage);
        when(tripMapper.toDto(savedTripEntity)).thenReturn(createdTripDto);

        Page<TripDto> result = tripService.getUserTrips(userId, "created", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(createdTripDto.getId(), result.getContent().get(0).getId());

        verify(userService, times(1)).getUserEntityById(userId);
        verify(tripRepository, times(1)).findAllByCreator(currentUser, pageable);
        verify(tripRepository, never()).findAllSharedWithUser(any(), any());
        verify(tripRepository, never()).findAllAvailableToUser(any(), any());
        verify(tripMapper, times(1)).toDto(savedTripEntity);
    }

    @Test
    @DisplayName("Should return page of shared trips for filter 'shared'")
    void getUserTrips_FilterShared_Success() {
        Long userId = currentUser.getUserId();
        Pageable pageable = PageRequest.of(0, 10);
        User creator = new User();
        creator.setUserId(99L);
        savedTripEntity.setCreator(creator);
        List<Trip> sharedTrips = List.of(savedTripEntity);
        Page<Trip> tripPage = new PageImpl<>(sharedTrips, pageable, sharedTrips.size());

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        when(tripRepository.findAllSharedWithUser(currentUser, pageable)).thenReturn(tripPage);
        createdTripDto.setCreator(null);
        when(tripMapper.toDto(savedTripEntity)).thenReturn(createdTripDto);

        Page<TripDto> result = tripService.getUserTrips(userId, "shared", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        verify(userService, times(1)).getUserEntityById(userId);
        verify(tripRepository, never()).findAllByCreator(any(), any());
        verify(tripRepository, times(1)).findAllSharedWithUser(currentUser, pageable);
        verify(tripRepository, never()).findAllAvailableToUser(any(), any());
        verify(tripMapper, times(1)).toDto(savedTripEntity);

        savedTripEntity.setCreator(currentUser);
    }

    @Test
    @DisplayName("Should return page of all available trips for filter 'all'")
    void getUserTrips_FilterAll_Success() {
        Long userId = currentUser.getUserId();
        Pageable pageable = PageRequest.of(0, 10);
        List<Trip> allTrips = List.of(savedTripEntity);
        Page<Trip> tripPage = new PageImpl<>(allTrips, pageable, allTrips.size());

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        when(tripRepository.findAllAvailableToUser(currentUser, pageable)).thenReturn(tripPage);
        when(tripMapper.toDto(savedTripEntity)).thenReturn(createdTripDto);

        Page<TripDto> result = tripService.getUserTrips(userId, "all", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(userService, times(1)).getUserEntityById(userId);
        verify(tripRepository, never()).findAllByCreator(any(), any());
        verify(tripRepository, never()).findAllSharedWithUser(any(), any());
        verify(tripRepository, times(1)).findAllAvailableToUser(currentUser, pageable);
        verify(tripMapper, times(1)).toDto(savedTripEntity);
    }

    @Test
    @DisplayName("Should return empty page when no trips match filter")
    void getUserTrips_NoResults() {
        Long userId = currentUser.getUserId();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Trip> emptyPage = Page.empty(pageable);

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        when(tripRepository.findAllByCreator(currentUser, pageable)).thenReturn(emptyPage);

        Page<TripDto> result = tripService.getUserTrips(userId, "created", pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(userService, times(1)).getUserEntityById(userId);
        verify(tripRepository, times(1)).findAllByCreator(currentUser, pageable);
        verify(tripMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user is not found for getUserTrips")
    void getUserTrips_UserNotFound() {
        Long nonExistentUserId = 999L;
        Pageable pageable = PageRequest.of(0, 10);
        String filter = "all";

        when(userService.getUserEntityById(nonExistentUserId))
                .thenThrow(new ResourceNotFoundException("Пользователь", "ID", nonExistentUserId));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            tripService.getUserTrips(nonExistentUserId, filter, pageable);
        });

        verify(tripRepository, never()).findAllByCreator(any(), any());
        verify(tripRepository, never()).findAllSharedWithUser(any(), any());
        verify(tripRepository, never()).findAllAvailableToUser(any(), any());
        verify(tripMapper, never()).toDto(any());
    }
} 