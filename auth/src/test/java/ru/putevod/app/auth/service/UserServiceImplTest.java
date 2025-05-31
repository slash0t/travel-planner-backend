package ru.putevod.app.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.putevod.app.auth.dto.UserInfoDto;
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.repository.UserRepository;
import ru.putevod.app.auth.service.impl.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1)
                .email("test@example.com")
                .username("testuser")
                .isVerified(true)
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findByEmail_whenUserExists_shouldReturnUser() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByEmail(testUser.getEmail());

        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        verify(userRepository).findByEmail(testUser.getEmail());
    }

    @Test
    void findByEmail_whenUserDoesNotExist_shouldReturnEmpty() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail("nonexistent@example.com");

        assertTrue(result.isEmpty());
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void findByUsername_whenUserExists_shouldReturnUser() {
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByUsername(testUser.getUsername());

        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findByUsername(testUser.getUsername());
    }

    @Test
    void findByUsername_whenUserDoesNotExist_shouldReturnEmpty() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername("nonexistent");

        assertTrue(result.isEmpty());
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void updateLastLogin_shouldUpdateAndSaveUser() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateLastLogin(testUser);

        assertNotNull(result.getLastLogin());
        verify(userRepository).save(testUser);
    }

    @Test
    void mapToUserInfoDto_shouldMapCorrectly() {
        UserInfoDto result = userService.mapToUserInfoDto(testUser);

        assertEquals(testUser.getUserId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertTrue(result.isEmailVerified());
        assertFalse(result.isAdmin());
        assertEquals(testUser.getCreatedAt(), result.getCreatedAt());
    }
} 