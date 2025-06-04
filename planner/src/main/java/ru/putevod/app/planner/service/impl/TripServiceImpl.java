package ru.putevod.app.planner.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.planner.dto.CreateTripDto;
import ru.putevod.app.planner.dto.TripAccessDto;
import ru.putevod.app.planner.dto.TripDto;
import ru.putevod.app.planner.dto.UpdateTripDto;
import ru.putevod.app.planner.dto.CreateTripAccessDto;
import ru.putevod.app.planner.dto.UserDto;
import ru.putevod.app.planner.dto.RemoveShareResponseDto;
import ru.putevod.app.planner.exception.AccessDeniedException;
import ru.putevod.app.planner.exception.BadRequestException;
import ru.putevod.app.planner.exception.ResourceNotFoundException;
import ru.putevod.app.planner.mapper.TripAccessMapper;
import ru.putevod.app.planner.mapper.TripMapper;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.TripAccess;
import ru.putevod.app.planner.model.TripDay;
import ru.putevod.app.planner.model.User;
import ru.putevod.app.planner.repository.PlaceRepository;
import ru.putevod.app.planner.repository.TripAccessRepository;
import ru.putevod.app.planner.repository.TripDayRepository;
import ru.putevod.app.planner.repository.TripRepository;
import ru.putevod.app.planner.repository.EventRepository;
import ru.putevod.app.planner.service.NotificationService;
import ru.putevod.app.planner.service.TripPreviewService;
import ru.putevod.app.planner.service.TripService;
import ru.putevod.app.planner.service.UserService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TripAccessRepository tripAccessRepository;
    private final PlaceRepository placeRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final TripPreviewService tripPreviewService;
    private final TripMapper tripMapper;
    private final TripAccessMapper tripAccessMapper;
    private final TripDayRepository tripDayRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public TripDto createTrip(Long userId, TripDto tripDto) {
        validateRequiredFields(tripDto);

        User user = userService.getUserEntityById(userId);

        Trip trip = tripMapper.toEntity(tripDto);
        trip.setCreator(user);
        trip.setDeleted(false);

        if (tripDto.getCity() != null && !tripDto.getCity().isEmpty()) {
            String previewUrl = tripPreviewService.generatePreviewForCity(tripDto.getCity());
            trip.setPreviewUrl(previewUrl);
            log.info("Для поездки {} установлено превью: {}", tripDto.getTitle(), previewUrl);
        } else {
            trip.setPreviewUrl(tripPreviewService.getDefaultPreviewUrl());
            log.info("Для поездки {} установлено превью по умолчанию", tripDto.getTitle());
        }

        trip = tripRepository.save(trip);

        TripAccess creatorAccess = TripAccess.builder()
                .trip(trip)
                .user(user)
                .accessLevel("admin")
                .invitationStatus("accepted")
                .build();

        tripAccessRepository.save(creatorAccess);

        if (trip.getStartDate() != null && trip.getEndDate() != null) {
            createTripDays(trip);
        }

        return tripMapper.toDto(trip);
    }

    @Override
    @Transactional
    public TripDto createTrip(Long userId, CreateTripDto createTripDto) {
        validateRequiredFields(createTripDto);

        User user = userService.getUserEntityById(userId);

        Trip trip = tripMapper.toEntityFromCreate(createTripDto, user);

        if (createTripDto.getCity() != null && !createTripDto.getCity().isEmpty()) {
            String previewUrl = tripPreviewService.generatePreviewForCity(createTripDto.getCity());
            trip.setPreviewUrl(previewUrl);
            log.info("Для поездки {} установлено превью: {}", createTripDto.getTitle(), previewUrl);
        } else {
            trip.setPreviewUrl(tripPreviewService.getDefaultPreviewUrl());
            log.info("Для поездки {} установлено превью по умолчанию", createTripDto.getTitle());
        }

        trip = tripRepository.save(trip);

        TripAccess creatorAccess = TripAccess.builder()
                .trip(trip)
                .user(user)
                .accessLevel("admin")
                .invitationStatus("accepted")
                .build();

        tripAccessRepository.save(creatorAccess);

        if (trip.getStartDate() != null && trip.getEndDate() != null) {
            createTripDays(trip);
        }

        return tripMapper.toDto(trip);
    }

    /**
     * Создает дни поездки автоматически на основе дат начала и окончания поездки
     *
     * @param trip поездка
     */
    private void createTripDays(Trip trip) {
        LocalDate currentDate = trip.getStartDate();
        int dayNumber = 1;

        while (!currentDate.isAfter(trip.getEndDate())) {
            if (tripDayRepository.findByTripAndDate(trip, currentDate).isEmpty()) {
                TripDay tripDay = TripDay.builder()
                        .trip(trip)
                        .dayNumber(dayNumber)
                        .date(currentDate)
                        .build();

                tripDayRepository.save(tripDay);
                log.info("Создан день {} для поездки {}: {}", dayNumber, trip.getTripId(), currentDate);
            }

            currentDate = currentDate.plusDays(1);
            dayNumber++;
        }
    }

    /**
     * Проверяет наличие обязательных полей в DTO поездки
     *
     * @param tripDto DTO поездки для проверки
     * @throws BadRequestException если какое-либо обязательное поле отсутствует
     */
    private void validateRequiredFields(TripDto tripDto) {
        if (tripDto.getTitle() == null || tripDto.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Название поездки обязательно для заполнения");
        }

        if (tripDto.getStartDate() == null) {
            throw new BadRequestException("Дата начала поездки обязательна для заполнения");
        }

        if (tripDto.getEndDate() == null) {
            throw new BadRequestException("Дата окончания поездки обязательна для заполнения");
        }

        if (tripDto.getEndDate().isBefore(tripDto.getStartDate())) {
            throw new BadRequestException("Дата окончания поездки не может быть раньше даты начала");
        }

        if (tripDto.getCountry() == null || tripDto.getCountry().trim().isEmpty()) {
            throw new BadRequestException("Страна поездки обязательна для заполнения");
        }

        if (tripDto.getCity() == null || tripDto.getCity().trim().isEmpty()) {
            throw new BadRequestException("Город поездки обязателен для заполнения");
        }
    }

    /**
     * Проверяет наличие обязательных полей в DTO создания поездки
     *
     * @param createTripDto DTO создания поездки для проверки
     * @throws BadRequestException если какое-либо обязательное поле отсутствует
     */
    private void validateRequiredFields(CreateTripDto createTripDto) {
        if (createTripDto.getTitle() == null || createTripDto.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Название поездки обязательно для заполнения");
        }

        if (createTripDto.getStartDate() == null) {
            throw new BadRequestException("Дата начала поездки обязательна для заполнения");
        }

        if (createTripDto.getEndDate() == null) {
            throw new BadRequestException("Дата окончания поездки обязательна для заполнения");
        }

        if (createTripDto.getEndDate().isBefore(createTripDto.getStartDate())) {
            throw new BadRequestException("Дата окончания поездки не может быть раньше даты начала");
        }

        if (createTripDto.getCountry() == null || createTripDto.getCountry().trim().isEmpty()) {
            throw new BadRequestException("Страна поездки обязательна для заполнения");
        }

        if (createTripDto.getCity() == null || createTripDto.getCity().trim().isEmpty()) {
            throw new BadRequestException("Город поездки обязателен для заполнения");
        }
    }

    @Override
    @Transactional
    public TripDto updateTrip(Long userId, Long tripId, UpdateTripDto updateTripDto) {
        if (updateTripDto.getEndDate().isBefore(updateTripDto.getStartDate())) {
            throw new BadRequestException("Дата окончания поездки не может быть раньше даты начала");
        }

        Trip trip = getTripEntityWithAccessCheck(userId, tripId, "admin", "write");

        String oldCity = trip.getCity();
        LocalDate oldStartDate = trip.getStartDate();
        LocalDate oldEndDate = trip.getEndDate();

        tripMapper.updateEntityFromUpdate(updateTripDto, trip);

        if (updateTripDto.getCity() != null && !updateTripDto.getCity().equals(oldCity)) {
            String previewUrl = tripPreviewService.generatePreviewForCity(updateTripDto.getCity());
            trip.setPreviewUrl(previewUrl);
            log.info("Обновлено превью для поездки {} с изменением города на {}: {}",
                    updateTripDto.getTitle(), updateTripDto.getCity(), previewUrl);
        }

        boolean datesChanged = !updateTripDto.getStartDate().equals(oldStartDate) || 
                              !updateTripDto.getEndDate().equals(oldEndDate);

        if (datesChanged) {
            log.info("Даты поездки {} изменились с [{} - {}] на [{} - {}], обновляем дни поездки", 
                    tripId, oldStartDate, oldEndDate, updateTripDto.getStartDate(), updateTripDto.getEndDate());
            
            // Умно обновляем дни поездки, сохраняя существующие данные
            updateTripDaysIntelligently(trip, updateTripDto.getStartDate(), updateTripDto.getEndDate());
        }

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
        Trip trip = getTripEntityById(tripId);

        if (trip.isDeleted()) {
            throw new BadRequestException("Поездка уже была удалена");
        }

        if (!trip.getCreator().getUserId().equals(userId)) {
            if (!hasAccessToTrip(user, trip, "admin")) {
                throw new AccessDeniedException("У вас нет прав на удаление этой поездки");
            }
        }

        trip.setDeleted(true);
        tripRepository.save(trip);

        log.info("Поездка {} успешно удалена пользователем {}", tripId, userId);
    }

    @Override
    @Transactional
    public TripAccessDto shareTrip(Long userId, Long tripId, CreateTripAccessDto accessDto) {
        User owner = userService.getUserEntityById(userId);
        Trip trip = getTripEntityWithAccessCheck(userId, tripId, "admin");

        User userToShare;
        if (accessDto.getUserId() != null) {
            userToShare = userService.getUserEntityById(accessDto.getUserId());
        } else if (accessDto.getUsername() != null && !accessDto.getUsername().trim().isEmpty()) {
            try {
                UserDto userDto = userService.findByUsername(accessDto.getUsername().trim());
                userToShare = userService.getUserEntityById(userDto.getId());
            } catch (Exception e) {
                throw new BadRequestException("Пользователь с никнеймом '" + accessDto.getUsername() + "' не найден");
            }
        } else {
            throw new BadRequestException("Необходимо указать либо ID пользователя, либо его никнейм");
        }

        if (userToShare.getUserId().equals(userId)) {
            throw new BadRequestException("Вы не можете пригласить самого себя");
        }

        if (tripAccessRepository.existsByTripAndUser(trip, userToShare)) {
            throw new BadRequestException("Пользователь уже имеет доступ к этой поездке");
        }

        TripAccess tripAccess = tripAccessMapper.toEntityFromCreate(accessDto, userToShare, trip);
        tripAccess = tripAccessRepository.save(tripAccess);

        notificationService.createTripInviteNotification(
                userToShare.getUserId(),
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
    public RemoveShareResponseDto removeShare(Long userId, Long tripId, Long shareUserId) {
        User user = userService.getUserEntityById(userId);
        Trip trip = getTripEntityWithAccessCheck(userId, tripId, "admin");

        if (trip.getCreator().getUserId().equals(shareUserId)) {
            throw new BadRequestException("Невозможно удалить доступ создателя поездки");
        }

        if (userId.equals(shareUserId) && !trip.getCreator().getUserId().equals(userId)) {
            throw new BadRequestException("Вы не можете удалить свой собственный доступ");
        }

        User shareUser = userService.getUserEntityById(shareUserId);

        // Находим запись доступа для получения информации перед удалением
        TripAccess tripAccess = tripAccessRepository.findByTripAndUser(trip, shareUser)
                .orElseThrow(() -> new ResourceNotFoundException("Доступ для указанного пользователя не найден"));

        // Сохраняем информацию о доступе перед удалением
        String previousStatus = tripAccess.getInvitationStatus();
        String previousAccessLevel = tripAccess.getAccessLevel();
        String removedUsername = shareUser.getUsername();

        // Удаляем доступ
        tripAccessRepository.deleteByTripAndUser(trip, shareUser);
        
        // Отправляем уведомление пользователю об отмене приглашения, если оно было pending
        if ("pending".equals(previousStatus)) {
            try {
                notificationService.createTripInviteCancelledNotification(
                        shareUserId, 
                        tripId, 
                        user.getUsername());
            } catch (Exception e) {
                log.warn("Не удалось отправить уведомление об отмене приглашения пользователю {}: {}", 
                        shareUserId, e.getMessage());
            }
        }
        
        // Определяем тип сообщения в зависимости от статуса
        String message;
        if ("pending".equals(previousStatus)) {
            message = "Приглашение пользователя '" + removedUsername + "' отменено";
        } else {
            message = "Доступ пользователя '" + removedUsername + "' к поездке удален";
        }

        log.info("Удален доступ пользователя {} (статус: {}, уровень: {}) к поездке {} пользователем {}", 
                shareUserId, previousStatus, previousAccessLevel, tripId, userId);

        return RemoveShareResponseDto.builder()
                .message(message)
                .removedUserId(shareUserId)
                .removedUsername(removedUsername)
                .previousInvitationStatus(previousStatus)
                .previousAccessLevel(previousAccessLevel)
                .build();
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
        // Если поездка удалена, доступа нет ни у кого
        if (trip.isDeleted()) {
            return false;
        }

        // Создатель поездки всегда имеет права администратора
        if (trip.getCreator().getUserId().equals(user.getUserId())) {
            // Создатель имеет все права на свою поездку, независимо от требуемого уровня доступа
            return true;
        }

        // Для публичных поездок - уровень чтения доступен всем
        if (trip.isPublished() && Arrays.asList(requiredLevels).contains("read")) {
            return true;
        }

        // Проверяем уровень доступа согласно записям в базе
        return tripAccessRepository.findByTripAndUser(trip, user)
                .filter(access -> "accepted".equals(access.getInvitationStatus()))
                .map(access -> Arrays.asList(requiredLevels).contains(access.getAccessLevel()))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canPublishTrip(Long userId, Long tripId) {
        try {
            Trip trip = getTripEntityById(tripId);
            User user = userService.getUserEntityById(userId);

            boolean isCreator = trip.getCreator().getUserId().equals(userId);
            boolean isAdmin = tripAccessRepository.findByTripAndUser(trip, user)
                    .filter(access -> "accepted".equals(access.getInvitationStatus()))
                    .map(access -> "admin".equals(access.getAccessLevel()))
                    .orElse(false);

            log.info("Проверка возможности публикации маршрута {}: userId={}, isCreator={}, isAdmin={}",
                    tripId, userId, isCreator, isAdmin);

            return isCreator || isAdmin;
        } catch (Exception e) {
            log.error("Ошибка при проверке возможности публикации маршрута {}: {}", tripId, e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public TripDto publishTrip(Long userId, Long tripId, boolean publish) {
        Trip trip = getTripEntityWithAccessCheck(userId, tripId, "admin");

        if (!canPublishTrip(userId, tripId)) {
            throw new AccessDeniedException("У вас нет прав на публикацию или снятие с публикации этого маршрута");
        }

        trip.setPublished(publish);
        trip = tripRepository.save(trip);

        log.info("Маршрут {} {}: userId={}",
                tripId, publish ? "опубликован" : "снят с публикации", userId);

        return tripMapper.toDto(trip);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalTripsCount(Long userId) {
        User user = userService.getUserEntityById(userId);
        return tripRepository.countAllUserTrips(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalPlacesCount(Long userId) {
        User user = userService.getUserEntityById(userId);
        return eventRepository.countUserEvents(user);
    }

    /**
     * Обновляет дни поездки, сохраняя существующие данные
     *
     * @param trip поездка
     * @param newStartDate новая дата начала поездки
     * @param newEndDate новая дата окончания поездки
     */
    private void updateTripDaysIntelligently(Trip trip, LocalDate newStartDate, LocalDate newEndDate) {
        // Получаем все существующие дни поездки
        List<TripDay> existingDays = tripDayRepository.findByTripOrderByDayNumberAsc(trip);
        
        // Удаляем дни, которые выходят за пределы новых дат
        List<TripDay> daysToDelete = existingDays.stream()
                .filter(day -> day.getDate().isBefore(newStartDate) || day.getDate().isAfter(newEndDate))
                .collect(Collectors.toList());
        
        if (!daysToDelete.isEmpty()) {
            tripDayRepository.deleteAll(daysToDelete);
            log.info("Удалено {} дней поездки {}, которые выходят за пределы новых дат", 
                    daysToDelete.size(), trip.getTripId());
        }
        
        // Создаем недостающие дни и обновляем номера дней
        LocalDate currentDate = newStartDate;
        int dayNumber = 1;
        
        while (!currentDate.isAfter(newEndDate)) {
            Optional<TripDay> existingDay = tripDayRepository.findByTripAndDate(trip, currentDate);
            
            if (existingDay.isPresent()) {
                // День существует - обновляем только номер дня если он изменился
                TripDay day = existingDay.get();
                if (day.getDayNumber() != dayNumber) {
                    day.setDayNumber(dayNumber);
                    tripDayRepository.save(day);
                    log.info("Обновлен номер дня поездки {} с {} на {} для даты {}", 
                            trip.getTripId(), day.getDayNumber(), dayNumber, currentDate);
                }
            } else {
                // День не существует - создаем новый
                TripDay tripDay = TripDay.builder()
                        .trip(trip)
                        .dayNumber(dayNumber)
                        .date(currentDate)
                        .build();
                
                tripDayRepository.save(tripDay);
                log.info("Создан новый день {} для поездки {}: {}", dayNumber, trip.getTripId(), currentDate);
            }
            
            currentDate = currentDate.plusDays(1);
            dayNumber++;
        }
        
        log.info("Обновление дней поездки {} завершено: диапазон дат [{} - {}]", 
                trip.getTripId(), newStartDate, newEndDate);
    }
} 