package ru.putevod.app.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.auth.dto.*;
import ru.putevod.app.auth.service.AuthService;
import ru.putevod.app.auth.config.CurrentUser;
import ru.putevod.app.auth.annotation.TrackMetrics;
import ru.putevod.app.auth.service.MetricsService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Аутентификация", description = "API для регистрации, авторизации и управления аккаунтом")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final MetricsService metricsService;

    @Operation(
            summary = "Авторизация пользователя",
            description = "Аутентифицирует пользователя по email и паролю и возвращает JWT токены"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная авторизация",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос")
    })
    @PostMapping("/login")
    @TrackMetrics(type = TrackMetrics.Type.AUTH, eventName = "login")
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

    @Operation(
            summary = "Обновление токена",
            description = "Обновляет JWT access token с помощью refresh token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Токен успешно обновлен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Невалидный refresh token"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос")
    })
    @PostMapping("/refresh")
    @TrackMetrics(type = TrackMetrics.Type.AUTH, eventName = "refresh_token")
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

    @Operation(
            summary = "Выход из системы",
            description = "Выполняет выход пользователя из системы и инвалидирует refresh token",
    security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный выход из системы"
            ),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PostMapping("/logout")
    @TrackMetrics(type = TrackMetrics.Type.AUTH, eventName = "logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody RefreshTokenRequest refreshRequest) {
        authService.logout(refreshRequest.getRefreshToken());
        SecurityContextHolder.clearContext();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Вы успешно вышли из системы");

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Регистрирует нового пользователя в системе и отправляет email для подтверждения"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно зарегистрирован"
            ),
            @ApiResponse(responseCode = "400", description = "Неверные данные регистрации"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует")
    })
    @PostMapping("/register")
    @TrackMetrics(type = TrackMetrics.Type.AUTH, eventName = "register")
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

    @Operation(
            summary = "Подтверждение email",
            description = "Подтверждает email пользователя по токену из письма"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email успешно подтвержден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Неверный или истекший токен")
    })
    @PostMapping("/verify-email")
    @TrackMetrics(type = TrackMetrics.Type.AUTH, eventName = "verify_email")
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

    @Operation(
            summary = "Подтверждение email с миграцией анонимного пользователя",
            description = "Подтверждает email пользователя по токену из письма и мигрирует данные анонимного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email успешно подтвержден и данные мигрированы",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Неверный или истекший токен")
    })
    @PostMapping("/verify-email-with-migration")
    @TrackMetrics(type = TrackMetrics.Type.AUTH, eventName = "verify_email_migration")
    public ResponseEntity<AuthResponse> verifyEmailWithMigration(
            @Valid @RequestBody EmailVerificationWithMigrationRequest verificationRequest,
            HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String deviceInfo = request.getHeader("User-Agent");

        AuthResponse authResponse = authService.verifyEmail(
                verificationRequest.getToken(),
                ipAddress,
                deviceInfo,
                verificationRequest.getDeviceId()
        );

        return ResponseEntity.ok(authResponse);
    }

    @Operation(
            summary = "Повторная отправка письма подтверждения",
            description = "Повторно отправляет письмо для подтверждения email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Письмо успешно отправлено"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PostMapping("/resend-verification")
    @TrackMetrics(type = TrackMetrics.Type.AUTH, eventName = "resend_verification")
    public ResponseEntity<Map<String, String>> resendVerification(@Valid @RequestBody EmailRequest emailRequest) {
        authService.resendVerificationEmail(emailRequest.getEmail());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Новое письмо подтверждения отправлено на ваш email");

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Запрос на восстановление пароля",
            description = "Отправляет письмо с инструкциями по восстановлению пароля"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Письмо успешно отправлено"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PostMapping("/forgot-password")
    @TrackMetrics(type = TrackMetrics.Type.AUTH, eventName = "forgot_password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody EmailRequest emailRequest) {
        authService.sendPasswordResetEmail(emailRequest.getEmail());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Инструкции по восстановлению пароля отправлены на указанный email");

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Проверка кода восстановления пароля",
            description = "Проверяет код для восстановления пароля и возвращает токен сброса"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Код валидный, токен сброса создан"),
            @ApiResponse(responseCode = "400", description = "Неверный код или email")
    })
    @PostMapping("/verify-reset-code")
    @TrackMetrics(type = TrackMetrics.Type.AUTH, eventName = "verify_reset_code")
    public ResponseEntity<Map<String, String>> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest resetCodeRequest) {
        String resetToken = authService.verifyPasswordResetCode(resetCodeRequest.getEmail(), resetCodeRequest.getCode());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Код подтверждения действителен");
        response.put("resetToken", resetToken);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Восстановление пароля",
            description = "Устанавливает новый пароль пользователя по токену сброса"
    )

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пароль успешно изменен"),
            @ApiResponse(responseCode = "400", description = "Неверный или истекший токен")
    })
    @PostMapping("/reset-password")
    @TrackMetrics(type = TrackMetrics.Type.AUTH, eventName = "reset_password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        authService.resetPassword(resetPasswordRequest.getResetToken(), resetPasswordRequest.getNewPassword());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Пароль успешно изменен");

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Получение анонимного токена",
            description = "Создает анонимный токен для неавторизованных пользователей"
    )

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токен успешно создан")
    })
    @PostMapping("/anonymous-token")
    @TrackMetrics(type = TrackMetrics.Type.AUTH, eventName = "anonymous_token")
    public ResponseEntity<Map<String, Object>> getAnonymousToken(@RequestBody(required = false) Map<String, String> request) {
        String deviceId = request != null ? request.get("deviceId") : null;

        Map<String, Object> response = authService.createAnonymousToken(deviceId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestBody TokenValidationRequest tokenRequest,
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken) {

        TokenValidationResponse response = authService.validateToken(tokenRequest.getToken(), serviceToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(
            @RequestBody TokenValidationRequest tokenRequest,
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken) {
        
        Map<String, Object> userInfo = authService.getUserInfoFromToken(tokenRequest.getToken(), serviceToken);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserInfoDto> getUserById(
            @PathVariable Integer userId,
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken) {
        
        UserInfoDto userInfo = authService.getUserById(userId, serviceToken);
        return ResponseEntity.ok(userInfo);
    }

    @Operation(
            summary = "Обновить профиль текущего пользователя",
            description = "Обновляет профиль текущего авторизованного пользователя",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Профиль успешно обновлен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInfoDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Неверные данные профиля"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "409", description = "Email или username уже используются")
    })
    @PutMapping("/profile")
    @TrackMetrics(type = TrackMetrics.Type.AUTH, eventName = "update_profile")
    public ResponseEntity<UserInfoDto> updateProfile(
            @CurrentUser Integer userId,
            @Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserInfoDto updatedUser = authService.updateUserProfile(userId, updateProfileRequest);
        return ResponseEntity.ok(updatedUser);
    }
} 