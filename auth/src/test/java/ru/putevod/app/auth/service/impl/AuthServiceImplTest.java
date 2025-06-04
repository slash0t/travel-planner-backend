package ru.putevod.app.auth.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.putevod.app.auth.dto.UpdateProfileRequest;
import ru.putevod.app.auth.dto.UserInfoDto;
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.repository.UserRepository;
import ru.putevod.app.auth.security.JwtTokenProvider;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final Integer TEST_USER_ID = 1;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_AVATAR_URL = "https://example.com/avatar.jpg";
    private static final String TEST_SERVICE_TOKEN = "test-service-token";
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(TEST_USER_ID)
                .email(TEST_EMAIL)
                .username(TEST_USERNAME)
                .profilePictureUrl(TEST_AVATAR_URL)
                .isVerified(true)
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void updateUserProfile_Success() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .email("newemail@example.com")
                .username("newusername")
                .avatarUrl("https://example.com/new-avatar.jpg")
                .build();

        User updatedUser = User.builder()
                .userId(TEST_USER_ID)
                .email(request.getEmail())
                .username(request.getUsername())
                .profilePictureUrl(request.getAvatarUrl())
                .isVerified(true)
                .isAdmin(false)
                .createdAt(testUser.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(Long.valueOf(TEST_USER_ID))).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserInfoDto result = authService.updateUserProfile(TEST_USER_ID, request);

        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getId());
        assertEquals(request.getEmail(), result.getEmail());
        assertEquals(request.getUsername(), result.getUsername());
        assertEquals(request.getAvatarUrl(), result.getAvatarUrl());
        assertTrue(result.isEmailVerified());
        assertFalse(result.isAdmin());
        assertEquals(testUser.getCreatedAt(), result.getCreatedAt());

        verify(userRepository).findById(Long.valueOf(TEST_USER_ID));
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository).existsByUsername(request.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserProfile_UserNotFound() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .email("newemail@example.com")
                .build();

        when(userRepository.findById(Long.valueOf(TEST_USER_ID))).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.updateUserProfile(TEST_USER_ID, request));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Пользователь не найден", exception.getReason());

        verify(userRepository).findById(Long.valueOf(TEST_USER_ID));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProfile_EmailAlreadyExists() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .email("existing@example.com")
                .build();

        when(userRepository.findById(Long.valueOf(TEST_USER_ID))).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.updateUserProfile(TEST_USER_ID, request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Пользователь с таким email уже существует", exception.getReason());

        verify(userRepository).findById(Long.valueOf(TEST_USER_ID));
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProfile_UsernameAlreadyExists() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .username("existinguser")
                .build();

        when(userRepository.findById(Long.valueOf(TEST_USER_ID))).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.updateUserProfile(TEST_USER_ID, request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Пользователь с таким именем уже существует", exception.getReason());

        verify(userRepository).findById(Long.valueOf(TEST_USER_ID));
        verify(userRepository).existsByUsername(request.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProfile_PartialUpdate() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .avatarUrl("https://example.com/new-avatar.jpg")
                .build();

        User updatedUser = User.builder()
                .userId(TEST_USER_ID)
                .email(TEST_EMAIL)
                .username(TEST_USERNAME)
                .profilePictureUrl(request.getAvatarUrl())
                .isVerified(true)
                .isAdmin(false)
                .createdAt(testUser.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(Long.valueOf(TEST_USER_ID))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserInfoDto result = authService.updateUserProfile(TEST_USER_ID, request);

        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getId());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_USERNAME, result.getUsername());
        assertEquals(request.getAvatarUrl(), result.getAvatarUrl());
        assertTrue(result.isEmailVerified());
        assertFalse(result.isAdmin());
        assertEquals(testUser.getCreatedAt(), result.getCreatedAt());

        verify(userRepository).findById(Long.valueOf(TEST_USER_ID));
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUserById_WhenValidServiceToken_ShouldReturnUserInfo() {
        when(tokenProvider.validateServiceToken(TEST_SERVICE_TOKEN)).thenReturn(true);
        when(userRepository.findById(TEST_USER_ID.longValue())).thenReturn(Optional.of(testUser));

        UserInfoDto result = authService.getUserById(TEST_USER_ID, TEST_SERVICE_TOKEN);

        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getId());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_USERNAME, result.getUsername());
        assertEquals(TEST_AVATAR_URL, result.getAvatarUrl());
        assertTrue(result.isEmailVerified());
        assertFalse(result.isAdmin());
        assertNotNull(result.getCreatedAt());

        verify(tokenProvider).validateServiceToken(TEST_SERVICE_TOKEN);
        verify(userRepository).findById(TEST_USER_ID.longValue());
    }

    @Test
    void getUserById_WhenInvalidServiceToken_ShouldThrowUnauthorizedException() {
        when(tokenProvider.validateServiceToken(TEST_SERVICE_TOKEN)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.getUserById(TEST_USER_ID, TEST_SERVICE_TOKEN));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Неверный сервисный токен", exception.getReason());

        verify(tokenProvider).validateServiceToken(TEST_SERVICE_TOKEN);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getUserById_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(tokenProvider.validateServiceToken(TEST_SERVICE_TOKEN)).thenReturn(true);
        when(userRepository.findById(TEST_USER_ID.longValue())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.getUserById(TEST_USER_ID, TEST_SERVICE_TOKEN));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Пользователь не найден", exception.getReason());

        verify(tokenProvider).validateServiceToken(TEST_SERVICE_TOKEN);
        verify(userRepository).findById(TEST_USER_ID.longValue());
    }

    @Test
    void getUserById_WhenUserHasNullFields_ShouldHandleGracefully() {
        User userWithNullFields = User.builder()
                .userId(TEST_USER_ID)
                .email(TEST_EMAIL)
                .username(TEST_USERNAME)
                .isVerified(null)
                .isAdmin(null)
                .createdAt(LocalDateTime.now())
                .build();

        when(tokenProvider.validateServiceToken(TEST_SERVICE_TOKEN)).thenReturn(true);
        when(userRepository.findById(TEST_USER_ID.longValue())).thenReturn(Optional.of(userWithNullFields));

        UserInfoDto result = authService.getUserById(TEST_USER_ID, TEST_SERVICE_TOKEN);

        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getId());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_USERNAME, result.getUsername());
        assertFalse(result.isEmailVerified());
        assertFalse(result.isAdmin());
        assertNotNull(result.getCreatedAt());

        verify(tokenProvider).validateServiceToken(TEST_SERVICE_TOKEN);
        verify(userRepository).findById(TEST_USER_ID.longValue());
    }
} 