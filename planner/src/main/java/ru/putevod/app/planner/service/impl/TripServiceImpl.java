package ru.putevod.app.planner.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.planner.client.AuthServiceClient;
import ru.putevod.app.planner.dto.TripAccessDto;
import ru.putevod.app.planner.dto.TripDto;
import ru.putevod.app.planner.dto.UserDto;
import ru.putevod.app.planner.exception.AccessDeniedException;
import ru.putevod.app.planner.exception.BadRequestException;
import ru.putevod.app.planner.exception.ResourceNotFoundException;
import ru.putevod.app.planner.mapper.TripAccessMapper;
import ru.putevod.app.planner.mapper.TripMapper;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.TripAccess;
import ru.putevod.app.planner.model.User;
import ru.putevod.app.planner.repository.TripAccessRepository;
import ru.putevod.app.planner.repository.TripRepository;
import ru.putevod.app.planner.service.NotificationService;
import ru.putevod.app.planner.service.TripService;
import ru.putevod.app.planner.service.UserService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TripAccessRepository tripAccessRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final TripMapper tripMapper;
    private final TripAccessMapper tripAccessMapper;
    private final AuthServiceClient authServiceClient;

    @Override
    @Transactional
    public TripDto createTrip(Long userId, TripDto tripDto) {
        User user = userService.getUserEntityById(userId);
        
        Trip trip = tripMapper.toEntity(tripDto);
        trip.setCreator(user);
        trip.setDeleted(false);
        
        trip = tripRepository.save(trip);

        TripAccess creatorAccess = TripAccess.builder()
                .trip(trip)
                .user(user)
                .accessLevel("admin")
                .invitationStatus("accepted")
                .build();
        
        tripAccessRepository.save(creatorAccess);
        
        return tripMapper.toDto(trip);
    }

    @Override
    @Transactional
    public TripDto updateTrip(Long userId, Long tripId, TripDto tripDto) {
        User user = userService.getUserEntityById(userId);
        Trip trip = getTripEntityWithAccessCheck(userId, tripId, "admin", "write");
        
        tripMapper.updateEntityFromDto(tripDto, trip);
        trip = tripRepository.save(trip);
        
        return tripMapper.toDto(trip);
    }

    @Override
    @Transactional(readOnly = true)
    public TripDto getTripById(Long userId, Long tripId) {
        User user = userService.getUserEntityById(userId);
        Trip trip = getTripEntityWithAccessCheck(userId, tripId, "admin", "read", "write");
        
        return tripMapper.toDto(trip);
    }

    @Override
    @Transactional(readOnly = true)
    public Trip getTripEntityById(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Поездка", "id", tripId));
    }

    @Override
    @Transactional(readOnly = true)
    public Trip getTripEntityWithAccessCheck(Long userId, Long tripId) {
        return getTripEntityWithAccessCheck(userId, tripId, "admin", "read", "write");
    }

    private Trip getTripEntityWithAccessCheck(Long userId, Long tripId, String... requiredLevels) {
        User user = userService.getUserEntityById(userId);
        Trip trip = getTripEntityById(tripId);
        
        if (!hasAccessToTrip(user, trip, requiredLevels)) {
            throw new AccessDeniedException("У вас нет доступа к этой поездке");
        }
        
        return trip;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TripDto> getUserTrips(Long userId, String filter, Pageable pageable) {
        User user = userService.getUserEntityById(userId);
        Page<Trip> trips = switch (filter.toLowerCase()) {
            case "created" -> tripRepository.findAllByCreator(user, pageable);
            case "shared" -> tripRepository.findAllSharedWithUser(user, pageable);
            default -> tripRepository.findAllAvailableToUser(user, pageable);
        };

        return trips.map(tripMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteTrip(Long userId, Long tripId) {
        User user = userService.getUserEntityById(userId);
        Trip trip = getTripEntityWithAccessCheck(userId, tripId, "admin");
        
        // Выполняем "мягкое" удаление
        trip.setDeleted(true);
        tripRepository.save(trip);
    }

    @Override
    @Transactional
    public TripAccessDto shareTrip(Long userId, Long tripId, TripAccessDto accessDto) {
        User owner = userService.getUserEntityById(userId);
        Trip trip = getTripEntityWithAccessCheck(userId, tripId, "admin");
        
        // Получаем пользователя, которому предоставляется доступ
        User sharedUser = userService.getUserEntityById(accessDto.getUser().getId());
        
        // Проверяем, что пользователь не пытается поделиться с самим собой
        if (userId.equals(sharedUser.getUserId())) {
            throw new BadRequestException("Вы не можете предоставить доступ самому себе");
        }
        
        // Проверяем, нет ли уже доступа у этого пользователя
        if (tripAccessRepository.existsByTripAndUser(trip, sharedUser)) {
            throw new BadRequestException("Доступ для данного пользователя уже существует");
        }
        
        // Создаем запись о доступе
        TripAccess tripAccess = tripAccessMapper.fromDto(accessDto, trip, sharedUser);
        tripAccess.setInvitationStatus("pending");
        
        tripAccess = tripAccessRepository.save(tripAccess);
        
        // Отправляем уведомление пользователю о приглашении
        notificationService.createTripInviteNotification(
                sharedUser.getUserId(),
                tripId, 
                owner.getUsername());
        
        return tripAccessMapper.toDto(tripAccess);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripAccessDto> getTripShares(Long userId, Long tripId) {
        User user = userService.getUserEntityById(userId);
        Trip trip = getTripEntityWithAccessCheck(userId, tripId, "admin", "read");
        
        List<TripAccess> accesses = tripAccessRepository.findByTrip(trip);
        
        return accesses.stream()
                .map(tripAccessMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeShare(Long userId, Long tripId, Long shareUserId) {
        User user = userService.getUserEntityById(userId);
        Trip trip = getTripEntityWithAccessCheck(userId, tripId, "admin");
        
        if (trip.getCreator().getUserId().equals(shareUserId)) {
            throw new BadRequestException("Невозможно удалить доступ создателя поездки");
        }
        
        if (userId.equals(shareUserId) && !trip.getCreator().getUserId().equals(userId)) {
            throw new BadRequestException("Вы не можете удалить свой собственный доступ");
        }
        
        User shareUser = userService.getUserEntityById(shareUserId);
        
        if (!tripAccessRepository.existsByTripAndUser(trip, shareUser)) {
            throw new ResourceNotFoundException("Доступ для указанного пользователя не найден");
        }
        
        tripAccessRepository.deleteByTripAndUser(trip, shareUser);
    }

    @Override
    @Transactional
    public TripAccessDto respondToInvitation(Long userId, Long tripId, String status) {
        User user = userService.getUserEntityById(userId);
        Trip trip = getTripEntityById(tripId);
        
        TripAccess tripAccess = tripAccessRepository.findByTripAndUser(trip, user)
                .orElseThrow(() -> new ResourceNotFoundException("Приглашение не найдено"));
        
        if (!"pending".equals(tripAccess.getInvitationStatus())) {
            throw new BadRequestException("На это приглашение уже был дан ответ");
        }
        
        if (!Arrays.asList("accepted", "rejected").contains(status)) {
            throw new BadRequestException("Недопустимый статус: " + status);
        }
        
        tripAccess.setInvitationStatus(status);
        tripAccess = tripAccessRepository.save(tripAccess);
        
        if ("accepted".equals(status)) {
            notificationService.createTripShareAcceptedNotification(
                    trip.getCreator().getUserId(),
                    tripId,
                    user.getUsername());
        }
        
        return tripAccessMapper.toDto(tripAccess);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDto> getUpcomingTrips(Long userId) {
        User user = userService.getUserEntityById(userId);
        LocalDate today = LocalDate.now();
        
        List<Trip> trips = tripRepository.findUpcomingTrips(user, today);
        
        return trips.stream()
                .map(tripMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDto> getOngoingTrips(Long userId) {
        User user = userService.getUserEntityById(userId);
        LocalDate today = LocalDate.now();
        
        List<Trip> trips = tripRepository.findOngoingTrips(user, today);
        
        return trips.stream()
                .map(tripMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDto> getPastTrips(Long userId) {
        User user = userService.getUserEntityById(userId);
        LocalDate today = LocalDate.now();
        
        List<Trip> trips = tripRepository.findPastTrips(user, today);
        
        return trips.stream()
                .map(tripMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccessToTrip(User user, Trip trip, String... requiredLevels) {
        if (trip.isDeleted()) {
            return false;
        }
        
        if (trip.getCreator().getUserId().equals(user.getUserId())) {
            return true;
        }
        
        if (trip.isPublic() && Arrays.asList(requiredLevels).contains("read")) {
            return true;
        }
        
        return tripAccessRepository.findByTripAndUser(trip, user)
                .filter(access -> "accepted".equals(access.getInvitationStatus()))
                .map(access -> Arrays.asList(requiredLevels).contains(access.getAccessLevel()))
                .orElse(false);
    }
} 