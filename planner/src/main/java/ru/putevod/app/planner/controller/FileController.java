package ru.putevod.app.planner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.putevod.app.planner.config.CurrentUser;
import ru.putevod.app.planner.dto.FileDto;
import ru.putevod.app.planner.service.FileService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Files", description = "API для управления файлами")
public class FileController {
    
    private final FileService fileService;
    
    @PostMapping("/files/upload")
    @Operation(summary = "Загрузить файл")
    public ResponseEntity<FileDto> uploadFile(
            @CurrentUser Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.uploadFile(userId, file, description));
    }
    
    @GetMapping("/files/{fileId}")
    @Operation(summary = "Получить информацию о файле")
    public ResponseEntity<FileDto> getFileInfo(
            @CurrentUser Long userId,
            @PathVariable Long fileId) {
        return ResponseEntity.ok(fileService.getFileInfo(userId, fileId));
    }
    
    @GetMapping("/files/{fileId}/download")
    @Operation(summary = "Скачать файл")
    public ResponseEntity<Resource> downloadFile(
            @CurrentUser Long userId,
            @PathVariable Long fileId) {
        FileDto fileDto = fileService.getFileInfo(userId, fileId);
        byte[] fileContent = fileService.downloadFile(userId, fileId);
        
        ByteArrayResource resource = new ByteArrayResource(fileContent);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDto.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(fileDto.getFileType()))
                .contentLength(fileDto.getFileSize())
                .body(resource);
    }
    
    @DeleteMapping("/files/{fileId}")
    @Operation(summary = "Удалить файл")
    public ResponseEntity<Void> deleteFile(
            @CurrentUser Long userId,
            @PathVariable Long fileId) {
        fileService.deleteFile(userId, fileId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/trips/{tripId}/files")
    @Operation(summary = "Добавить файл к поездке")
    public ResponseEntity<FileDto> addFileToTrip(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @RequestParam Long fileId,
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.addFileToTrip(userId, tripId, fileId, description));
    }
    
    @GetMapping("/trips/{tripId}/files")
    @Operation(summary = "Получить все файлы поездки")
    public ResponseEntity<List<FileDto>> getTripFiles(
            @CurrentUser Long userId,
            @PathVariable Long tripId) {
        return ResponseEntity.ok(fileService.getTripFiles(userId, tripId));
    }
    
    @DeleteMapping("/trips/{tripId}/files/{fileId}")
    @Operation(summary = "Удалить файл из поездки")
    public ResponseEntity<Void> removeTripFile(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long fileId) {
        fileService.removeTripFile(userId, tripId, fileId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/trips/{tripId}/days/{dayId}/places/{placeId}/files")
    @Operation(summary = "Добавить файл к месту")
    public ResponseEntity<FileDto> addFileToPlace(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @PathVariable Long placeId,
            @RequestParam Long fileId,
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.addFileToEvent(userId, placeId, fileId, description));
    }
    
    @GetMapping("/trips/{tripId}/days/{dayId}/places/{placeId}/files")
    @Operation(summary = "Получить все файлы места")
    public ResponseEntity<List<FileDto>> getPlaceFiles(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @PathVariable Long placeId) {
        return ResponseEntity.ok(fileService.getEventFiles(userId, placeId));
    }
    
    @DeleteMapping("/trips/{tripId}/days/{dayId}/places/{placeId}/files/{fileId}")
    @Operation(summary = "Удалить файл из места")
    public ResponseEntity<Void> removePlaceFile(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @PathVariable Long placeId,
            @PathVariable Long fileId) {
        fileService.removeEventFile(userId, placeId, fileId);
        return ResponseEntity.noContent().build();
    }
} 