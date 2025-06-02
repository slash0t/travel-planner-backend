package ru.putevod.app.planner.service;

import org.springframework.web.multipart.MultipartFile;
import ru.putevod.app.planner.dto.FileDto;
import ru.putevod.app.planner.model.File;

import java.util.List;

public interface FileService {

    FileDto uploadFile(Long userId, MultipartFile file, String description);

    FileDto getFileInfo(Long userId, Long fileId);

    byte[] downloadFile(Long userId, Long fileId);

    void deleteFile(Long userId, Long fileId);

    FileDto addFileToTrip(Long userId, Long tripId, Long fileId, String description);

    FileDto addFileToEvent(Long userId, Long eventId, Long fileId, String description);

    List<FileDto> getTripFiles(Long userId, Long tripId);

    List<FileDto> getEventFiles(Long userId, Long eventId);

    void removeTripFile(Long userId, Long tripId, Long fileId);

    void removeEventFile(Long userId, Long eventId, Long fileId);

    File getFileEntityById(Long fileId);
} 