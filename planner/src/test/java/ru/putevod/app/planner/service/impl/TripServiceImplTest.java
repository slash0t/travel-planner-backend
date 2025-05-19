package ru.putevod.app.planner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import ru.putevod.app.planner.repository.TripRepository;
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
    private UserService userService;

    @Mock
    private TripMapper tripMapper;

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

        tripEntityFromMapper = new Trip();
        tripEntityFromMapper.setTitle("Test Trip");
        tripEntityFromMapper.setStartDate(LocalDate.now().plusDays(10));
        tripEntityFromMapper.setEndDate(LocalDate.now().plusDays(17));

        Trip tripEntityToSave = new Trip();
        tripEntityToSave.setTitle("Test Trip");
        tripEntityToSave.setStartDate(LocalDate.now().plusDays(10));
        tripEntityToSave.setEndDate(LocalDate.now().plusDays(17));
        tripEntityToSave.setCreator(currentUser);
        tripEntityToSave.setDeleted(false);

        savedTripEntity = new Trip();
        savedTripEntity.setTripId(100L);
        savedTripEntity.setTitle("Test Trip");
        savedTripEntity.setStartDate(LocalDate.now().plusDays(10));
        savedTripEntity.setEndDate(LocalDate.now().plusDays(17));
        savedTripEntity.setCreator(currentUser);
        savedTripEntity.setDeleted(false);

        createdTripDto = new TripDto();
        createdTripDto.setId(100L);
        createdTripDto.setTitle("Test Trip");
        createdTripDto.setStartDate(LocalDate.now().plusDays(10));
        createdTripDto.setEndDate(LocalDate.now().plusDays(17));
        createdTripDto.setCreator(currentUserDto);

        tripDtoToUpdate = new TripDto();
        tripDtoToUpdate.setTitle("Updated Test Trip");
        tripDtoToUpdate.setDescription("Updated Description");
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
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user is not found during trip creation")
    void createTrip_UserNotFound() {
        // Arrange
        Long nonExistentUserId = 999L;
        String expectedErrorMessage = "Пользователь с ID " + nonExistentUserId + " не найден"; // Or match the actual message

        // Mock userService to throw exception
        when(userService.getUserEntityById(nonExistentUserId))
                .thenThrow(new ResourceNotFoundException("Пользователь", "ID", nonExistentUserId));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            tripService.createTrip(nonExistentUserId, tripDtoToCreate);
        });

        // Optionally assert on the exception message if it's consistent
        // assertTrue(exception.getMessage().contains(expectedErrorMessage));

        // Verify that repository and mapper methods were NOT called
        verify(tripMapper, never()).toEntity(any(TripDto.class));
        verify(tripRepository, never()).save(any(Trip.class));
        verify(tripAccessRepository, never()).save(any(TripAccess.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when trip repository fails during trip creation")
    void createTrip_RepositoryError() {
        // Arrange
        Long currentUserId = currentUser.getUserId();
        String expectedErrorMessage = "Database error"; // Or match the actual exception type/message if specific

        when(userService.getUserEntityById(currentUserId)).thenReturn(currentUser);
        when(tripMapper.toEntity(any(TripDto.class))).thenReturn(tripEntityFromMapper);

        // Mock tripRepository.save to throw an exception
        when(tripRepository.save(any(Trip.class)))
                .thenThrow(new RuntimeException(expectedErrorMessage));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tripService.createTrip(currentUserId, tripDtoToCreate);
        });

        // Optionally assert on the exception message
        // assertEquals(expectedErrorMessage, exception.getMessage());

        // Verify interactions up to the point of failure
        verify(userService, times(1)).getUserEntityById(currentUserId);
        verify(tripMapper, times(1)).toEntity(tripDtoToCreate);
        verify(tripRepository, times(1)).save(any(Trip.class));
        // Verify tripAccessRepository and final toDto mapping were NOT called
        verify(tripAccessRepository, never()).save(any(TripAccess.class));
        verify(tripMapper, never()).toDto(any(Trip.class));
    }

    // --- Tests for getTripById --- 

    @Test
    @DisplayName("Should return TripDto when trip exists and user has access")
    void getTripById_Success() {
        // Arrange
        Long userId = currentUser.getUserId();
        Long tripId = savedTripEntity.getTripId();

        // Mock fetching the user
        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        // Mock fetching the trip
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(savedTripEntity));
        // Mock the access check (simulate having access)
        // Since hasAccessToTrip is complex, we might need to mock it directly if it were public,
        // but here we assume the default behavior within getTripEntityWithAccessCheck works if trip exists and user is creator.
        // Alternatively, we mock TripAccessRepository if needed, but let's rely on the creator check for now.
        // We assume savedTripEntity's creator is currentUser, set up in @BeforeEach

        // Mock the final mapping
        when(tripMapper.toDto(savedTripEntity)).thenReturn(createdTripDto);

        // Act
        TripDto result = tripService.getTripById(userId, tripId);

        // Assert
        assertNotNull(result);
        assertEquals(createdTripDto.getId(), result.getId());
        assertEquals(createdTripDto.getTitle(), result.getTitle());

        // Verify interactions
        verify(userService, times(2)).getUserEntityById(userId);
        verify(tripRepository, times(1)).findById(tripId);
        // Verify tripAccessRepository was likely checked internally by hasAccessToTrip
        // verify(tripAccessRepository, times(1)).findByTripAndUser(any(Trip.class), any(User.class)); // If checking explicit access
        verify(tripMapper, times(1)).toDto(savedTripEntity);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when trip does not exist")
    void getTripById_TripNotFound() {
        // Arrange
        Long userId = currentUser.getUserId();
        Long nonExistentTripId = 999L;

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        // Mock repository returning empty Optional
        when(tripRepository.findById(nonExistentTripId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            tripService.getTripById(userId, nonExistentTripId);
        });

        // Assert exception message if needed
        // assertTrue(exception.getMessage().contains("Поездка"));
        // assertTrue(exception.getMessage().contains(String.valueOf(nonExistentTripId)));

        // Verify no mapping occurred
        verify(tripMapper, never()).toDto(any(Trip.class));
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when user does not have access")
    void getTripById_AccessDenied() {
        // Arrange
        Long userId = currentUser.getUserId();
        Long tripId = savedTripEntity.getTripId();

        // Create a different user who is NOT the creator
        User anotherUser = new User();
        anotherUser.setUserId(2L);
        anotherUser.setUsername("anotheruser");

        // Mock fetching the *different* user
        when(userService.getUserEntityById(anotherUser.getUserId())).thenReturn(anotherUser);
        // Mock fetching the trip (which belongs to currentUser)
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(savedTripEntity));

        // Mock TripAccessRepository to return no access record for 'anotherUser'
        // The getTripEntityWithAccessCheck method calls hasAccessToTrip, which checks the creator OR TripAccess table.
        // We need to ensure TripAccessRepository check fails for this user.
        when(tripAccessRepository.findByTripAndUser(savedTripEntity, anotherUser)).thenReturn(Optional.empty());

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            tripService.getTripById(anotherUser.getUserId(), tripId);
        });

        // Assert message
        assertEquals("У вас нет доступа к этой поездке", exception.getMessage());

        // Verify no mapping occurred
        verify(tripMapper, never()).toDto(any(Trip.class));
        // Verify access repo was checked
        verify(tripAccessRepository, times(1)).findByTripAndUser(savedTripEntity, anotherUser);
    }

    // --- Tests for updateTrip ---

    @Test
    @DisplayName("Should update trip successfully when user has access")
    void updateTrip_Success() {
        // Arrange
        Long userId = currentUser.getUserId(); // Assume creator has admin/write access
        Long tripId = savedTripEntity.getTripId();
        String updatedTitle = "Updated Test Trip";
        tripDtoToUpdate.setTitle(updatedTitle);

        // Mock user fetch
        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        // Mock trip fetch for access check
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(savedTripEntity));
        // Assume creator has necessary access (admin), so access check passes
        // Mock access repository if testing non-creator with specific 'write' access
        // when(tripAccessRepository.findByTripAndUser(any(), any())).thenReturn(Optional.of(someWriteAccessRecord));

        // Mock repository save: returns the *same* entity instance after update
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock the final mapping (using the updated entity)
        TripDto updatedTripDto = new TripDto(); // DTO reflecting the update
        updatedTripDto.setId(tripId);
        updatedTripDto.setTitle(updatedTitle);
        updatedTripDto.setCreator(currentUserDto);
        // Set other fields same as createdTripDto or updated
        when(tripMapper.toDto(any(Trip.class))).thenReturn(updatedTripDto);

        // Use ArgumentCaptor to capture the entity passed to the mapper's update method
        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);

        // Act
        TripDto result = tripService.updateTrip(userId, tripId, tripDtoToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(tripId, result.getId());
        assertEquals(updatedTitle, result.getTitle()); // Check if the title was updated

        // Verify interactions
        verify(userService, times(2)).getUserEntityById(userId); // Called in updateTrip and access check
        verify(tripRepository, times(1)).findById(tripId); // Called in access check
        // Verify the mapper's updateEntityFromDto was called
        verify(tripMapper, times(1)).updateEntityFromDto(eq(tripDtoToUpdate), tripCaptor.capture());
        assertEquals(savedTripEntity, tripCaptor.getValue()); // Ensure the correct entity was passed to mapper update
        // Verify repository save was called with the updated entity
        verify(tripRepository, times(1)).save(tripCaptor.getValue());
        // Verify the final mapping to DTO
        verify(tripMapper, times(1)).toDto(tripCaptor.getValue());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent trip")
    void updateTrip_TripNotFound() {
        // Arrange
        Long userId = currentUser.getUserId();
        Long nonExistentTripId = 999L;

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        // Mock repository returning empty for access check
        when(tripRepository.findById(nonExistentTripId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            tripService.updateTrip(userId, nonExistentTripId, tripDtoToUpdate);
        });

        // Verify that mapper and save were not called
        verify(tripMapper, never()).updateEntityFromDto(any(), any());
        verify(tripRepository, never()).save(any());
        verify(tripMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when updating trip without access")
    void updateTrip_AccessDenied() {
        // Arrange
        Long tripId = savedTripEntity.getTripId();
        User anotherUser = new User();
        anotherUser.setUserId(2L);
        anotherUser.setUsername("anotheruser");
        Long anotherUserId = anotherUser.getUserId();

        when(userService.getUserEntityById(anotherUserId)).thenReturn(anotherUser);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(savedTripEntity));
        // Mock access check to fail (user is not creator and no access record)
        when(tripAccessRepository.findByTripAndUser(savedTripEntity, anotherUser)).thenReturn(Optional.empty());

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            tripService.updateTrip(anotherUserId, tripId, tripDtoToUpdate);
        });

        assertEquals("У вас нет доступа к этой поездке", exception.getMessage());

        // Verify that mapper and save were not called
        verify(tripMapper, never()).updateEntityFromDto(any(), any());
        verify(tripRepository, never()).save(any());
        verify(tripMapper, never()).toDto(any());
        // Verify access repo was checked
        verify(tripAccessRepository, times(1)).findByTripAndUser(savedTripEntity, anotherUser);

    }

    // --- Tests for deleteTrip ---

    @Test
    @DisplayName("Should soft delete trip successfully when user is admin")
    void deleteTrip_Success() {
        // Arrange
        Long userId = currentUser.getUserId(); // Assume creator is admin
        Long tripId = savedTripEntity.getTripId();

        // Mock user fetch
        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        // Mock trip fetch for access check
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(savedTripEntity));
        // Assume creator has necessary access (admin), so access check passes

        // Use ArgumentCaptor to verify the entity state when saved
        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        // Mock save to capture the argument
        when(tripRepository.save(tripCaptor.capture())).thenReturn(null); // Return value doesn't matter here

        // Act
        assertDoesNotThrow(() -> {
            tripService.deleteTrip(userId, tripId);
        });

        // Assert
        // Verify the captured entity has isDeleted set to true
        assertNotNull(tripCaptor.getValue());
        assertTrue(tripCaptor.getValue().isDeleted());
        assertEquals(tripId, tripCaptor.getValue().getTripId()); // Ensure it's the correct trip

        // Verify interactions
        verify(userService, times(2)).getUserEntityById(userId); // Called in deleteTrip and access check
        verify(tripRepository, times(1)).findById(tripId); // Called in access check
        verify(tripRepository, times(1)).save(any(Trip.class)); // Verify save was called
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent trip")
    void deleteTrip_TripNotFound() {
        // Arrange
        Long userId = currentUser.getUserId();
        Long nonExistentTripId = 999L;

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        // Mock repository returning empty for access check
        when(tripRepository.findById(nonExistentTripId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            tripService.deleteTrip(userId, nonExistentTripId);
        });

        // Verify save was not called
        verify(tripRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when deleting trip without admin access")
    void deleteTrip_AccessDenied() {
        // Arrange
        Long tripId = savedTripEntity.getTripId();
        User anotherUser = new User();
        anotherUser.setUserId(2L);
        anotherUser.setUsername("anotheruser");
        Long anotherUserId = anotherUser.getUserId();

        when(userService.getUserEntityById(anotherUserId)).thenReturn(anotherUser);
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(savedTripEntity));
        // Mock access check to fail (user is not creator and no access record indicating admin)
        // Need to ensure hasAccessToTrip check for "admin" fails
        when(tripAccessRepository.findByTripAndUser(savedTripEntity, anotherUser)).thenReturn(Optional.empty());

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            tripService.deleteTrip(anotherUserId, tripId);
        });

        assertEquals("У вас нет доступа к этой поездке", exception.getMessage());

        // Verify save was not called
        verify(tripRepository, never()).save(any());
        // Verify access repo was checked
        verify(tripAccessRepository, times(1)).findByTripAndUser(savedTripEntity, anotherUser);
    }

    // --- Tests for getUserTrips ---

    @Test
    @DisplayName("Should return page of created trips for filter 'created'")
    void getUserTrips_FilterCreated_Success() {
        // Arrange
        Long userId = currentUser.getUserId();
        Pageable pageable = PageRequest.of(0, 10);
        List<Trip> createdTrips = List.of(savedTripEntity); // Assume savedTripEntity was created by currentUser
        Page<Trip> tripPage = new PageImpl<>(createdTrips, pageable, createdTrips.size());

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        // Mock the specific repository method for 'created' filter
        when(tripRepository.findAllByCreator(currentUser, pageable)).thenReturn(tripPage);
        // Mock mapping for each trip in the page
        when(tripMapper.toDto(savedTripEntity)).thenReturn(createdTripDto);

        // Act
        Page<TripDto> result = tripService.getUserTrips(userId, "created", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(createdTripDto.getId(), result.getContent().get(0).getId());

        // Verify interactions
        verify(userService, times(1)).getUserEntityById(userId);
        verify(tripRepository, times(1)).findAllByCreator(currentUser, pageable);
        verify(tripRepository, never()).findAllSharedWithUser(any(), any());
        verify(tripRepository, never()).findAllAvailableToUser(any(), any());
        verify(tripMapper, times(1)).toDto(savedTripEntity);
    }

    @Test
    @DisplayName("Should return page of shared trips for filter 'shared'")
    void getUserTrips_FilterShared_Success() {
        // Arrange
        Long userId = currentUser.getUserId();
        Pageable pageable = PageRequest.of(0, 10);
        // Assume savedTripEntity is shared with currentUser (different creator)
        User creator = new User(); creator.setUserId(99L);
        savedTripEntity.setCreator(creator);
        List<Trip> sharedTrips = List.of(savedTripEntity);
        Page<Trip> tripPage = new PageImpl<>(sharedTrips, pageable, sharedTrips.size());

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        // Mock the specific repository method for 'shared' filter
        when(tripRepository.findAllSharedWithUser(currentUser, pageable)).thenReturn(tripPage);
        // Mock mapping
        createdTripDto.setCreator(null); // Adjust DTO mock if creator is different
        when(tripMapper.toDto(savedTripEntity)).thenReturn(createdTripDto);

        // Act
        Page<TripDto> result = tripService.getUserTrips(userId, "shared", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        // Verify interactions
        verify(userService, times(1)).getUserEntityById(userId);
        verify(tripRepository, never()).findAllByCreator(any(), any());
        verify(tripRepository, times(1)).findAllSharedWithUser(currentUser, pageable);
        verify(tripRepository, never()).findAllAvailableToUser(any(), any());
        verify(tripMapper, times(1)).toDto(savedTripEntity);

        // --- Cleanup: Reset creator on shared entity if it affects other tests ---
        savedTripEntity.setCreator(currentUser); // Reset for other tests
        // --- ----------------------------------------------------------------- ---
    }

    @Test
    @DisplayName("Should return page of all available trips for filter 'all'")
    void getUserTrips_FilterAll_Success() {
        // Arrange
        Long userId = currentUser.getUserId();
        Pageable pageable = PageRequest.of(0, 10);
        List<Trip> allTrips = List.of(savedTripEntity);
        Page<Trip> tripPage = new PageImpl<>(allTrips, pageable, allTrips.size());

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        // Mock the specific repository method for 'all' filter
        when(tripRepository.findAllAvailableToUser(currentUser, pageable)).thenReturn(tripPage);
        // Mock mapping
        when(tripMapper.toDto(savedTripEntity)).thenReturn(createdTripDto);

        // Act
        Page<TripDto> result = tripService.getUserTrips(userId, "all", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // Verify interactions
        verify(userService, times(1)).getUserEntityById(userId);
        verify(tripRepository, never()).findAllByCreator(any(), any());
        verify(tripRepository, never()).findAllSharedWithUser(any(), any());
        verify(tripRepository, times(1)).findAllAvailableToUser(currentUser, pageable);
        verify(tripMapper, times(1)).toDto(savedTripEntity);
    }

    @Test
    @DisplayName("Should return empty page when no trips match filter")
    void getUserTrips_NoResults() {
        // Arrange
        Long userId = currentUser.getUserId();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Trip> emptyPage = Page.empty(pageable);

        when(userService.getUserEntityById(userId)).thenReturn(currentUser);
        // Mock the repository method for 'created' filter to return empty
        when(tripRepository.findAllByCreator(currentUser, pageable)).thenReturn(emptyPage);

        // Act
        Page<TripDto> result = tripService.getUserTrips(userId, "created", pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        // Verify interactions
        verify(userService, times(1)).getUserEntityById(userId);
        verify(tripRepository, times(1)).findAllByCreator(currentUser, pageable);
        verify(tripMapper, never()).toDto(any()); // Mapper should not be called
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user is not found for getUserTrips")
    void getUserTrips_UserNotFound() {
        // Arrange
        Long nonExistentUserId = 999L;
        Pageable pageable = PageRequest.of(0, 10);
        String filter = "all"; // Filter doesn't matter as user fetch fails first

        // Mock userService to throw exception
        when(userService.getUserEntityById(nonExistentUserId))
                .thenThrow(new ResourceNotFoundException("Пользователь", "ID", nonExistentUserId));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            tripService.getUserTrips(nonExistentUserId, filter, pageable);
        });

        // Verify no repository methods were called
        verify(tripRepository, never()).findAllByCreator(any(), any());
        verify(tripRepository, never()).findAllSharedWithUser(any(), any());
        verify(tripRepository, never()).findAllAvailableToUser(any(), any());
        verify(tripMapper, never()).toDto(any());
    }

} 