package ru.putevod.app.planner.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.putevod.app.planner.dto.FileDto;
import ru.putevod.app.planner.model.File;
import ru.putevod.app.planner.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class FileMapperTest {

    @Autowired
    private FileMapper fileMapper;

    @Test
    void toDto_ShouldMapCorrectly() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");

        File file = new File();
        file.setFileId(1L);
        file.setUser(user);
        file.setFileName("test.jpg");
        file.setFilePath("/path/to/test.jpg");
        file.setFileType("image/jpeg");
        file.setFileSize(1024);
        file.setCreatedAt(LocalDateTime.now());

        FileDto result = fileMapper.toDto(file);

        assertNotNull(result);
        assertEquals(file.getFileId(), result.getId());
        assertEquals(file.getUser().getUserId(), result.getUserId());
        assertEquals(file.getFileName(), result.getFileName());
        assertEquals(file.getFilePath(), result.getFilePath());
        assertEquals(file.getFileType(), result.getFileType());
        assertEquals(file.getFileSize(), result.getFileSize());
        assertEquals(file.getCreatedAt(), result.getCreatedAt());
    }

    @Test
    void toEntity_ShouldMapCorrectly() {
        FileDto fileDto = new FileDto();
        fileDto.setId(1L);
        fileDto.setUserId(1L);
        fileDto.setFileName("test.jpg");
        fileDto.setFilePath("/path/to/test.jpg");
        fileDto.setFileType("image/jpeg");
        fileDto.setFileSize(1024);
        fileDto.setDescription("Test file description");
        fileDto.setCreatedAt(LocalDateTime.now());

        File result = fileMapper.toEntity(fileDto);

        assertNotNull(result);
        assertEquals(fileDto.getId(), result.getFileId());
        assertEquals(fileDto.getFileName(), result.getFileName());
        assertEquals(fileDto.getFilePath(), result.getFilePath());
        assertEquals(fileDto.getFileType(), result.getFileType());
        assertEquals(fileDto.getFileSize(), result.getFileSize());
    }
} 