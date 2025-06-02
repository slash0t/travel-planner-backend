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
        User user = userService.getUserEntityById(userId);

        String originalFilename = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Имя файла содержит некорректный путь: " + originalFilename);
        }

        try {
            // Создаем уникальный идентификатор для файла
            String uniqueFilename = UUID.randomUUID().toString();

            // Создаем запись в БД с метаданными файла
            // Примечание: сам файл будет храниться на мобильном устройстве 
            // в локальной SQLite базе данных
            File file = File.builder()
                    .user(user)
                    .fileName(originalFilename)
                    .filePath(uniqueFilename) // Используем как идентификатор для мобильного приложения
                    .fileType(multipartFile.getContentType())
                    .fileSize((int) multipartFile.getSize())
                    .build();

            file = fileRepository.save(file);

            FileDto fileDto = fileMapper.toDto(file);
            fileDto.setDescription(description);
            fileDto.setRequiresLocalStorage(true);
            fileDto.setLocalStorageId(file.getFilePath());

            return fileDto;
        } catch (Exception ex) {
            log.error("Не удалось обработать файл: {}", originalFilename, ex);
            throw new BadRequestException("Не удалось обработать файл: " + originalFilename);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileDto getFileInfo(Long userId, Long fileId) {
        User user = userService.getUserEntityById(userId);
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Файл", "id", fileId));

        // Проверяем, имеет ли пользователь доступ к файлу
        checkFileAccess(user, file);

        FileDto fileDto = fileMapper.toDto(file);
        fileDto.setRequiresLocalStorage(true);
        fileDto.setLocalStorageId(file.getFilePath());

        return fileDto;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadFile(Long userId, Long fileId) {
        // В мобильном приложении эта функция не используется,
        // так как файлы хранятся локально на устройстве
        throw new BadRequestException("Для мобильного приложения загрузка файлов с сервера не поддерживается");
    }

    @Override
    @Transactional
    public void deleteFile(Long userId, Long fileId) {
        User user = userService.getUserEntityById(userId);
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Файл", "id", fileId));

        // Проверяем, что файл принадлежит этому пользователю
        if (!file.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на удаление этого файла");
        }

        // Удаляем запись из БД
        // Мобильное приложение должно самостоятельно удалить файл из локального хранилища
        fileRepository.delete(file);
    }

    @Override
    @Transactional
    public FileDto addFileToTrip(Long userId, Long tripId, Long fileId, String description) {
        User user = userService.getUserEntityById(userId);
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);
        File file = getFileEntityById(fileId);

        // Проверяем, что файл принадлежит пользователю
        if (!file.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на использование этого файла");
        }

        // Проверяем, не добавлен ли уже файл к поездке
        if (tripFileRepository.existsByTripAndFile(trip, file)) {
            throw new BadRequestException("Файл уже добавлен к данной поездке");
        }

        // Создаем связь файла с поездкой
        TripFile tripFile = TripFile.builder()
                .trip(trip)
                .file(file)
                .description(description)
                .build();

        tripFileRepository.save(tripFile);

        FileDto fileDto = fileMapper.toDto(file);
        fileDto.setDescription(description);
        fileDto.setRequiresLocalStorage(true);
        fileDto.setLocalStorageId(file.getFilePath());

        return fileDto;
    }

    @Override
    @Transactional
    public FileDto addFileToEvent(Long userId, Long eventId, Long fileId, String description) {
        User user = userService.getUserEntityById(userId);
        Event event = eventService.getEventEntityById(eventId);

        // Проверяем, что у пользователя есть доступ к поездке, в которой находится событие
        Trip trip = event.getDay().getTrip();
        tripService.hasAccessToTrip(user, trip, "admin", "write");

        File file = getFileEntityById(fileId);

        // Проверяем, что файл принадлежит пользователю
        if (!file.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("У вас нет прав на использование этого файла");
        }

        // Проверяем, не добавлен ли уже файл к событию
        if (eventFileRepository.existsByEventAndFile(event, file)) {
            throw new BadRequestException("Файл уже добавлен к данному событию");
        }

        // Создаем связь файла с событием
        EventFile eventFile = EventFile.builder()
                .event(event)
                .file(file)
                .description(description)
                .build();

        eventFileRepository.save(eventFile);

        FileDto fileDto = fileMapper.toDto(file);
        fileDto.setDescription(description);
        fileDto.setRequiresLocalStorage(true);
        fileDto.setLocalStorageId(file.getFilePath());

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
                    FileDto fileDto = fileMapper.toDto(file);
                    tripFileRepository.findByTripAndFile(trip, file)
                            .ifPresent(tripFile -> fileDto.setDescription(tripFile.getDescription()));
                    fileDto.setRequiresLocalStorage(true);
                    fileDto.setLocalStorageId(file.getFilePath());
                    return fileDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileDto> getEventFiles(Long userId, Long eventId) {
        User user = userService.getUserEntityById(userId);
        Event event = eventService.getEventEntityById(eventId);

        // Проверяем, что у пользователя есть доступ к поездке, в которой находится событие
        Trip trip = event.getDay().getTrip();
        tripService.hasAccessToTrip(user, trip, "admin", "read", "write");

        List<File> files = fileRepository.findByEventId(eventId);

        return files.stream()
                .map(file -> {
                    FileDto fileDto = fileMapper.toDto(file);
                    eventFileRepository.findByEventAndFile(event, file)
                            .ifPresent(eventFile -> fileDto.setDescription(eventFile.getDescription()));
                    fileDto.setRequiresLocalStorage(true);
                    fileDto.setLocalStorageId(file.getFilePath());
                    return fileDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeTripFile(Long userId, Long tripId, Long fileId) {
        User user = userService.getUserEntityById(userId);
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);
        File file = getFileEntityById(fileId);

        // Находим и удаляем связь файла с поездкой
        TripFile tripFile = tripFileRepository.findByTripAndFile(trip, file)
                .orElseThrow(() -> new ResourceNotFoundException("Файл не связан с данной поездкой"));

        tripFileRepository.delete(tripFile);
    }

    @Override
    @Transactional
    public void removeEventFile(Long userId, Long eventId, Long fileId) {
        User user = userService.getUserEntityById(userId);
        Event event = eventService.getEventEntityById(eventId);

        // Проверяем, что у пользователя есть доступ к поездке, в которой находится событие
        Trip trip = event.getDay().getTrip();
        tripService.hasAccessToTrip(user, trip, "admin", "write");

        File file = getFileEntityById(fileId);

        // Находим и удаляем связь файла с событием
        EventFile eventFile = eventFileRepository.findByEventAndFile(event, file)
                .orElseThrow(() -> new ResourceNotFoundException("Файл не связан с данным событием"));

        eventFileRepository.delete(eventFile);
    }

    @Override
    @Transactional(readOnly = true)
    public File getFileEntityById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Файл", "id", fileId));
    }

    private String getFileExtension(String filename) {
        if (filename.lastIndexOf(".") != -1 && filename.lastIndexOf(".") != 0) {
            return filename.substring(filename.lastIndexOf("."));
        } else {
            return "";
        }
    }

    private void checkFileAccess(User user, File file) {
        // Файл принадлежит пользователю
        if (file.getUser().getUserId().equals(user.getUserId())) {
            return;
        }

        // Проверяем, есть ли у пользователя доступ к поездкам, в которых есть этот файл
        boolean hasAccess = false;

        for (TripFile tripFile : file.getTripFiles()) {
            if (tripService.hasAccessToTrip(user, tripFile.getTrip(), "admin", "read", "write")) {
                hasAccess = true;
                break;
            }
        }

        if (!hasAccess) {
            // Проверяем, есть ли у пользователя доступ к событиям, в которых есть этот файл
            for (EventFile eventFile : file.getEventFiles()) {
                Trip trip = eventFile.getEvent().getDay().getTrip();
                if (tripService.hasAccessToTrip(user, trip, "admin", "read", "write")) {
                    hasAccess = true;
                    break;
                }
            }
        }

        if (!hasAccess) {
            throw new BadRequestException("У вас нет прав на доступ к этому файлу");
        }
    }
} 