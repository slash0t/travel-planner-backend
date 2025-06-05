package ru.putevod.app.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import ru.putevod.app.auth.config.AppProperties;
import ru.putevod.app.auth.dto.AuthResponse;
import ru.putevod.app.auth.dto.RegisterRequest;
import ru.putevod.app.auth.dto.UserInfoDto;
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.model.UserSession;
import ru.putevod.app.auth.repository.UserRepository;
import ru.putevod.app.auth.repository.UserSessionRepository;
import ru.putevod.app.auth.security.JwtTokenProvider;
import ru.putevod.app.auth.service.impl.AuthServiceImpl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Spy
    private EmailService emailService;

    @Mock
    private UserService userService;

    @Mock
    private UserSessionRepository sessionRepository;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Jwt jwtConfig;

    @Mock
    private AnonymousUserService anonymousUserService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @BeforeEach
    void setUp() {
        setupAppPropertiesMocks();
    }

    private void setupAppPropertiesMocks() {
        when(appProperties.getJwt()).thenReturn(jwtConfig);
        when(jwtConfig.getAccessTokenExpirationMs()).thenReturn(3600000L);
        when(jwtConfig.getRefreshTokenExpirationMs()).thenReturn(360000000000L);
        when(jwtConfig.getAnonymousTokenExpirationMs()).thenReturn(1800000L);
    }

    @Test
    void registerUser_whenSuccess_shouldSaveUserAndSendVerificationEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setPassword("password123");

        String ipAddress = "127.0.0.1";
        String deviceInfo = "Test Device";
        String encodedPassword = "encodedPassword";
        Integer expectedUserId = 123;

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setUserId(expectedUserId);
            userToSave.setCreatedAt(LocalDateTime.now());
            userToSave.setUpdatedAt(LocalDateTime.now());
            return userToSave;
        });

        String actualUserId = authService.registerUser(request, ipAddress, deviceInfo);

        assertNotNull(actualUserId);
        assertEquals(expectedUserId.toString(), actualUserId);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(request.getEmail(), savedUser.getEmail());
        assertEquals(request.getUsername(), savedUser.getUsername());
        assertEquals(encodedPassword, savedUser.getPasswordHash());
        assertFalse(savedUser.getIsAdmin());
        assertFalse(savedUser.getIsVerified());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());

        verify(emailService).sendVerificationEmail(eq(request.getEmail()), eq(request.getUsername()), stringCaptor.capture());
        assertNotNull(stringCaptor.getValue());
        assertTrue(isValidUUID(stringCaptor.getValue()));

        verify(jwtTokenProvider, never()).generateAccessToken(any());
        verify(jwtTokenProvider, never()).generateRefreshToken(any(), anyString(), anyString());
    }

    private boolean isValidUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Test
    void registerUser_whenEmailExists_shouldThrowConflictException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setUsername("testuser");
        request.setPassword("password123");
        String ipAddress = "127.0.0.1";
        String deviceInfo = "Test Device";

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.registerUser(request, ipAddress, deviceInfo);
        });

        assertEquals(409, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("email"));

        verify(userRepository, never()).existsByUsername(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void registerUser_whenUsernameExists_shouldThrowConflictException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("existinguser");
        request.setPassword("password123");
        String ipAddress = "127.0.0.1";
        String deviceInfo = "Test Device";

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.registerUser(request, ipAddress, deviceInfo);
        });

        assertEquals(409, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("username"));

        verify(userRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void verifyEmail_whenTokenIsValid_shouldVerifyUserAndReturnAuthResponse() {
        String validToken = "valid-verification-token";
        String ipAddress = "192.168.1.1";
        String deviceInfo = "Verify Device";
        Integer userId = 456;
        String userEmail = "verify@example.com";
        String username = "verifyUser";
        String accessToken = "newAccessToken";
        String refreshToken = "newRefreshToken";

        User userToVerify = User.builder()
                .userId(userId)
                .email(userEmail)
                .username(username)
                .isVerified(false)
                .build();

        UserInfoDto userInfoDto = UserInfoDto.builder()
                .id(userId)
                .email(userEmail)
                .username(username)
                .emailVerified(true)
                .build();

        when(emailService.verifyEmailToken(validToken)).thenReturn(userToVerify);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any(User.class))).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(any(User.class), eq(deviceInfo), eq(ipAddress))).thenReturn(refreshToken);
        when(userService.mapToUserInfoDto(any(User.class))).thenReturn(userInfoDto);

        AuthResponse response = authService.verifyEmail(validToken, ipAddress, deviceInfo);

        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals(userEmail, response.getUser().getEmail());
        assertTrue(response.getUser().isEmailVerified());

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.getIsVerified());
        assertNotNull(savedUser.getUpdatedAt());

        verify(jwtTokenProvider).generateAccessToken(savedUser);
        verify(jwtTokenProvider).generateRefreshToken(savedUser, deviceInfo, ipAddress);
        verify(userService).mapToUserInfoDto(savedUser);

        // Проверяем, что миграция НЕ вызывается без deviceId
        verify(anonymousUserService, never()).migrateAnonymousUserToRegistered(anyString(), any(User.class));
    }

    @Test
    void verifyEmail_withDeviceId_shouldVerifyUserAndMigrateAnonymousData() {
        String validToken = "valid-verification-token";
        String ipAddress = "192.168.1.1";
        String deviceInfo = "Verify Device";
        String deviceId = "test-device-123";
        Integer userId = 456;
        String userEmail = "verify@example.com";
        String username = "verifyUser";
        String accessToken = "newAccessToken";
        String refreshToken = "newRefreshToken";

        User userToVerify = User.builder()
                .userId(userId)
                .email(userEmail)
                .username(username)
                .isVerified(false)
                .build();

        UserInfoDto userInfoDto = UserInfoDto.builder()
                .id(userId)
                .email(userEmail)
                .username(username)
                .emailVerified(true)
                .build();

        when(emailService.verifyEmailToken(validToken)).thenReturn(userToVerify);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any(User.class))).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(any(User.class), eq(deviceInfo), eq(ipAddress))).thenReturn(refreshToken);
        when(userService.mapToUserInfoDto(any(User.class))).thenReturn(userInfoDto);

        AuthResponse response = authService.verifyEmail(validToken, ipAddress, deviceInfo, deviceId);

        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals(userEmail, response.getUser().getEmail());
        assertTrue(response.getUser().isEmailVerified());

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.getIsVerified());
        assertNotNull(savedUser.getUpdatedAt());

        verify(jwtTokenProvider).generateAccessToken(savedUser);
        verify(jwtTokenProvider).generateRefreshToken(savedUser, deviceInfo, ipAddress);
        verify(userService).mapToUserInfoDto(savedUser);

        // Проверяем, что миграция вызывается с правильными параметрами
        verify(anonymousUserService).migrateAnonymousUserToRegistered(deviceId, savedUser);
    }

    @Test
    void verifyEmail_withEmptyDeviceId_shouldNotCallMigration() {
        String validToken = "valid-verification-token";
        String ipAddress = "192.168.1.1";
        String deviceInfo = "Verify Device";
        String emptyDeviceId = "   "; // Пустая строка с пробелами
        Integer userId = 456;
        String userEmail = "verify@example.com";
        String username = "verifyUser";
        String accessToken = "newAccessToken";
        String refreshToken = "newRefreshToken";

        User userToVerify = User.builder()
                .userId(userId)
                .email(userEmail)
                .username(username)
                .isVerified(false)
                .build();

        UserInfoDto userInfoDto = UserInfoDto.builder()
                .id(userId)
                .email(userEmail)
                .username(username)
                .emailVerified(true)
                .build();

        when(emailService.verifyEmailToken(validToken)).thenReturn(userToVerify);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any(User.class))).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(any(User.class), eq(deviceInfo), eq(ipAddress))).thenReturn(refreshToken);
        when(userService.mapToUserInfoDto(any(User.class))).thenReturn(userInfoDto);

        AuthResponse response = authService.verifyEmail(validToken, ipAddress, deviceInfo, emptyDeviceId);

        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());

        // Проверяем, что миграция НЕ вызывается для пустого deviceId
        verify(anonymousUserService, never()).migrateAnonymousUserToRegistered(anyString(), any(User.class));
    }

    @Test
    void verifyEmail_whenTokenIsInvalid_shouldThrowException() {
        String invalidToken = "invalid-or-expired-token";
        String ipAddress = "192.168.1.1";
        String deviceInfo = "Verify Device";

        when(emailService.verifyEmailToken(invalidToken))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Неверный или истекший токен"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.verifyEmail(invalidToken, ipAddress, deviceInfo);
        });

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Неверный или истекший токен"));

        verify(userRepository, never()).save(any(User.class));
        verify(jwtTokenProvider, never()).generateAccessToken(any(User.class));
        verify(jwtTokenProvider, never()).generateRefreshToken(any(User.class), anyString(), anyString());
        verify(userService, never()).mapToUserInfoDto(any(User.class));
    }

    @Test
    void createAuthResponse_whenUserExists_shouldReturnAuthResponse() {
        String email = "login@example.com";
        String ipAddress = "10.0.0.1";
        String deviceInfo = "Login Device";
        String deviceId = "push-id-123";
        Integer userId = 789;
        String username = "loginUser";
        String accessToken = "loginAccessToken";
        String refreshToken = "loginRefreshToken";

        User foundUser = User.builder()
                .userId(userId)
                .email(email)
                .username(username)
                .isVerified(true)
                .build();

        User updatedUser = User.builder()
                .userId(userId)
                .email(email)
                .username(username)
                .isVerified(true)
                .lastLogin(LocalDateTime.now())
                .build();

        UserInfoDto userInfoDto = UserInfoDto.builder()
                .id(userId)
                .email(email)
                .username(username)
                .emailVerified(true)
                .build();

        when(userService.findByEmail(email)).thenReturn(Optional.of(foundUser));
        when(userService.updateLastLogin(foundUser)).thenReturn(updatedUser);
        when(jwtTokenProvider.generateAccessToken(updatedUser)).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(updatedUser, deviceInfo, ipAddress)).thenReturn(refreshToken);
        when(userService.mapToUserInfoDto(updatedUser)).thenReturn(userInfoDto);

        AuthResponse response = authService.createAuthResponse(email, ipAddress, deviceInfo, deviceId);

        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals(3600, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals(email, response.getUser().getEmail());

        verify(userService).findByEmail(email);
        verify(userService).updateLastLogin(foundUser);
        verify(jwtTokenProvider).generateAccessToken(updatedUser);
        verify(jwtTokenProvider).generateRefreshToken(updatedUser, deviceInfo, ipAddress);
        verify(userService).mapToUserInfoDto(updatedUser);
    }

    @Test
    void createAuthResponse_whenUserNotFound_shouldThrowBadCredentialsException() {
        String email = "notfound@example.com";
        String ipAddress = "10.0.0.1";
        String deviceInfo = "Login Device";
        String deviceId = "push-id-123";

        when(userService.findByEmail(email)).thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.createAuthResponse(email, ipAddress, deviceInfo, deviceId);
        });

        assertTrue(exception.getMessage().contains(email));

        verify(userService).findByEmail(email);
        verify(userService, never()).updateLastLogin(any());
        verify(jwtTokenProvider, never()).generateAccessToken(any());
        verify(jwtTokenProvider, never()).generateRefreshToken(any(), anyString(), anyString());
        verify(userService, never()).mapToUserInfoDto(any());
    }

    @Test
    void refreshToken_whenTokenIsValid_shouldReturnNewTokens() {
        String oldRefreshToken = "valid-refresh-token";
        String ipAddress = "10.0.0.2";
        String deviceInfo = "Refresh Device";
        Integer userId = 987;
        String email = "refresh@example.com";
        String username = "refreshUser";
        String newAccessToken = "refreshedAccessToken";
        String newRefreshToken = "refreshedRefreshToken";

        User user = User.builder().userId(userId).email(email).username(username).build();
        UserSession session = UserSession.builder().token(oldRefreshToken).user(user).build();

        UserInfoDto userInfoDto = UserInfoDto.builder()
                .id(userId)
                .email(email)
                .username(username)
                .build();

        when(sessionRepository.findByToken(oldRefreshToken)).thenReturn(Optional.of(session));
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn(newAccessToken);
        when(jwtTokenProvider.generateRefreshToken(user, deviceInfo, ipAddress)).thenReturn(newRefreshToken);
        when(userService.mapToUserInfoDto(user)).thenReturn(userInfoDto);

        AuthResponse response = authService.refreshToken(oldRefreshToken, ipAddress, deviceInfo);

        assertNotNull(response);
        assertEquals(newAccessToken, response.getAccessToken());
        assertEquals(newRefreshToken, response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals(email, response.getUser().getEmail());

        verify(sessionRepository).findByToken(oldRefreshToken);
    }

    @Test
    void logout_whenTokenExists_shouldDeleteToken() {
        String refreshToken = "valid-logout-token";
        UserSession session = UserSession.builder().token(refreshToken).build();

        when(sessionRepository.findByToken(refreshToken)).thenReturn(Optional.of(session));

        authService.logout(refreshToken);

        verify(sessionRepository).findByToken(refreshToken);
        verify(sessionRepository).deleteByToken(refreshToken);
    }

    @Test
    void logout_whenTokenDoesNotExist_shouldNotDeleteToken() {
        String refreshToken = "invalid-logout-token";

        when(sessionRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

        authService.logout(refreshToken);

        verify(sessionRepository).findByToken(refreshToken);
        verify(sessionRepository, never()).deleteByToken(anyString());
    }

    @Test
    void resendVerificationEmail_whenUserUnverified_shouldSendEmail() {
        String email = "unverified@example.com";
        String username = "unverifiedUser";
        User user = User.builder().email(email).username(username).isVerified(false).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        authService.resendVerificationEmail(email);

        verify(userRepository).findByEmail(email);
        verify(emailService).sendVerificationEmail(eq(email), eq(username), stringCaptor.capture());
        assertNotNull(stringCaptor.getValue());
        assertTrue(isValidUUID(stringCaptor.getValue()));
    }

    @Test
    void resendVerificationEmail_whenUserNotFound_shouldThrowNotFoundException() {
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.resendVerificationEmail(email);
        });

        assertEquals(404, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Пользователь не найден"));

        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void resendVerificationEmail_whenUserAlreadyVerified_shouldThrowBadRequestException() {
        String email = "verified@example.com";
        User user = User.builder().email(email).isVerified(true).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.resendVerificationEmail(email);
        });

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Email уже подтвержден"));

        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendPasswordResetEmail_whenUserExists_shouldStoreCodeAndSendEmail() {
        String email = "reset@example.com";
        String username = "resetUser";
        User user = User.builder().email(email).username(username).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        authService.sendPasswordResetEmail(email);

        verify(emailService).sendPasswordResetEmail(eq(email), eq(username), codeCaptor.capture());

        String generatedCode = codeCaptor.getValue();
        assertNotNull(generatedCode);
        assertTrue(generatedCode.matches("^\\d{6}$"));

        verify(emailService).sendPasswordResetEmail(eq(email), eq(username), eq(generatedCode));
    }

    @Test
    void sendPasswordResetEmail_whenUserNotFound_shouldDoNothing() {
        String email = "nonexistent-reset@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        authService.sendPasswordResetEmail(email);

        verify(userRepository).findByEmail(email);
        verify(emailService, never()).storeResetCode(anyString(), anyString());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    void verifyPasswordResetCode_whenCodeIsValid_shouldStoreResetTokenAndReturnIt() {
        String email = "reset@example.com";
        String validCode = "123456";

        when(emailService.verifyResetCode(email, validCode)).thenReturn(true);
        ArgumentCaptor<String> resetTokenCaptor = ArgumentCaptor.forClass(String.class);

        String resetToken = authService.verifyPasswordResetCode(email, validCode);

        assertNotNull(resetToken);
        assertTrue(isValidUUID(resetToken));

        verify(emailService).verifyResetCode(email, validCode);
        verify(emailService).storeResetToken(eq(email), resetTokenCaptor.capture());
        assertEquals(resetToken, resetTokenCaptor.getValue());
    }

    @Test
    void verifyPasswordResetCode_whenCodeIsInvalid_shouldThrowBadRequestException() {
        String email = "reset@example.com";
        String invalidCode = "654321";

        when(emailService.verifyResetCode(email, invalidCode)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.verifyPasswordResetCode(email, invalidCode);
        });

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Неверный или истекший код"));

        verify(emailService).verifyResetCode(email, invalidCode);
        verify(emailService, never()).storeResetToken(anyString(), anyString());
    }

    @Test
    void resetPassword_whenTokenIsValidAndUserExists_shouldUpdatePasswordAndInvalidateToken() {
        String resetToken = "valid-reset-token-uuid";
        String newPassword = "newSecurePassword123";
        String encodedPassword = "encodedNewPassword";
        String email = "reset@example.com";
        User user = User.builder().email(email).passwordHash("oldPasswordHash").build();

        when(emailService.getEmailByResetToken(resetToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.resetPassword(resetToken, newPassword);

        verify(emailService).getEmailByResetToken(resetToken);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(encodedPassword, savedUser.getPasswordHash());
        assertNotNull(savedUser.getUpdatedAt());

        verify(emailService).invalidateResetToken(resetToken);
    }

    @Test
    void resetPassword_whenTokenIsInvalid_shouldThrowBadRequestException() {
        String invalidResetToken = "invalid-reset-token";
        String newPassword = "newSecurePassword123";

        when(emailService.getEmailByResetToken(invalidResetToken)).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.resetPassword(invalidResetToken, newPassword);
        });

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Неверный или истекший токен"));

        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).invalidateResetToken(anyString());
    }

    @Test
    void resetPassword_whenUserNotFound_shouldThrowNotFoundException() {
        String resetToken = "valid-reset-token-uuid";
        String newPassword = "newSecurePassword123";
        String email = "nonexistent-reset@example.com";

        when(emailService.getEmailByResetToken(resetToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.resetPassword(resetToken, newPassword);
        });

        assertEquals(404, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Пользователь не найден"));

        verify(emailService).getEmailByResetToken(resetToken);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).invalidateResetToken(anyString());
    }

    @Test
    void createAnonymousToken_shouldGenerateAndReturnToken() {
        String deviceId = "anon-device-id";
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("anonymousToken", "generated-anonymous-token");
        expectedResponse.put("expiresIn", 1800);
        expectedResponse.put("anonymousUserId", 123L);

        when(anonymousUserService.createAnonymousToken(deviceId, null, null)).thenReturn(expectedResponse);

        Map<String, Object> response = authService.createAnonymousToken(deviceId);

        assertNotNull(response);
        assertEquals(expectedResponse, response);

        verify(anonymousUserService).createAnonymousToken(deviceId, null, null);
    }
} 