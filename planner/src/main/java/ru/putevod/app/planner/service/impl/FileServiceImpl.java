package ru.putevod.app.planner.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.putevod.app.planner.dto.FileDto;
import ru.putevod.app.planner.exception.BadRequestException;
import ru.putevod.app.planner.exception.ResourceNotFoundException;
import ru.putevod.app.planner.mapper.FileMapper;
import ru.putevod.app.planner.model.*;
import ru.putevod.app.planner.repository.EventFileRepository;
import ru.putevod.app.planner.repository.FileRepository;
import ru.putevod.app.planner.repository.TripFileRepository;
import ru.putevod.app.planner.service.EventService;
import ru.putevod.app.planner.service.FileService;
import ru.putevod.app.planner.service.TripService;
import ru.putevod.app.planner.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final TripFileRepository tripFileRepository;
    private final EventFileRepository eventFileRepository;
    private final UserService userService;
    private final TripService tripService;
    private final EventService eventService;
    private final FileMapper fileMapper;

    @Override
    @Transactional
    public FileDto uploadFile(Long userId, MultipartFile multipartFile, String description) {
        // Для Flutter приложения этот метод не используется
        // Вместо него используется registerLocalFile
        throw new BadRequestException("Для мобильного приложения используйте метод регистрации локального файла");
    }

    /**
     * Регистрирует файл, который хранится локально на мобильном устройстве
     * @param userId ID пользователя
     * @param fileName оригинальное имя файла
     * @param localPath путь к файлу на мобильном устройстве
     * @param fileType MIME тип файла
     * @param fileSize размер файла в байтах
     * @param description описание файла
     * @return DTO с информацией о зарегистрированном файле
     */
    @Transactional
    public FileDto registerLocalFile(Long userId, String fileName, String localPath, 
                                   String fileType, Integer fileSize, String description) {
        User user = userService.getUserEntityById(userId);

        // Очищаем имя файла от потенциально опасных символов
        String cleanFileName = StringUtils.cleanPath(fileName);
        if (cleanFileName.contains("..")) {
            throw new BadRequestException("Имя файла содержит некорректный путь: " + fileName);
        }

        String serverFileId = UUID.randomUUID().toString();

        File file = File.builder()
                .user(user)
                .fileName(cleanFileName)
                .filePath(serverFileId) // Используем как уникальный идентификатор
                .fileType(fileType)
                .fileSize(fileSize)
                .build();

        file = fileRepository.save(file);

        // Формируем DTO для Flutter приложения
        FileDto fileDto = fileMapper.toDto(file);
        fileDto.setDescription(description);
        fileDto.setRequiresLocalStorage(true);
        fileDto.setLocalStorageId(localPath); // Путь на устройстве
        
        log.info("Зарегистрирован локальный файл: {} для пользователя: {}", cleanFileName, userId);
        return fileDto;
    }

    @Override
    @Transactional(readOnly = true)
    public FileDto getFileInfo(Long userId, Long fileId) {
        User user = userService.getUserEntityById(userId);
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Файл", "id", fileId));

        // Проверяем доступ к файлу
        checkFileAccess(user, file);

        FileDto fileDto = fileMapper.toDto(file);
        fileDto.setRequiresLocalStorage(true);
        // localStorageId будет установлен клиентом на основе локального пути
        
        return fileDto;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadFile(Long userId, Long fileId) {
        // Для мобильного приложения файлы хранятся локально
        // Этот метод не используется, клиент читает файлы из локального хранилища
        throw new BadRequestException("Файлы хранятся локально на устройстве. Используйте локальный путь для доступа к файлу.");
    }

    @Override
    @Transactional
    public void deleteFile(Long userId, Long fileId) {
        User user = userService.getUserEntityById(userId);
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Файл", "id", fileId));

        // Проверяем владельца файла
        if (!file.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на удаление этого файла");
        }

        // Удаляем все связи файла с поездками
        List<TripFile> tripFiles = tripFileRepository.findByFile(file);
        tripFileRepository.deleteAll(tripFiles);
        
        // Удаляем все связи файла с событиями
        List<EventFile> eventFiles = eventFileRepository.findByFile(file);
        eventFileRepository.deleteAll(eventFiles);
        
        // Удаляем запись из БД
        fileRepository.delete(file);
        
        log.info("Удален файл: {} пользователя: {}", file.getFileName(), userId);
        // ВАЖНО: Flutter приложение должно самостоятельно удалить файл из локального хранилища
    }

    @Override
    @Transactional
    public FileDto addFileToTrip(Long userId, Long tripId, Long fileId, String description) {
        User user = userService.getUserEntityById(userId);
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);
        File file = getFileEntityById(fileId);

        // Проверяем владельца файла
        if (!file.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на использование этого файла");
        }

        // Проверяем дублирование
        if (tripFileRepository.existsByTripAndFile(trip, file)) {
            throw new BadRequestException("Файл уже добавлен к данной поездке");
        }

        // Создаем связь
        TripFile tripFile = TripFile.builder()
                .trip(trip)
                .file(file)
                .description(description)
                .build();

        tripFileRepository.save(tripFile);

        FileDto fileDto = fileMapper.toDto(file);
        fileDto.setDescription(description);
        fileDto.setRequiresLocalStorage(true);

        return fileDto;
    }

    @Override
    @Transactional
    public FileDto addFileToEvent(Long userId, Long eventId, Long fileId, String description) {
        User user = userService.getUserEntityById(userId);
        Event event = eventService.getEventEntityById(eventId);

        // Проверяем доступ к поездке
        Trip trip = event.getDay().getTrip();
        if (!tripService.hasAccessToTrip(user, trip, "admin", "write")) {
            throw new BadRequestException("У вас нет прав на добавление файлов к этому событию");
        }

        File file = getFileEntityById(fileId);

        // Проверяем владельца файла
        if (!file.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на использование этого файла");
        }

        // Проверяем дублирование
        if (eventFileRepository.existsByEventAndFile(event, file)) {
            throw new BadRequestException("Файл уже добавлен к данному событию");
        }

        // Создаем связь
        EventFile eventFile = EventFile.builder()
                .event(event)
                .file(file)
                .description(description)
                .build();

        eventFileRepository.save(eventFile);

        FileDto fileDto = fileMapper.toDto(file);
        fileDto.setDescription(description);
        fileDto.setRequiresLocalStorage(true);

        return fileDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileDto> getTripFiles(Long userId, Long tripId) {
        User user = userService.getUserEntityById(userId);
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

        List<File> files = fileRepository.findByTripId(tripId);
        
        return files.stream()
                .map(file -> {
                    FileDto dto = fileMapper.toDto(file);
                    dto.setRequiresLocalStorage(true);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileDto> getEventFiles(Long userId, Long eventId) {
        User user = userService.getUserEntityById(userId);
        Event event = eventService.getEventEntityById(eventId);

        // Проверяем доступ к поездке
        Trip trip = event.getDay().getTrip();
        if (!tripService.hasAccessToTrip(user, trip, "admin", "read", "write")) {
            throw new BadRequestException("У вас нет доступа к файлам этого события");
        }

        List<File> files = fileRepository.findByEventId(eventId);
        
        return files.stream()
                .map(file -> {
                    FileDto dto = fileMapper.toDto(file);
                    dto.setRequiresLocalStorage(true);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeTripFile(Long userId, Long tripId, Long fileId) {
        User user = userService.getUserEntityById(userId);
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);
        File file = getFileEntityById(fileId);

        TripFile tripFile = tripFileRepository.findByTripAndFile(trip, file)
                .orElseThrow(() -> new ResourceNotFoundException("Связь файла с поездкой не найдена"));

        tripFileRepository.delete(tripFile);
    }

    @Override
    @Transactional
    public void removeEventFile(Long userId, Long eventId, Long fileId) {
        User user = userService.getUserEntityById(userId);
        Event event = eventService.getEventEntityById(eventId);
        
        // Проверяем доступ
        Trip trip = event.getDay().getTrip();
        if (!tripService.hasAccessToTrip(user, trip, "admin", "write")) {
            throw new BadRequestException("У вас нет прав на удаление файлов из этого события");
        }

        File file = getFileEntityById(fileId);

        EventFile eventFile = eventFileRepository.findByEventAndFile(event, file)
                .orElseThrow(() -> new ResourceNotFoundException("Связь файла с событием не найдена"));

        eventFileRepository.delete(eventFile);
    }

    @Override
    @Transactional(readOnly = true)
    public File getFileEntityById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Файл", "id", fileId));
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    private void checkFileAccess(User user, File file) {
        // Файл доступен только его владельцу
        if (!file.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestException("У вас нет доступа к этому файлу");
        }
    }
} 