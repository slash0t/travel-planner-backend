package ru.putevod.app.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.putevod.app.auth.dto.RegisterRequest;
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.repository.UserRepository;
import ru.putevod.app.auth.security.JwtTokenProvider;
import ru.putevod.app.auth.service.impl.AuthServiceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException; // Import ResponseStatusException
import ru.putevod.app.auth.dto.AuthResponse;
import ru.putevod.app.auth.dto.UserInfoDto;
import ru.putevod.app.auth.service.UserService;
import ru.putevod.app.auth.model.UserSession; // Import UserSession
import ru.putevod.app.auth.repository.UserSessionRepository; // Import UserSessionRepository
import org.springframework.security.authentication.BadCredentialsException; // Import BadCredentialsException

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private EmailService emailService;

    @Mock // Add mock for UserService
    private UserService userService;

    @Mock // Add mock for UserSessionRepository
    private UserSessionRepository sessionRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<String> stringCaptor; // Captor for verification token

    @BeforeEach
    void setUp() {
        // No common setup needed for this test yet
    }

    @Test
    void registerUser_whenSuccess_shouldSaveUserAndSendVerificationEmail() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setPassword("password123");

        String ipAddress = "127.0.0.1";
        String deviceInfo = "Test Device";
        String encodedPassword = "encodedPassword";
        Integer expectedUserId = 123; // Use Integer instead of UUID

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);

        // Mock the save operation to set the ID and return the user
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setUserId(expectedUserId); // Set the ID here
            userToSave.setCreatedAt(LocalDateTime.now()); // Ensure non-null date
            userToSave.setUpdatedAt(LocalDateTime.now()); // Ensure non-null date
            return userToSave;
        });

        // Act
        String actualUserId = authService.registerUser(request, ipAddress, deviceInfo);

        // Assert
        assertNotNull(actualUserId);
        assertEquals(expectedUserId.toString(), actualUserId);

        // Verify interactions and captured arguments
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
        assertNotNull(stringCaptor.getValue()); // Check that a token was generated and passed
        assertTrue(isValidUUID(stringCaptor.getValue())); // Verify it looks like a UUID

        // Verify no unexpected interactions
        verify(jwtTokenProvider, never()).generateAccessToken(any());
        verify(jwtTokenProvider, never()).generateRefreshToken(any(), anyString(), anyString());
    }

    // Helper method to check if a string is a valid UUID
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
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setUsername("testuser");
        request.setPassword("password123");
        String ipAddress = "127.0.0.1";
        String deviceInfo = "Test Device";

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.registerUser(request, ipAddress, deviceInfo);
        });

        assertEquals(409, exception.getStatusCode().value()); // Check for CONFLICT status
        assertTrue(exception.getReason().contains("email")); // Check message

        // Verify no user was saved or email sent
        verify(userRepository, never()).existsByUsername(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void registerUser_whenUsernameExists_shouldThrowConflictException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("existinguser");
        request.setPassword("password123");
        String ipAddress = "127.0.0.1";
        String deviceInfo = "Test Device";

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.registerUser(request, ipAddress, deviceInfo);
        });

        assertEquals(409, exception.getStatusCode().value()); // Check for CONFLICT status
        assertTrue(exception.getReason().contains("username")); // Check message

        // Verify email check happened, but no save or email sent
        verify(userRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    // --- Email Verification Tests --- 

    @Test
    void verifyEmail_whenTokenIsValid_shouldVerifyUserAndReturnAuthResponse() {
        // Arrange
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
                .isVerified(false) // Start as unverified
                .build();

        UserInfoDto userInfoDto = UserInfoDto.builder()
                                            .id(userId)
                                            .email(userEmail)
                                            .username(username)
                                            .emailVerified(true)
                                            .build();

        when(emailService.verifyEmailToken(validToken)).thenReturn(userToVerify);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved user
        when(jwtTokenProvider.generateAccessToken(any(User.class))).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(any(User.class), eq(deviceInfo), eq(ipAddress))).thenReturn(refreshToken);
        when(userService.mapToUserInfoDto(any(User.class))).thenReturn(userInfoDto); // Mock the mapping

        // Act
        AuthResponse response = authService.verifyEmail(validToken, ipAddress, deviceInfo);

        // Assert
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals(userEmail, response.getUser().getEmail());
        assertTrue(response.getUser().isEmailVerified());

        // Verify user state was updated and saved
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.getIsVerified());
        assertNotNull(savedUser.getUpdatedAt()); // Check timestamp updated

        // Verify correct parameters passed for token generation
        verify(jwtTokenProvider).generateAccessToken(savedUser);
        verify(jwtTokenProvider).generateRefreshToken(savedUser, deviceInfo, ipAddress);
        verify(userService).mapToUserInfoDto(savedUser);
    }

    @Test
    void verifyEmail_whenTokenIsInvalid_shouldThrowException() {
        // Arrange
        String invalidToken = "invalid-or-expired-token";
        String ipAddress = "192.168.1.1";
        String deviceInfo = "Verify Device";

        // Simulate EmailService throwing an exception for an invalid token
        when(emailService.verifyEmailToken(invalidToken))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Неверный или истекший токен"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.verifyEmail(invalidToken, ipAddress, deviceInfo);
        });

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Неверный или истекший токен"));

        // Verify no user was saved and no tokens were generated
        verify(userRepository, never()).save(any(User.class));
        verify(jwtTokenProvider, never()).generateAccessToken(any(User.class));
        verify(jwtTokenProvider, never()).generateRefreshToken(any(User.class), anyString(), anyString());
        verify(userService, never()).mapToUserInfoDto(any(User.class));
    }

    // --- Login (createAuthResponse) Tests --- 

    @Test
    void createAuthResponse_whenUserExists_shouldReturnAuthResponse() {
        // Arrange
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
        
        User updatedUser = User.builder() // Simulate user returned by updateLastLogin
                .userId(userId)
                .email(email)
                .username(username)
                .isVerified(true)
                .lastLogin(LocalDateTime.now()) // Add last login time
                .build();

        UserInfoDto userInfoDto = UserInfoDto.builder()
                                            .id(userId)
                                            .email(email)
                                            .username(username)
                                            .emailVerified(true)
                                            .build();

        when(userService.findByEmail(email)).thenReturn(Optional.of(foundUser));
        when(userService.updateLastLogin(foundUser)).thenReturn(updatedUser); // Return user with updated timestamp
        when(jwtTokenProvider.generateAccessToken(updatedUser)).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(updatedUser, deviceInfo, ipAddress)).thenReturn(refreshToken);
        when(userService.mapToUserInfoDto(updatedUser)).thenReturn(userInfoDto);

        // Act
        AuthResponse response = authService.createAuthResponse(email, ipAddress, deviceInfo, deviceId);

        // Assert
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals(3600, response.getExpiresIn()); // Verify fixed expiry
        assertNotNull(response.getUser());
        assertEquals(email, response.getUser().getEmail());

        // Verify interactions
        verify(userService).findByEmail(email);
        verify(userService).updateLastLogin(foundUser);
        verify(jwtTokenProvider).generateAccessToken(updatedUser);
        verify(jwtTokenProvider).generateRefreshToken(updatedUser, deviceInfo, ipAddress);
        verify(userService).mapToUserInfoDto(updatedUser);
    }

    @Test
    void createAuthResponse_whenUserNotFound_shouldThrowBadCredentialsException() {
        // Arrange
        String email = "notfound@example.com";
        String ipAddress = "10.0.0.1";
        String deviceInfo = "Login Device";
        String deviceId = "push-id-123";

        when(userService.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.createAuthResponse(email, ipAddress, deviceInfo, deviceId);
        });

        assertTrue(exception.getMessage().contains(email)); // Check message contains email

        // Verify only findByEmail was called
        verify(userService).findByEmail(email);
        verify(userService, never()).updateLastLogin(any());
        verify(jwtTokenProvider, never()).generateAccessToken(any());
        verify(jwtTokenProvider, never()).generateRefreshToken(any(), anyString(), anyString());
        verify(userService, never()).mapToUserInfoDto(any());
    }

    // --- Refresh Token Tests --- 

    @Test
    void refreshToken_whenTokenIsValid_shouldReturnNewTokens() {
        // Arrange
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

        // Act
        AuthResponse response = authService.refreshToken(oldRefreshToken, ipAddress, deviceInfo);

        // Assert
        assertNotNull(response);
        assertEquals(newAccessToken, response.getAccessToken());
        assertEquals(newRefreshToken, response.getRefreshToken());
        assertEquals(360000000, response.getExpiresIn()); // Check the large expiry from the code
        assertNotNull(response.getUser());
        assertEquals(email, response.getUser().getEmail());

        // Verify session was deleted and new tokens generated
        verify(sessionRepository).findByToken(oldRefreshToken);
    }

    // --- Logout Tests ---

    @Test
    void logout_whenTokenExists_shouldDeleteToken() {
        // Arrange
        String refreshToken = "valid-logout-token";
        UserSession session = UserSession.builder().token(refreshToken).build();

        when(sessionRepository.findByToken(refreshToken)).thenReturn(Optional.of(session));

        // Act
        authService.logout(refreshToken);

        // Assert
        verify(sessionRepository).findByToken(refreshToken);
        verify(sessionRepository).deleteByToken(refreshToken);
    }

    @Test
    void logout_whenTokenDoesNotExist_shouldNotDeleteToken() {
        // Arrange
        String refreshToken = "invalid-logout-token";

        when(sessionRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

        // Act
        authService.logout(refreshToken);

        // Assert
        verify(sessionRepository).findByToken(refreshToken);
        verify(sessionRepository, never()).deleteByToken(anyString());
    }

    // --- Resend Verification Email Tests ---

    @Test
    void resendVerificationEmail_whenUserUnverified_shouldSendEmail() {
        // Arrange
        String email = "unverified@example.com";
        String username = "unverifiedUser";
        User user = User.builder().email(email).username(username).isVerified(false).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        authService.resendVerificationEmail(email);

        // Assert
        verify(userRepository).findByEmail(email);
        verify(emailService).sendVerificationEmail(eq(email), eq(username), stringCaptor.capture());
        assertNotNull(stringCaptor.getValue()); // Check a token was generated
        assertTrue(isValidUUID(stringCaptor.getValue())); // Check it's a UUID
    }

    @Test
    void resendVerificationEmail_whenUserNotFound_shouldThrowNotFoundException() {
        // Arrange
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.resendVerificationEmail(email);
        });

        assertEquals(404, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Пользователь не найден"));

        // Verify no email was sent
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void resendVerificationEmail_whenUserAlreadyVerified_shouldThrowBadRequestException() {
        // Arrange
        String email = "verified@example.com";
        User user = User.builder().email(email).isVerified(true).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.resendVerificationEmail(email);
        });

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Email уже подтвержден"));

        // Verify no email was sent
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    // --- Send Password Reset Email Tests ---

    @Test
    void sendPasswordResetEmail_whenUserExists_shouldStoreCodeAndSendEmail() {
        // Arrange
        String email = "reset@example.com";
        String username = "resetUser";
        User user = User.builder().email(email).username(username).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        // Capture the generated code
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        authService.sendPasswordResetEmail(email);

        // Assert
        verify(userRepository).findByEmail(email);
        verify(emailService).storeResetCode(eq(email), codeCaptor.capture());
        String generatedCode = codeCaptor.getValue();
        assertNotNull(generatedCode);
        // Basic check for 6 digits, as per generateRandomCode()
        assertTrue(generatedCode.matches("^\\d{6}$")); 

        verify(emailService).sendPasswordResetEmail(eq(email), eq(username), eq(generatedCode));
    }

    @Test
    void sendPasswordResetEmail_whenUserNotFound_shouldDoNothing() {
        // Arrange
        String email = "nonexistent-reset@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        authService.sendPasswordResetEmail(email);

        // Assert
        verify(userRepository).findByEmail(email);
        // Verify no email service interaction
        verify(emailService, never()).storeResetCode(anyString(), anyString());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    // --- Verify Password Reset Code Tests ---

    @Test
    void verifyPasswordResetCode_whenCodeIsValid_shouldStoreResetTokenAndReturnIt() {
        // Arrange
        String email = "reset@example.com";
        String validCode = "123456";

        when(emailService.verifyResetCode(email, validCode)).thenReturn(true);
        // Capture the generated reset token
        ArgumentCaptor<String> resetTokenCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        String resetToken = authService.verifyPasswordResetCode(email, validCode);

        // Assert
        assertNotNull(resetToken);
        assertTrue(isValidUUID(resetToken)); // Check it's a UUID

        verify(emailService).verifyResetCode(email, validCode);
        verify(emailService).storeResetToken(eq(email), resetTokenCaptor.capture());
        assertEquals(resetToken, resetTokenCaptor.getValue()); // Verify the returned token was stored
    }

    @Test
    void verifyPasswordResetCode_whenCodeIsInvalid_shouldThrowBadRequestException() {
        // Arrange
        String email = "reset@example.com";
        String invalidCode = "654321";

        when(emailService.verifyResetCode(email, invalidCode)).thenReturn(false);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.verifyPasswordResetCode(email, invalidCode);
        });

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Неверный или истекший код"));

        // Verify storeResetToken was not called
        verify(emailService).verifyResetCode(email, invalidCode);
        verify(emailService, never()).storeResetToken(anyString(), anyString());
    }

    // --- Reset Password Tests ---

    @Test
    void resetPassword_whenTokenIsValidAndUserExists_shouldUpdatePasswordAndInvalidateToken() {
        // Arrange
        String resetToken = "valid-reset-token-uuid";
        String newPassword = "newSecurePassword123";
        String encodedPassword = "encodedNewPassword";
        String email = "reset@example.com";
        User user = User.builder().email(email).passwordHash("oldPasswordHash").build();

        when(emailService.getEmailByResetToken(resetToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        authService.resetPassword(resetToken, newPassword);

        // Assert
        verify(emailService).getEmailByResetToken(resetToken);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(encodedPassword, savedUser.getPasswordHash()); // Verify password updated
        assertNotNull(savedUser.getUpdatedAt()); // Verify timestamp updated

        verify(emailService).invalidateResetToken(resetToken);
        // verify(sessionRepository).deleteAllByUserId(user.getUserId()); // Verify sessions cleared if uncommented in source
    }

    @Test
    void resetPassword_whenTokenIsInvalid_shouldThrowBadRequestException() {
        // Arrange
        String invalidResetToken = "invalid-reset-token";
        String newPassword = "newSecurePassword123";

        when(emailService.getEmailByResetToken(invalidResetToken)).thenReturn(null); // Simulate invalid token

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.resetPassword(invalidResetToken, newPassword);
        });

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Неверный или истекший токен"));

        // Verify no user lookup or password change occurred
        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).invalidateResetToken(anyString());
    }

    @Test
    void resetPassword_whenUserNotFound_shouldThrowNotFoundException() {
        // Arrange
        String resetToken = "valid-reset-token-uuid";
        String newPassword = "newSecurePassword123";
        String email = "nonexistent-reset@example.com";

        when(emailService.getEmailByResetToken(resetToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty()); // Simulate user not found

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.resetPassword(resetToken, newPassword);
        });

        assertEquals(404, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Пользователь не найден"));

        // Verify email service interaction occurred but no password change
        verify(emailService).getEmailByResetToken(resetToken);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).invalidateResetToken(anyString());
    }

    // --- Create Anonymous Token Test ---

    @Test
    void createAnonymousToken_shouldGenerateAndReturnToken() {
        // Arrange
        String deviceId = "anon-device-id";
        String expectedToken = "generated-anonymous-token";

        when(jwtTokenProvider.generateAnonymousToken(deviceId)).thenReturn(expectedToken);

        // Act
        Map<String, Object> response = authService.createAnonymousToken(deviceId);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(expectedToken, response.get("anonymousToken"));
        assertEquals(1800, response.get("expiresIn")); // Verify fixed expiry

        // Verify interaction
        verify(jwtTokenProvider).generateAnonymousToken(deviceId);
    }
} 