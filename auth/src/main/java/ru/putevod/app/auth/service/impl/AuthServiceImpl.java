package ru.putevod.app.auth.service.impl;

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
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.model.UserSession;
import ru.putevod.app.auth.repository.UserRepository;
import ru.putevod.app.auth.repository.UserSessionRepository;
import ru.putevod.app.auth.security.JwtTokenProvider;
import ru.putevod.app.auth.service.AuthService;
import ru.putevod.app.auth.service.EmailService;
import ru.putevod.app.auth.service.UserService;

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
                .expiresIn(3600) // 1 час в секундах
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
                .expiresIn(3600) // 1 час в секундах
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
        User user = emailService.verifyEmailToken(token);

        user.setIsVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user, deviceInfo, ipAddress);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600)
                .user(userService.mapToUserInfoDto(user))
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public void sendPasswordResetEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String resetCode = generateRandomCode();
            
            // Сохраняем код в кеше или БД
            // В данном примере предполагается, что это реализовано в emailService
            emailService.storeResetCode(email, resetCode);

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
        
        // выход пользователя из всех сессий
        // sessionRepository.deleteAllByUserId(user.getUserId());
    }
    
    @Override
    public Map<String, Object> createAnonymousToken(String deviceId) {
        String anonymousToken = tokenProvider.generateAnonymousToken(deviceId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("anonymousToken", anonymousToken);
        response.put("expiresIn", 1800);
        
        return response;
    }

    private String generateRandomCode() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }
} 