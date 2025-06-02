package ru.putevod.app.planner.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.putevod.app.planner.dto.UserDto;
import ru.putevod.app.planner.service.UserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserDto mockUserDto;
    private Long userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = 1L;

        mockUserDto = UserDto.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .admin(false)
                .verified(true)
                .createdAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .profilePictureUrl("https://example.com/profile.jpg")
                .build();
    }

    @Test
    void getCurrentUser_ShouldReturnUser() {
        when(userService.getUserById(userId))
                .thenReturn(mockUserDto);

        ResponseEntity<UserDto> response = userController.getCurrentUser(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockUserDto.getId(), response.getBody().getId());
        assertEquals(mockUserDto.getUsername(), response.getBody().getUsername());
        assertEquals(mockUserDto.getEmail(), response.getBody().getEmail());
        verify(userService).getUserById(userId);
    }

    @Test
    void getUserById_ShouldReturnUser() {
        when(userService.getUserById(userId))
                .thenReturn(mockUserDto);

        ResponseEntity<UserDto> response = userController.getUserById(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockUserDto.getId(), response.getBody().getId());
        assertEquals(mockUserDto.getUsername(), response.getBody().getUsername());
        assertEquals(mockUserDto.getEmail(), response.getBody().getEmail());
        verify(userService).getUserById(userId);
    }

    @Test
    void findByUsername_ShouldReturnUser() {
        String username = "testuser";
        when(userService.findByUsername(username))
                .thenReturn(mockUserDto);

        ResponseEntity<UserDto> response = userController.findByUsername(username);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockUserDto.getId(), response.getBody().getId());
        assertEquals(mockUserDto.getUsername(), response.getBody().getUsername());
        assertEquals(mockUserDto.getEmail(), response.getBody().getEmail());
        verify(userService).findByUsername(username);
    }

    @Test
    void findByEmail_ShouldReturnUser() {
        String email = "test@example.com";
        when(userService.findByEmail(email))
                .thenReturn(mockUserDto);

        ResponseEntity<UserDto> response = userController.findByEmail(email);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockUserDto.getId(), response.getBody().getId());
        assertEquals(mockUserDto.getUsername(), response.getBody().getUsername());
        assertEquals(mockUserDto.getEmail(), response.getBody().getEmail());
        verify(userService).findByEmail(email);
    }

    // Тест для updateUserProfile удален, так как этот метод теперь должен выполняться через auth сервис
    // Обновление профиля пользователя больше не доступно в planner сервисе
    
    /*
    @Test
    void updateUserProfile_ShouldReturnUpdatedUser() {
        UserDto updatedUserDto = UserDto.builder()
                .id(userId)
                .username("updateduser")
                .email("updated@example.com")
                .profilePictureUrl("https://example.com/new-profile.jpg")
                .build();

        when(userService.updateUserProfile(eq(userId), any(UserDto.class)))
                .thenReturn(updatedUserDto);

        ResponseEntity<UserDto> response = userController.updateUserProfile(userId, updatedUserDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(updatedUserDto.getId(), response.getBody().getId());
        assertEquals(updatedUserDto.getUsername(), response.getBody().getUsername());
        assertEquals(updatedUserDto.getEmail(), response.getBody().getEmail());
        assertEquals(updatedUserDto.getProfilePictureUrl(), response.getBody().getProfilePictureUrl());
        verify(userService).updateUserProfile(eq(userId), any(UserDto.class));
    }
    */
} 