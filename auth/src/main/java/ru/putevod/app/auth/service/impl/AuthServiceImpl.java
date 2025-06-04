package ru.putevod.app.auth.service.impl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.putevod.app.auth.dto.AuthResponse;
import ru.putevod.app.auth.dto.RegisterRequest;
import ru.putevod.app.auth.dto.TokenValidationResponse;
import ru.putevod.app.auth.dto.UpdateProfileRequest;
import ru.putevod.app.auth.dto.UserInfoDto;
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.model.UserSession;
import ru.putevod.app.auth.repository.UserRepository;
import ru.putevod.app.auth.repository.UserSessionRepository;
import ru.putevod.app.auth.security.JwtTokenProvider;
import ru.putevod.app.auth.service.AuthService;
import ru.putevod.app.auth.service.EmailService;
import ru.putevod.app.auth.service.UserService;
import ru.putevod.app.auth.config.AppProperties;
import ru.putevod.app.auth.service.AnonymousUserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AppProperties appProperties;
    private final AnonymousUserService anonymousUserService;

    @Override
    @Transactional
    public AuthResponse createAuthResponse(String email, String ipAddress, String deviceInfo, String deviceId) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Пользователь с email " + email + " не найден"));

        user = userService.updateLastLogin(user);

        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user, deviceInfo, ipAddress);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn((int) (appProperties.getJwt().getAccessTokenExpirationMs() / 1000)) // Конвертируем мс в секунды
                .user(userService.mapToUserInfoDto(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken, String ipAddress, String deviceInfo) {
        UserSession session = sessionRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Недействительный refresh токен"));

        User user = session.getUser();

        sessionRepository.deleteByToken(refreshToken);

        String accessToken = tokenProvider.generateAccessToken(user);
        String newRefreshToken = tokenProvider.generateRefreshToken(user, deviceInfo, ipAddress);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .expiresIn((int) (appProperties.getJwt().getAccessTokenExpirationMs() / 1000)) // Конвертируем мс в секунды
                .user(userService.mapToUserInfoDto(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        sessionRepository.findByToken(refreshToken)
                .ifPresent(session -> sessionRepository.deleteByToken(refreshToken));
    }

    @Override
    @Transactional
    public String registerUser(RegisterRequest registerRequest, String ipAddress, String deviceInfo) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь с таким email уже существует");
        }

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь с таким username уже существует");
        }

        User newUser = User.builder()
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isVerified(false)
                .build();

        User savedUser = userRepository.save(newUser);

        String verificationToken = UUID.randomUUID().toString();
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getUsername(), verificationToken);

        return savedUser.getUserId().toString();
    }

    @Override
    @Transactional
    public AuthResponse verifyEmail(String token, String ipAddress, String deviceInfo) {
        return verifyEmail(token, ipAddress, deviceInfo, null);
    }

    @Override
    @Transactional
    public AuthResponse verifyEmail(String token, String ipAddress, String deviceInfo, String deviceId) {
        User user = emailService.verifyEmailToken(token);

        user.setIsVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Если предоставлен deviceId, выполняем миграцию анонимного пользователя
        if (deviceId != null && !deviceId.trim().isEmpty()) {
            try {
                anonymousUserService.migrateAnonymousUserToRegistered(deviceId, user);
                log.info("Успешно выполнена миграция анонимного пользователя с deviceId {} к пользователю {}", 
                        deviceId, user.getUserId());
            } catch (Exception e) {
                log.warn("Ошибка при миграции анонимного пользователя с deviceId {} к пользователю {}: {}", 
                        deviceId, user.getUserId(), e.getMessage());
                // Не прерываем процесс верификации из-за ошибки миграции
            }
        }

        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user, deviceInfo, ipAddress);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn((int) (appProperties.getJwt().getAccessTokenExpirationMs() / 1000))
                .user(userService.mapToUserInfoDto(user))
                .build();
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email уже подтвержден");
        }

        String verificationToken = UUID.randomUUID().toString();
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationToken);
    }

    @Override
    @Transactional
    public void sendPasswordResetEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String resetCode = generateRandomCode();

            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetCode);
        } else {
            log.info("Попытка сброса пароля для несуществующего email: {}", email);
        }
    }

    @Override
    @Transactional
    public String verifyPasswordResetCode(String email, String code) {
        boolean isValid = emailService.verifyResetCode(email, code);

        if (!isValid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный или истекший код");
        }

        String resetToken = UUID.randomUUID().toString();

        emailService.storeResetToken(email, resetToken);

        return resetToken;
    }

    @Override
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        String email = emailService.getEmailByResetToken(resetToken);

        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный или истекший токен");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        emailService.invalidateResetToken(resetToken);
    }

    @Override
    public Map<String, Object> createAnonymousToken(String deviceId) {
        return anonymousUserService.createAnonymousToken(deviceId, null, null);
    }

    public Map<String, Object> createAnonymousToken(String deviceId, String deviceInfo, String ipAddress) {
        return anonymousUserService.createAnonymousToken(deviceId, deviceInfo, ipAddress);
    }

    private String generateRandomCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    @Override
    public TokenValidationResponse validateToken(String token, String serviceToken) {
        if (!tokenProvider.validateServiceToken(serviceToken)) {
            log.warn("Попытка валидации с неверным сервисным токеном");
            return TokenValidationResponse.builder()
                    .valid(false)
                    .errorMessage("Отказано в доступе")
                    .errorType("AccessDenied")
                    .build();
        }

        try {
            if (!tokenProvider.validateToken(token)) {
                return TokenValidationResponse.builder().valid(false).build();
            }

            if (tokenProvider.isAnonymousToken(token)) {
                String deviceId = tokenProvider.getDeviceIdFromToken(token);
                Long anonymousUserId = tokenProvider.getAnonymousUserIdFromToken(token);
                
                if (deviceId != null) {
                    anonymousUserService.updateLastActivity(deviceId);
                }
                
                return TokenValidationResponse.builder()
                        .valid(true)
                        .anonymousUserId(anonymousUserId)
                        .deviceId(deviceId)
                        .isAnonymous(true)
                        .build();
            }

            String email = tokenProvider.getEmailFromToken(token);
            Long userId = tokenProvider.getUserIdFromToken(token);

            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("Токен содержит email несуществующего пользователя: {}", email);
                return TokenValidationResponse.builder().valid(false).build();
            }

            User user = userOpt.get();
            return TokenValidationResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .email(email)
                    .username(user.getUsername())
                    .admin(user.getIsAdmin() != null ? user.getIsAdmin() : false)
                    .build();

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT токен истек: {}", e.getMessage());
            return TokenValidationResponse.builder()
                    .valid(false)
                    .errorMessage("Срок действия токена истек")
                    .errorType("ExpiredJwtException")
                    .build();
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("Неверная подпись JWT: {}", e.getMessage());
            return TokenValidationResponse.builder()
                    .valid(false)
                    .errorMessage("Неверная подпись токена")
                    .errorType("SignatureException")
                    .build();
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("Неверный формат JWT: {}", e.getMessage());
            return TokenValidationResponse.builder()
                    .valid(false)
                    .errorMessage("Неверный формат токена")
                    .errorType("MalformedJwtException")
                    .build();
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.warn("JWT токен не поддерживается: {}", e.getMessage());
            return TokenValidationResponse.builder()
                    .valid(false)
                    .errorMessage("Неподдерживаемый токен")
                    .errorType("UnsupportedJwtException")
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("JWT неверные аргументы: {}", e.getMessage());
            return TokenValidationResponse.builder()
                    .valid(false)
                    .errorMessage("Недопустимые аргументы JWT")
                    .errorType("IllegalArgumentException")
                    .build();
        } catch (Exception e) {
            log.error("Ошибка при валидации токена: {}", e.getMessage());
            return TokenValidationResponse.builder()
                    .valid(false)
                    .errorMessage("Ошибка обработки токена")
                    .errorType("Exception")
                    .build();
        }
    }

    @Override
    public Map<String, Object> getUserInfoFromToken(String token, String serviceToken) {
        if (!tokenProvider.validateServiceToken(serviceToken)) {
            log.warn("Попытка получения информации с неверным сервисным токеном");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный сервисный токен");
        }

        try {
            if (!tokenProvider.validateToken(token)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный токен пользователя");
            }

            if (tokenProvider.isAnonymousToken(token)) {
                String deviceId = tokenProvider.getDeviceIdFromToken(token);
                Long anonymousUserId = tokenProvider.getAnonymousUserIdFromToken(token);
                
                Map<String, Object> info = new HashMap<>();
                info.put("isAnonymous", true);
                info.put("anonymousUserId", anonymousUserId);
                info.put("deviceId", deviceId);
                info.put("roles", new String[]{"ROLE_ANONYMOUS"});
                
                // Обновляем активность
                if (deviceId != null) {
                    anonymousUserService.updateLastActivity(deviceId);
                }
                
                return info;
            }

            String email = tokenProvider.getEmailFromToken(token);
            Long userId = tokenProvider.getUserIdFromToken(token);

            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден");
            }

            User user = userOpt.get();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", userId);
            userInfo.put("email", email);
            userInfo.put("username", user.getUsername());
            userInfo.put("isAdmin", user.getIsAdmin() != null ? user.getIsAdmin() : false);
                userInfo.put("verified", user.getIsVerified());
                userInfo.put("roles", user.getIsAdmin() ? new String[]{"ROLE_USER", "ROLE_ADMIN"} : new String[]{"ROLE_USER"});

            return userInfo;

        } catch (ExpiredJwtException e) {
            log.warn("JWT токен истек: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Срок действия токена истек");
        } catch (SignatureException e) {
            log.warn("Неверная подпись JWT: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверная подпись токена");
        } catch (MalformedJwtException e) {
            log.warn("Неверный формат JWT: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный формат токена");
        } catch (UnsupportedJwtException e) {
            log.warn("JWT токен не поддерживается: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неподдерживаемый токен");
        } catch (IllegalArgumentException e) {
            log.warn("JWT неверные аргументы: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Недопустимые аргументы JWT");
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при получении информации из токена: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка обработки токена");
        }
    }

    @Override
    public UserInfoDto getUserById(Integer userId, String serviceToken) {
        if (!tokenProvider.validateServiceToken(serviceToken)) {
            log.warn("Попытка получения пользователя с неверным сервисным токеном");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный сервисный токен");
        }

        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        return UserInfoDto.builder()
                .id(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .avatarUrl(user.getProfilePictureUrl())
                .emailVerified(user.getIsVerified() != null ? user.getIsVerified() : false)
                .isAdmin(user.getIsAdmin() != null ? user.getIsAdmin() : false)
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public UserInfoDto updateUserProfile(Integer userId, UpdateProfileRequest updateProfileRequest) {
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        if (updateProfileRequest.getEmail() != null &&
            !updateProfileRequest.getEmail().equals(user.getEmail())) {
            
            if (userRepository.existsByEmail(updateProfileRequest.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Пользователь с таким email уже существует");
            }
            user.setEmail(updateProfileRequest.getEmail());
        }

        if (updateProfileRequest.getUsername() != null &&
            !updateProfileRequest.getUsername().equals(user.getUsername())) {
            
            if (userRepository.existsByUsername(updateProfileRequest.getUsername())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Пользователь с таким именем уже существует");
            }
            user.setUsername(updateProfileRequest.getUsername());
        }

        if (updateProfileRequest.getAvatarUrl() != null) {
            user.setProfilePictureUrl(updateProfileRequest.getAvatarUrl());
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        log.info("Профиль пользователя {} успешно обновлен", userId);

        return UserInfoDto.builder()
                .id(updatedUser.getUserId())
                .email(updatedUser.getEmail())
                .username(updatedUser.getUsername())
                .avatarUrl(updatedUser.getProfilePictureUrl())
                .emailVerified(updatedUser.getIsVerified() != null ? updatedUser.getIsVerified() : false)
                .isAdmin(updatedUser.getIsAdmin() != null ? updatedUser.getIsAdmin() : false)
                .createdAt(updatedUser.getCreatedAt())
                .build();
    }
} 