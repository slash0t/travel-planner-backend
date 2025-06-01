package ru.putevod.app.planner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
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
import ru.putevod.app.planner.service.TripService;
import ru.putevod.app.planner.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private FileRepository fileRepository;
    @Mock
    private TripFileRepository tripFileRepository;
    @Mock
    private EventFileRepository eventFileRepository;
    @Mock
    private UserService userService;
    @Mock
    private TripService tripService;
    @Mock
    private EventService eventService;
    @Mock
    private FileMapper fileMapper;

    @InjectMocks
    private FileServiceImpl fileService;

    private User user;
    private Trip trip;
    private Event event;
    private File file;
    private FileDto fileDto;
    private TripFile tripFile;
    private EventFile eventFile;
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);

        trip = new Trip();
        trip.setTripId(1L);

        event = new Event();
        event.setEventId(1L);
        TripDay day = new TripDay();
        day.setTrip(trip);
        event.setDay(day);

        file = File.builder()
                .fileId(1L)
                .user(user)
                .fileName("test.txt")
                .filePath("test-path")
                .fileType("text/plain")
                .fileSize(100)
                .build();

        fileDto = new FileDto();
        fileDto.setId(1L);
        fileDto.setFileName("test.txt");
        fileDto.setFilePath("test-path");
        fileDto.setFileType("text/plain");
        fileDto.setFileSize(100);

        tripFile = TripFile.builder()
                .trip(trip)
                .file(file)
                .description("Test description")
                .build();

        eventFile = EventFile.builder()
                .event(event)
                .file(file)
                .description("Test description")
                .build();

        multipartFile = new MockMultipartFile(
                "test.txt",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );
    }

    @Test
    void uploadFile_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(file).when(fileRepository).save(any());
        doReturn(fileDto).when(fileMapper).toDto(any());

        FileDto result = fileService.uploadFile(1L, multipartFile, "Test description");

        assertNotNull(result);
        verify(fileRepository).save(any());
    }

    @Test
    void uploadFile_InvalidPath() {
        MultipartFile invalidFile = new MockMultipartFile(
                "../test.txt",
                "../test.txt",
                "text/plain",
                "test content".getBytes()
        );

        assertThrows(BadRequestException.class, () ->
                fileService.uploadFile(1L, invalidFile, "Test description"));
    }

    @Test
    void getFileInfo_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(Optional.of(file)).when(fileRepository).findById(anyLong());
        doReturn(fileDto).when(fileMapper).toDto(any());

        FileDto result = fileService.getFileInfo(1L, 1L);

        assertNotNull(result);
        assertEquals(fileDto.getId(), result.getId());
    }

    @Test
    void getFileInfo_NotFound() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(Optional.empty()).when(fileRepository).findById(anyLong());

        assertThrows(ResourceNotFoundException.class, () ->
                fileService.getFileInfo(1L, 1L));
    }

    @Test
    void downloadFile_NotSupported() {
        assertThrows(BadRequestException.class, () ->
                fileService.downloadFile(1L, 1L));
    }

    @Test
    void deleteFile_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(Optional.of(file)).when(fileRepository).findById(anyLong());

        fileService.deleteFile(1L, 1L);

        verify(fileRepository).delete(any());
    }

    @Test
    void deleteFile_NotOwner() {
        User otherUser = new User();
        otherUser.setUserId(2L);
        file.setUser(otherUser);

        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(Optional.of(file)).when(fileRepository).findById(anyLong());

        assertThrows(BadRequestException.class, () ->
                fileService.deleteFile(1L, 1L));
    }

    @Test
    void addFileToTrip_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(Optional.of(file)).when(fileRepository).findById(anyLong());
        doReturn(false).when(tripFileRepository).existsByTripAndFile(any(), any());
        doReturn(tripFile).when(tripFileRepository).save(any());
        doReturn(fileDto).when(fileMapper).toDto(any());

        FileDto result = fileService.addFileToTrip(1L, 1L, 1L, "Test description");

        assertNotNull(result);
        verify(tripFileRepository).save(any());
    }

    @Test
    void addFileToEvent_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(event).when(eventService).getEventEntityById(anyLong());
        doReturn(true).when(tripService).hasAccessToTrip(any(), any(), eq("admin"), eq("write"));
        doReturn(Optional.of(file)).when(fileRepository).findById(anyLong());
        doReturn(false).when(eventFileRepository).existsByEventAndFile(any(), any());
        doReturn(eventFile).when(eventFileRepository).save(any());
        doReturn(fileDto).when(fileMapper).toDto(any());

        FileDto result = fileService.addFileToEvent(1L, 1L, 1L, "Test description");

        assertNotNull(result);
        verify(eventFileRepository).save(any());
    }

    @Test
    void getTripFiles_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(List.of(file)).when(fileRepository).findByTripId(anyLong());
        doReturn(Optional.of(tripFile)).when(tripFileRepository).findByTripAndFile(any(), any());
        doReturn(fileDto).when(fileMapper).toDto(any());

        List<FileDto> result = fileService.getTripFiles(1L, 1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getEventFiles_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(event).when(eventService).getEventEntityById(anyLong());
        doReturn(true).when(tripService).hasAccessToTrip(any(), any(), eq("admin"), eq("read"), eq("write"));
        doReturn(List.of(file)).when(fileRepository).findByEventId(anyLong());
        doReturn(Optional.of(eventFile)).when(eventFileRepository).findByEventAndFile(any(), any());
        doReturn(fileDto).when(fileMapper).toDto(any());

        List<FileDto> result = fileService.getEventFiles(1L, 1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void removeTripFile_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(trip).when(tripService).getTripEntityWithAccessCheck(anyLong(), anyLong());
        doReturn(Optional.of(file)).when(fileRepository).findById(anyLong());
        doReturn(Optional.of(tripFile)).when(tripFileRepository).findByTripAndFile(any(), any());

        fileService.removeTripFile(1L, 1L, 1L);

        verify(tripFileRepository).delete(any());
    }

    @Test
    void removeEventFile_Success() {
        doReturn(user).when(userService).getUserEntityById(anyLong());
        doReturn(event).when(eventService).getEventEntityById(anyLong());
        doReturn(true).when(tripService).hasAccessToTrip(any(), any(), eq("admin"), eq("write"));
        doReturn(Optional.of(file)).when(fileRepository).findById(anyLong());
        doReturn(Optional.of(eventFile)).when(eventFileRepository).findByEventAndFile(any(), any());

        fileService.removeEventFile(1L, 1L, 1L);

        verify(eventFileRepository).delete(any());
    }
} 