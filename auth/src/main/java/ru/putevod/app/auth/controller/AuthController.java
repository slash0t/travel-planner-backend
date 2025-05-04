package ru.putevod.app.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.putevod.app.auth.dto.AuthResponse;
import ru.putevod.app.auth.dto.LoginRequest;
import ru.putevod.app.auth.dto.RefreshTokenRequest;
import ru.putevod.app.auth.dto.RegisterRequest;
import ru.putevod.app.auth.dto.EmailVerificationRequest;
import ru.putevod.app.auth.dto.EmailRequest;
import ru.putevod.app.auth.dto.ResetPasswordRequest;
import ru.putevod.app.auth.dto.VerifyResetCodeRequest;
import ru.putevod.app.auth.dto.TokenValidationRequest;
import ru.putevod.app.auth.dto.TokenValidationResponse;
import ru.putevod.app.auth.service.AuthService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String ipAddress = request.getRemoteAddr();
        String deviceInfo = request.getHeader("User-Agent");

        AuthResponse authResponse = authService.createAuthResponse(
                loginRequest.getEmail(),
                ipAddress,
                deviceInfo,
                loginRequest.getDeviceId()
        );

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshRequest, 
                                                    HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String deviceInfo = request.getHeader("User-Agent");

        AuthResponse authResponse = authService.refreshToken(
                refreshRequest.getRefreshToken(),
                ipAddress,
                deviceInfo
        );

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody RefreshTokenRequest refreshRequest) {
        authService.logout(refreshRequest.getRefreshToken());
        SecurityContextHolder.clearContext();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Вы успешно вышли из системы");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest registerRequest, 
                                                       HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String deviceInfo = request.getHeader("User-Agent");
        
        String userId = authService.registerUser(registerRequest, ipAddress, deviceInfo);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Регистрация успешно завершена. Проверьте почту для подтверждения аккаунта.");
        response.put("userId", userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@Valid @RequestBody EmailVerificationRequest verificationRequest,
                                                    HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String deviceInfo = request.getHeader("User-Agent");
        
        AuthResponse authResponse = authService.verifyEmail(
                verificationRequest.getToken(),
                ipAddress,
                deviceInfo
        );
        
        return ResponseEntity.ok(authResponse);
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@Valid @RequestBody EmailRequest emailRequest) {
        authService.resendVerificationEmail(emailRequest.getEmail());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Новое письмо подтверждения отправлено на ваш email");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody EmailRequest emailRequest) {
        authService.sendPasswordResetEmail(emailRequest.getEmail());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Инструкции по восстановлению пароля отправлены на указанный email");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify-reset-code")
    public ResponseEntity<Map<String, String>> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest resetCodeRequest) {
        String resetToken = authService.verifyPasswordResetCode(resetCodeRequest.getEmail(), resetCodeRequest.getCode());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Код подтверждения действителен");
        response.put("resetToken", resetToken);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        authService.resetPassword(resetPasswordRequest.getResetToken(), resetPasswordRequest.getNewPassword());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Пароль успешно изменен");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/anonymous-token")
    public ResponseEntity<Map<String, Object>> getAnonymousToken(@RequestBody(required = false) Map<String, String> request) {
        String deviceId = request != null ? request.get("deviceId") : null;
        
        Map<String, Object> response = authService.createAnonymousToken(deviceId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Валидирует токен и возвращает информацию о нем
     * 
     * @param tokenRequest запрос с токеном для валидации
     * @param serviceToken токен для межсервисного взаимодействия (опционально)
     * @return информация о валидности токена и данные пользователя
     */
    @PostMapping("/auth/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestBody TokenValidationRequest tokenRequest,
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken) {
        
        TokenValidationResponse response = authService.validateToken(tokenRequest.getToken(), serviceToken);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Получает информацию о пользователе из токена
     * 
     * @param tokenRequest запрос с токеном для получения информации
     * @param serviceToken токен для межсервисного взаимодействия (опционально)
     * @return информация о пользователе
     */
    @PostMapping("/auth/userinfo")
    public ResponseEntity<Map<String, Object>> getUserInfo(
            @RequestBody TokenValidationRequest tokenRequest,
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken) {
        
        Map<String, Object> userInfo = authService.getUserInfoFromToken(tokenRequest.getToken(), serviceToken);
        return ResponseEntity.ok(userInfo);
    }
} 