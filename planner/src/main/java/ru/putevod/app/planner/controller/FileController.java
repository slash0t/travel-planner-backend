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
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.uploadFile(userId, file, description));
    }
    
    @GetMapping("/files/{fileId}")
    @Operation(summary = "Получить информацию о файле")
    public ResponseEntity<FileDto> getFileInfo(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long fileId) {
        return ResponseEntity.ok(fileService.getFileInfo(userId, fileId));
    }
    
    @GetMapping("/files/{fileId}/download")
    @Operation(summary = "Скачать файл")
    public ResponseEntity<Resource> downloadFile(
            @RequestHeader("X-User-Id") Long userId,
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
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long fileId) {
        fileService.deleteFile(userId, fileId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/trips/{tripId}/files")
    @Operation(summary = "Добавить файл к поездке")
    public ResponseEntity<FileDto> addFileToTrip(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long tripId,
            @RequestParam Long fileId,
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.addFileToTrip(userId, tripId, fileId, description));
    }
    
    @GetMapping("/trips/{tripId}/files")
    @Operation(summary = "Получить все файлы поездки")
    public ResponseEntity<List<FileDto>> getTripFiles(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long tripId) {
        return ResponseEntity.ok(fileService.getTripFiles(userId, tripId));
    }
    
    @DeleteMapping("/trips/{tripId}/files/{fileId}")
    @Operation(summary = "Удалить файл из поездки")
    public ResponseEntity<Void> removeTripFile(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long tripId,
            @PathVariable Long fileId) {
        fileService.removeTripFile(userId, tripId, fileId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/events/{eventId}/files")
    @Operation(summary = "Добавить файл к событию")
    public ResponseEntity<FileDto> addFileToEvent(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long eventId,
            @RequestParam Long fileId,
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.addFileToEvent(userId, eventId, fileId, description));
    }
    
    @GetMapping("/events/{eventId}/files")
    @Operation(summary = "Получить все файлы события")
    public ResponseEntity<List<FileDto>> getEventFiles(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long eventId) {
        return ResponseEntity.ok(fileService.getEventFiles(userId, eventId));
    }
    
    @DeleteMapping("/events/{eventId}/files/{fileId}")
    @Operation(summary = "Удалить файл из события")
    public ResponseEntity<Void> removeEventFile(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long eventId,
            @PathVariable Long fileId) {
        fileService.removeEventFile(userId, eventId, fileId);
        return ResponseEntity.noContent().build();
    }
} 