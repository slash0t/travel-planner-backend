package ru.putevod.app.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ru.putevod.app.auth.dto.TokenValidationResponse;
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.repository.UserRepository;
import ru.putevod.app.auth.security.JwtTokenProvider;
import ru.putevod.app.auth.service.impl.AuthServiceImpl;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private static final String TEST_TOKEN = "test-token";
    private static final String TEST_SERVICE_TOKEN = "test-service-token";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "testuser";
    private static final Integer TEST_USER_ID = 1;
    private static final Long TEST_USER_ID_LONG = 1L;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(TEST_USER_ID)
                .email(TEST_EMAIL)
                .username(TEST_USERNAME)
                .isVerified(true)
                .isAdmin(false)
                .build();
    }

    @Test
    void validateToken_whenServiceTokenInvalid_shouldReturnInvalidResponse() {
        when(tokenProvider.validateServiceToken(TEST_SERVICE_TOKEN)).thenReturn(false);

        TokenValidationResponse response = authService.validateToken(TEST_TOKEN, TEST_SERVICE_TOKEN);

        assertFalse(response.isValid());
        assertEquals("Отказано в доступе", response.getErrorMessage());
        assertEquals("AccessDenied", response.getErrorType());
    }

    @Test
    void validateToken_whenTokenInvalid_shouldReturnInvalidResponse() {
        when(tokenProvider.validateServiceToken(TEST_SERVICE_TOKEN)).thenReturn(true);
        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(false);

        TokenValidationResponse response = authService.validateToken(TEST_TOKEN, TEST_SERVICE_TOKEN);

        assertFalse(response.isValid());
    }

    @Test
    void validateToken_whenAnonymousToken_shouldReturnValidResponse() {
        when(tokenProvider.validateServiceToken(TEST_SERVICE_TOKEN)).thenReturn(true);
        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(true);
        when(tokenProvider.isAnonymousToken(TEST_TOKEN)).thenReturn(true);

        TokenValidationResponse response = authService.validateToken(TEST_TOKEN, TEST_SERVICE_TOKEN);

        assertTrue(response.isValid());
    }

    @Test
    void validateToken_whenValidToken_shouldReturnValidResponseWithUserInfo() {
        when(tokenProvider.validateServiceToken(TEST_SERVICE_TOKEN)).thenReturn(true);
        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(true);
        when(tokenProvider.isAnonymousToken(TEST_TOKEN)).thenReturn(false);
        when(tokenProvider.getEmailFromToken(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(tokenProvider.getUserIdFromToken(TEST_TOKEN)).thenReturn(TEST_USER_ID_LONG);
        // when(tokenProvider.getUsernameFromToken(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        // when(tokenProvider.isAdminFromToken(TEST_TOKEN)).thenReturn(false);
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        TokenValidationResponse response = authService.validateToken(TEST_TOKEN, TEST_SERVICE_TOKEN);

        assertTrue(response.isValid());
        assertEquals(TEST_USER_ID_LONG, response.getUserId());
        assertEquals(TEST_EMAIL, response.getEmail());
        assertEquals(TEST_USERNAME, response.getUsername());
        assertFalse(response.isAdmin());
    }

    @Test
    void validateToken_whenUserNotFound_shouldReturnInvalidResponse() {
        when(tokenProvider.validateServiceToken(TEST_SERVICE_TOKEN)).thenReturn(true);
        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(true);
        when(tokenProvider.isAnonymousToken(TEST_TOKEN)).thenReturn(false);
        when(tokenProvider.getEmailFromToken(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        TokenValidationResponse response = authService.validateToken(TEST_TOKEN, TEST_SERVICE_TOKEN);

        assertFalse(response.isValid());
    }

    @Test
    void getUserInfoFromToken_whenServiceTokenInvalid_shouldThrowException() {
        when(tokenProvider.validateServiceToken(TEST_SERVICE_TOKEN)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () ->
                authService.getUserInfoFromToken(TEST_TOKEN, TEST_SERVICE_TOKEN));
    }

    @Test
    void getUserInfoFromToken_whenTokenInvalid_shouldThrowException() {
        when(tokenProvider.validateServiceToken(TEST_SERVICE_TOKEN)).thenReturn(true);
        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () ->
                authService.getUserInfoFromToken(TEST_TOKEN, TEST_SERVICE_TOKEN));
    }

    @Test
    void getUserInfoFromToken_whenAnonymousToken_shouldReturnAnonymousInfo() {
        when(tokenProvider.validateServiceToken(TEST_SERVICE_TOKEN)).thenReturn(true);
        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(true);
        when(tokenProvider.isAnonymousToken(TEST_TOKEN)).thenReturn(true);

        Map<String, Object> info = authService.getUserInfoFromToken(TEST_TOKEN, TEST_SERVICE_TOKEN);

        assertTrue((Boolean) info.get("isAnonymous"));
    }

    @Test
    void getUserInfoFromToken_whenValidToken_shouldReturnUserInfo() {
        when(tokenProvider.validateServiceToken(TEST_SERVICE_TOKEN)).thenReturn(true);
        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(true);
        when(tokenProvider.isAnonymousToken(TEST_TOKEN)).thenReturn(false);
        when(tokenProvider.getEmailFromToken(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(tokenProvider.getUserIdFromToken(TEST_TOKEN)).thenReturn(TEST_USER_ID_LONG);
        // when(tokenProvider.getUsernameFromToken(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        // when(tokenProvider.isAdminFromToken(TEST_TOKEN)).thenReturn(false);
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        Map<String, Object> info = authService.getUserInfoFromToken(TEST_TOKEN, TEST_SERVICE_TOKEN);

        assertEquals(TEST_USER_ID_LONG, info.get("userId"));
        assertEquals(TEST_EMAIL, info.get("email"));
        assertEquals(TEST_USERNAME, info.get("username"));
        assertFalse((Boolean) info.get("isAdmin"));
        assertTrue((Boolean) info.get("verified"));
        assertArrayEquals(new String[]{"ROLE_USER"}, (String[]) info.get("roles"));
    }

    @Test
    void getUserInfoFromToken_whenUserNotFound_shouldReturnBasicInfo() {
        when(tokenProvider.validateServiceToken(TEST_SERVICE_TOKEN)).thenReturn(true);
        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(true);
        when(tokenProvider.isAnonymousToken(TEST_TOKEN)).thenReturn(false);
        when(tokenProvider.getEmailFromToken(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(tokenProvider.getUserIdFromToken(TEST_TOKEN)).thenReturn(TEST_USER_ID_LONG);
        // when(tokenProvider.getUsernameFromToken(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        // when(tokenProvider.isAdminFromToken(TEST_TOKEN)).thenReturn(false);
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        Map<String, Object> info = authService.getUserInfoFromToken(TEST_TOKEN, TEST_SERVICE_TOKEN);

        assertEquals(TEST_USER_ID_LONG, info.get("userId"));
        assertEquals(TEST_EMAIL, info.get("email"));
        assertEquals(TEST_USERNAME, info.get("username"));
        assertFalse((Boolean) info.get("isAdmin"));
        assertFalse(info.containsKey("verified"));
        assertFalse(info.containsKey("roles"));
    }
} 