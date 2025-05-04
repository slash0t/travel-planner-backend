package ru.putevod.app.planner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class FileDto {
    private Long id;
    private Long userId;
    private String fileName;
    private String filePath;
    private String fileType;
    private Integer fileSize;
    private String description;
    private LocalDateTime createdAt;
    private String downloadUrl;
    
    // Поля для работы с локальным хранилищем мобильного приложения
    private Boolean requiresLocalStorage;
    private String localStorageId;
} 