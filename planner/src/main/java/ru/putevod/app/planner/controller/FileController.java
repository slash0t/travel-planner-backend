package ru.putevod.app.planner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Files", description = "API для работы с файлами. Поддерживает локальное хранение для мобильных приложений.")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @Operation(
        summary = "Загрузить файл (устарело для мобильных приложений)",
        description = "Загружает файл на сервер. Для Flutter приложений рекомендуется использовать /register-local эндпоинт."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Для мобильных приложений используйте /register-local")
    })
    public ResponseEntity<FileDto> uploadFile(
            @CurrentUser Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.uploadFile(userId, file, description));
    }

    @PostMapping("/register-local")
    @Operation(
        summary = "Зарегистрировать локальный файл",
        description = "Регистрирует файл, который хранится локально на мобильном устройстве. Сервер сохраняет только метаданные файла."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Файл успешно зарегистрирован",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FileDto.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Некорректные данные файла"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<FileDto> registerLocalFile(
            @CurrentUser Long userId,
            @Parameter(description = "Оригинальное имя файла", required = true, example = "vacation_photo.jpg")
            @RequestParam @NotBlank String fileName,
            @Parameter(description = "Путь к файлу на устройстве", required = true, example = "/storage/emulated/0/Pictures/vacation_photo.jpg")
            @RequestParam @NotBlank String localPath,
            @Parameter(description = "MIME тип файла", required = true, example = "image/jpeg")
            @RequestParam @NotBlank String fileType,
            @Parameter(description = "Размер файла в байтах", required = true, example = "1048576")
            @RequestParam @NotNull @Positive Integer fileSize,
            @Parameter(description = "Описание файла", example = "Фото с отпуска")
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.registerLocalFile(userId, fileName, localPath, fileType, fileSize, description));
    }

    @GetMapping("/{fileId}")
    @Operation(
        summary = "Получить информацию о файле",
        description = "Возвращает метаданные файла. Для локальных файлов включает информацию о требовании локального хранения."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Информация о файле получена",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FileDto.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Файл не найден"),
        @ApiResponse(responseCode = "403", description = "Нет доступа к файлу")
    })
    public ResponseEntity<FileDto> getFileInfo(
            @CurrentUser Long userId,
            @Parameter(description = "ID файла", required = true, example = "1")
            @PathVariable Long fileId) {
        return ResponseEntity.ok(fileService.getFileInfo(userId, fileId));
    }

    @GetMapping("/{fileId}/download")
    @Operation(
        summary = "Скачать файл (не поддерживается для локальных файлов)",
        description = "Для файлов, хранящихся локально на устройстве, этот эндпоинт недоступен. Используйте локальный путь к файлу."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Файлы хранятся локально на устройстве")
    })
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

    @DeleteMapping("/{fileId}")
    @Operation(
        summary = "Удалить файл",
        description = "Удаляет метаданные файла с сервера. Flutter приложение должно самостоятельно удалить файл из локального хранилища."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Файл успешно удален"),
        @ApiResponse(responseCode = "404", description = "Файл не найден"),
        @ApiResponse(responseCode = "403", description = "Нет прав на удаление файла")
    })
    public ResponseEntity<Void> deleteFile(
            @CurrentUser Long userId,
            @Parameter(description = "ID файла", required = true, example = "1")
            @PathVariable Long fileId) {
        fileService.deleteFile(userId, fileId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/trips/{tripId}/files")
    @Operation(
        summary = "Добавить файл к поездке",
        description = "Связывает уже зарегистрированный файл с поездкой. Файл должен быть предварительно зарегистрирован через /register-local."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Файл успешно добавлен к поездке",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FileDto.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Файл уже добавлен к поездке или нет доступа"),
        @ApiResponse(responseCode = "404", description = "Поездка или файл не найдены")
    })
    public ResponseEntity<FileDto> addFileToTrip(
            @CurrentUser Long userId,
            @Parameter(description = "ID поездки", required = true, example = "1")
            @PathVariable Long tripId,
            @Parameter(description = "ID файла", required = true, example = "1")
            @RequestParam Long fileId,
            @Parameter(description = "Описание привязки файла к поездке", example = "Билеты на самолет")
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.addFileToTrip(userId, tripId, fileId, description));
    }

    @GetMapping("/trips/{tripId}/files")
    @Operation(
        summary = "Получить все файлы поездки",
        description = "Возвращает список всех файлов, привязанных к поездке. Для локальных файлов включает информацию о локальном хранении."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Список файлов поездки получен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FileDto.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Поездка не найдена"),
        @ApiResponse(responseCode = "403", description = "Нет доступа к поездке")
    })
    public ResponseEntity<List<FileDto>> getTripFiles(
            @CurrentUser Long userId,
            @Parameter(description = "ID поездки", required = true, example = "1")
            @PathVariable Long tripId) {
        return ResponseEntity.ok(fileService.getTripFiles(userId, tripId));
    }

    @DeleteMapping("/trips/{tripId}/files/{fileId}")
    @Operation(
        summary = "Удалить файл из поездки",
        description = "Убирает связь файла с поездкой. Сам файл остается в системе и может быть привязан к другим поездкам."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Связь файла с поездкой удалена"),
        @ApiResponse(responseCode = "404", description = "Поездка, файл или связь не найдены"),
        @ApiResponse(responseCode = "403", description = "Нет доступа к поездке")
    })
    public ResponseEntity<Void> removeTripFile(
            @CurrentUser Long userId,
            @Parameter(description = "ID поездки", required = true, example = "1")
            @PathVariable Long tripId,
            @Parameter(description = "ID файла", required = true, example = "1")
            @PathVariable Long fileId) {
        fileService.removeTripFile(userId, tripId, fileId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/trips/{tripId}/days/{dayId}/places/{placeId}/files")
    @Operation(
        summary = "Добавить файл к месту/событию",
        description = "Связывает уже зарегистрированный файл с конкретным местом в поездке."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Файл успешно добавлен к месту",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FileDto.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Файл уже добавлен к месту или нет доступа"),
        @ApiResponse(responseCode = "404", description = "Поездка, день, место или файл не найдены")
    })
    public ResponseEntity<FileDto> addFileToPlace(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @Parameter(description = "ID места/события", required = true, example = "1")
            @PathVariable Long placeId,
            @Parameter(description = "ID файла", required = true, example = "1")
            @RequestParam Long fileId,
            @Parameter(description = "Описание привязки файла к месту", example = "Фото в ресторане")
            @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileService.addFileToEvent(userId, placeId, fileId, description));
    }

    @GetMapping("/trips/{tripId}/days/{dayId}/places/{placeId}/files")
    @Operation(
        summary = "Получить все файлы места/события",
        description = "Возвращает список всех файлов, привязанных к конкретному месту в поездке."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Список файлов места получен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FileDto.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Поездка, день, место не найдены"),
        @ApiResponse(responseCode = "403", description = "Нет доступа к поездке")
    })
    public ResponseEntity<List<FileDto>> getPlaceFiles(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @Parameter(description = "ID места/события", required = true, example = "1")
            @PathVariable Long placeId) {
        return ResponseEntity.ok(fileService.getEventFiles(userId, placeId));
    }

    @DeleteMapping("/trips/{tripId}/days/{dayId}/places/{placeId}/files/{fileId}")
    @Operation(
        summary = "Удалить файл из места/события",
        description = "Убирает связь файла с местом. Сам файл остается в системе."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Связь файла с местом удалена"),
        @ApiResponse(responseCode = "404", description = "Поездка, день, место, файл или связь не найдены"),
        @ApiResponse(responseCode = "403", description = "Нет доступа к поездке")
    })
    public ResponseEntity<Void> removePlaceFile(
            @CurrentUser Long userId,
            @PathVariable Long tripId,
            @PathVariable Long dayId,
            @Parameter(description = "ID места/события", required = true, example = "1")
            @PathVariable Long placeId,
            @Parameter(description = "ID файла", required = true, example = "1")
            @PathVariable Long fileId) {
        fileService.removeEventFile(userId, placeId, fileId);
        return ResponseEntity.noContent().build();
    }
} 