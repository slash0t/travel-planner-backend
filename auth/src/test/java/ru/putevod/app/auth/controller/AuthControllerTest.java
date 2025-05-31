package ru.putevod.app.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.putevod.app.auth.dto.*;
import ru.putevod.app.auth.service.AuthService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_IP = "127.0.0.1";
    private static final String TEST_USER_AGENT = "Mozilla/5.0";
    private static final String TEST_DEVICE_ID = "device123";
    private static final String TEST_TOKEN = "test-token";
    private static final String TEST_REFRESH_TOKEN = "refresh-token";
    private static final String TEST_SERVICE_TOKEN = "service-token";
    private static final String TEST_RESET_CODE = "123456";

    @Test
    void login_shouldReturnAuthResponse() {
        when(request.getRemoteAddr()).thenReturn(TEST_IP);
        when(request.getHeader("User-Agent")).thenReturn(TEST_USER_AGENT);
        
        LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD, TEST_DEVICE_ID);
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(TEST_TOKEN)
                .refreshToken(TEST_REFRESH_TOKEN)
                .expiresIn(3600)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authService.createAuthResponse(TEST_EMAIL, TEST_IP, TEST_USER_AGENT, TEST_DEVICE_ID))
                .thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(loginRequest, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse, response.getBody());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authService).createAuthResponse(TEST_EMAIL, TEST_IP, TEST_USER_AGENT, TEST_DEVICE_ID);
    }

    @Test
    void refreshToken_shouldReturnAuthResponse() {
        when(request.getRemoteAddr()).thenReturn(TEST_IP);
        when(request.getHeader("User-Agent")).thenReturn(TEST_USER_AGENT);
        
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(TEST_REFRESH_TOKEN);
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(TEST_TOKEN)
                .refreshToken(TEST_REFRESH_TOKEN)
                .expiresIn(3600)
                .build();

        when(authService.refreshToken(TEST_REFRESH_TOKEN, TEST_IP, TEST_USER_AGENT))
                .thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.refreshToken(refreshRequest, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse, response.getBody());
        verify(authService).refreshToken(TEST_REFRESH_TOKEN, TEST_IP, TEST_USER_AGENT);
    }

    @Test
    void register_shouldReturnCreatedResponse() {
        when(request.getRemoteAddr()).thenReturn(TEST_IP);
        when(request.getHeader("User-Agent")).thenReturn(TEST_USER_AGENT);
        
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setUsername(TEST_USERNAME);

        String userId = "123";
        when(authService.registerUser(registerRequest, TEST_IP, TEST_USER_AGENT))
                .thenReturn(userId);

        ResponseEntity<Map<String, Object>> response = authController.register(registerRequest, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Регистрация успешно завершена. Проверьте почту для подтверждения аккаунта.", 
                response.getBody().get("message"));
        assertEquals(userId, response.getBody().get("userId"));
        verify(authService).registerUser(registerRequest, TEST_IP, TEST_USER_AGENT);
    }

    @Test
    void verifyEmail_shouldReturnAuthResponse() {
        when(request.getRemoteAddr()).thenReturn(TEST_IP);
        when(request.getHeader("User-Agent")).thenReturn(TEST_USER_AGENT);
        
        EmailVerificationRequest verificationRequest = new EmailVerificationRequest(TEST_TOKEN);
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(TEST_TOKEN)
                .refreshToken(TEST_REFRESH_TOKEN)
                .expiresIn(3600)
                .build();

        when(authService.verifyEmail(TEST_TOKEN, TEST_IP, TEST_USER_AGENT))
                .thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.verifyEmail(verificationRequest, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse, response.getBody());
        verify(authService).verifyEmail(TEST_TOKEN, TEST_IP, TEST_USER_AGENT);
    }

    @Test
    void validateToken_shouldReturnValidationResponse() {
        TokenValidationRequest tokenRequest = new TokenValidationRequest(TEST_TOKEN);
        TokenValidationResponse validationResponse = TokenValidationResponse.builder()
                .valid(true)
                .email(TEST_EMAIL)
                .build();

        when(authService.validateToken(TEST_TOKEN, TEST_SERVICE_TOKEN))
                .thenReturn(validationResponse);

        ResponseEntity<TokenValidationResponse> response =
                authController.validateToken(tokenRequest, TEST_SERVICE_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(validationResponse, response.getBody());
        verify(authService).validateToken(TEST_TOKEN, TEST_SERVICE_TOKEN);
    }

    @Test
    void getUserInfo_shouldReturnUserInfo() {
        TokenValidationRequest tokenRequest = new TokenValidationRequest(TEST_TOKEN);
        Map<String, Object> userInfo = Map.of(
                "userId", 1L,
                "email", TEST_EMAIL,
                "username", TEST_USERNAME,
                "isAdmin", false
        );

        when(authService.getUserInfoFromToken(TEST_TOKEN, TEST_SERVICE_TOKEN))
                .thenReturn(userInfo);

        ResponseEntity<Map<String, Object>> response =
                authController.getUserInfo(tokenRequest, TEST_SERVICE_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userInfo, response.getBody());
        verify(authService).getUserInfoFromToken(TEST_TOKEN, TEST_SERVICE_TOKEN);
    }

    @Test
    void verifyResetCode_shouldReturnSuccessResponse() {
        VerifyResetCodeRequest verifyRequest = new VerifyResetCodeRequest(TEST_EMAIL, TEST_RESET_CODE);
        String resetToken = "reset-token-123";
        when(authService.verifyPasswordResetCode(TEST_EMAIL, TEST_RESET_CODE)).thenReturn(resetToken);

        ResponseEntity<Map<String, String>> response = authController.verifyResetCode(verifyRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Код подтверждения действителен", response.getBody().get("message"));
        assertEquals(resetToken, response.getBody().get("resetToken"));
        verify(authService).verifyPasswordResetCode(TEST_EMAIL, TEST_RESET_CODE);
    }

    @Test
    void resetPassword_shouldReturnSuccessResponse() {
        ResetPasswordRequest resetRequest = new ResetPasswordRequest(TEST_TOKEN, TEST_PASSWORD);
        doNothing().when(authService).resetPassword(TEST_TOKEN, TEST_PASSWORD);

        ResponseEntity<Map<String, String>> response = authController.resetPassword(resetRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Пароль успешно изменен", response.getBody().get("message"));
        verify(authService).resetPassword(TEST_TOKEN, TEST_PASSWORD);
    }

    @Test
    void logout_shouldReturnSuccessResponse() {
        RefreshTokenRequest logoutRequest = new RefreshTokenRequest(TEST_REFRESH_TOKEN);
        doNothing().when(authService).logout(TEST_REFRESH_TOKEN);

        ResponseEntity<Map<String, String>> response = authController.logout(logoutRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Вы успешно вышли из системы", response.getBody().get("message"));
        verify(authService).logout(TEST_REFRESH_TOKEN);
    }

    @Test
    void resendVerification_shouldReturnSuccessResponse() {
        EmailRequest emailRequest = new EmailRequest(TEST_EMAIL);
        doNothing().when(authService).resendVerificationEmail(TEST_EMAIL);

        ResponseEntity<Map<String, String>> response = authController.resendVerification(emailRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Новое письмо подтверждения отправлено на ваш email", response.getBody().get("message"));
        verify(authService).resendVerificationEmail(TEST_EMAIL);
    }

    @Test
    void forgotPassword_shouldReturnSuccessResponse() {
        EmailRequest emailRequest = new EmailRequest(TEST_EMAIL);
        doNothing().when(authService).sendPasswordResetEmail(TEST_EMAIL);

        ResponseEntity<Map<String, String>> response = authController.forgotPassword(emailRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Инструкции по восстановлению пароля отправлены на указанный email", response.getBody().get("message"));
        verify(authService).sendPasswordResetEmail(TEST_EMAIL);
    }

    @Test
    void getAnonymousToken_shouldReturnTokenResponse() {
        Map<String, String> params = new HashMap<>();
        params.put("deviceId", TEST_DEVICE_ID);
        Map<String, Object> tokenResponse = Map.of(
            "anonymousToken", TEST_TOKEN,
            "expiresIn", 1800
        );

        when(authService.createAnonymousToken(TEST_DEVICE_ID)).thenReturn(tokenResponse);

        ResponseEntity<Map<String, Object>> response = authController.getAnonymousToken(params);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tokenResponse, response.getBody());
        verify(authService).createAnonymousToken(TEST_DEVICE_ID);
    }
} 