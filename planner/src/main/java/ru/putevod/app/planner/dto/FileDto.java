package ru.putevod.app.planner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO файла с поддержкой локального хранения")
public class FileDto {
    
    @Schema(description = "Уникальный идентификатор файла", example = "1")
    private Long id;
    
    @Schema(description = "ID пользователя-владельца файла", example = "1")
    private Long userId;
    
    @Schema(description = "Оригинальное имя файла", example = "vacation_photo.jpg")
    private String fileName;
    
    @Schema(description = "Путь к файлу (используется как уникальный идентификатор на сервере)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String filePath;
    
    @Schema(description = "MIME тип файла", example = "image/jpeg")
    private String fileType;
    
    @Schema(description = "Размер файла в байтах", example = "1048576")
    private Integer fileSize;
    
    @Schema(description = "Описание файла", example = "Фото с отпуска в Париже")
    private String description;
    
    @Schema(description = "Дата и время создания файла", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "URL для скачивания файла (для веб-версии)", example = "https://api.example.com/files/1/download")
    private String downloadUrl;

    @Schema(description = "Указывает, что файл должен храниться локально на устройстве", example = "true")
    private Boolean requiresLocalStorage;
    
    @Schema(description = "Идентификатор для локального хранения (путь на устройстве)", example = "/storage/emulated/0/Pictures/vacation_photo.jpg")
    private String localStorageId;
} 