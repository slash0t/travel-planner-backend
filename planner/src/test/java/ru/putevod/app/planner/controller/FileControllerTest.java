package ru.putevod.app.planner.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.putevod.app.planner.dto.FileDto;
import ru.putevod.app.planner.service.FileService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FileControllerTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    private Long userId;
    private Long fileId;
    private Long tripId;
    private Long dayId;
    private Long placeId;
    private FileDto mockFileDto;
    private MultipartFile mockMultipartFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = 1L;
        fileId = 1L;
        tripId = 1L;
        dayId = 1L;
        placeId = 1L;

        mockFileDto = FileDto.builder()
                .id(fileId)
                .userId(userId)
                .fileName("test.jpg")
                .fileType("image/jpeg")
                .fileSize(1024)
                .description("Test file")
                .build();

        mockMultipartFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    @Test
    void uploadFile_ShouldReturnCreatedFile() {
        when(fileService.uploadFile(eq(userId), any(MultipartFile.class), anyString()))
                .thenReturn(mockFileDto);

        ResponseEntity<FileDto> response = fileController.uploadFile(userId, mockMultipartFile, "Test file");

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockFileDto.getId(), response.getBody().getId());
        assertEquals(mockFileDto.getFileName(), response.getBody().getFileName());
        verify(fileService).uploadFile(eq(userId), any(MultipartFile.class), anyString());
    }

    @Test
    void getFileInfo_ShouldReturnFile() {
        when(fileService.getFileInfo(userId, fileId))
                .thenReturn(mockFileDto);

        ResponseEntity<FileDto> response = fileController.getFileInfo(userId, fileId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockFileDto.getId(), response.getBody().getId());
        assertEquals(mockFileDto.getFileName(), response.getBody().getFileName());
        verify(fileService).getFileInfo(userId, fileId);
    }

    @Test
    void downloadFile_ShouldReturnFileResource() {
        byte[] fileContent = "test content".getBytes();
        when(fileService.getFileInfo(userId, fileId))
                .thenReturn(mockFileDto);
        when(fileService.downloadFile(userId, fileId))
                .thenReturn(fileContent);

        ResponseEntity<Resource> response = fileController.downloadFile(userId, fileId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ByteArrayResource);
        verify(fileService).getFileInfo(userId, fileId);
        verify(fileService).downloadFile(userId, fileId);
    }

    @Test
    void deleteFile_ShouldReturnNoContent() {
        doNothing().when(fileService).deleteFile(userId, fileId);

        ResponseEntity<Void> response = fileController.deleteFile(userId, fileId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(fileService).deleteFile(userId, fileId);
    }

    @Test
    void addFileToTrip_ShouldReturnCreatedFile() {
        when(fileService.addFileToTrip(eq(userId), eq(tripId), eq(fileId), anyString()))
                .thenReturn(mockFileDto);

        ResponseEntity<FileDto> response = fileController.addFileToTrip(userId, tripId, fileId, "Test file");

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockFileDto.getId(), response.getBody().getId());
        assertEquals(mockFileDto.getFileName(), response.getBody().getFileName());
        verify(fileService).addFileToTrip(eq(userId), eq(tripId), eq(fileId), anyString());
    }

    @Test
    void getTripFiles_ShouldReturnFiles() {
        List<FileDto> files = Arrays.asList(mockFileDto);
        when(fileService.getTripFiles(userId, tripId))
                .thenReturn(files);

        ResponseEntity<List<FileDto>> response = fileController.getTripFiles(userId, tripId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(mockFileDto.getId(), response.getBody().get(0).getId());
        verify(fileService).getTripFiles(userId, tripId);
    }

    @Test
    void removeTripFile_ShouldReturnNoContent() {
        doNothing().when(fileService).removeTripFile(userId, tripId, fileId);

        ResponseEntity<Void> response = fileController.removeTripFile(userId, tripId, fileId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(fileService).removeTripFile(userId, tripId, fileId);
    }

    @Test
    void addFileToPlace_ShouldReturnCreatedFile() {
        when(fileService.addFileToEvent(eq(userId), eq(placeId), eq(fileId), anyString()))
                .thenReturn(mockFileDto);

        ResponseEntity<FileDto> response = fileController.addFileToPlace(userId, tripId, dayId, placeId, fileId, "Test file");

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockFileDto.getId(), response.getBody().getId());
        assertEquals(mockFileDto.getFileName(), response.getBody().getFileName());
        verify(fileService).addFileToEvent(eq(userId), eq(placeId), eq(fileId), anyString());
    }

    @Test
    void getPlaceFiles_ShouldReturnFiles() {
        List<FileDto> files = Arrays.asList(mockFileDto);
        when(fileService.getEventFiles(userId, placeId))
                .thenReturn(files);

        ResponseEntity<List<FileDto>> response = fileController.getPlaceFiles(userId, tripId, dayId, placeId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(mockFileDto.getId(), response.getBody().get(0).getId());
        verify(fileService).getEventFiles(userId, placeId);
    }

    @Test
    void removePlaceFile_ShouldReturnNoContent() {
        doNothing().when(fileService).removeEventFile(userId, placeId, fileId);

        ResponseEntity<Void> response = fileController.removePlaceFile(userId, tripId, dayId, placeId, fileId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(fileService).removeEventFile(userId, placeId, fileId);
    }
} 