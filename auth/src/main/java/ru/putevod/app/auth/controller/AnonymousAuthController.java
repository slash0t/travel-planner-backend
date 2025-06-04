package ru.putevod.app.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.auth.service.AnonymousUserService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/anonymous")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Anonymous Authentication", description = "API для анонимной аутентификации")
public class AnonymousAuthController {

    private final AnonymousUserService anonymousUserService;

    @PostMapping("/token")
    @Operation(summary = "Создание анонимного токена", 
               description = "Создает токен для анонимного пользователя по ID устройства")
    @ApiResponse(responseCode = "200", description = "Токен успешно создан")
    public ResponseEntity<Map<String, Object>> createAnonymousToken(
            @Parameter(description = "ID устройства") @RequestParam(required = false) String deviceId,
            HttpServletRequest request) {
        
        // Если deviceId не предоставлен, генерируем новый
        if (deviceId == null || deviceId.trim().isEmpty()) {
            deviceId = UUID.randomUUID().toString();
        }
        
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(request);
        
        log.info("Создание анонимного токена для deviceId: {}, IP: {}", deviceId, ipAddress);
        
        Map<String, Object> response = anonymousUserService.createAnonymousToken(deviceId, userAgent, ipAddress);
        response.put("deviceId", deviceId); // Возвращаем deviceId клиенту
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/activity")
    @Operation(summary = "Обновление активности", 
               description = "Обновляет время последней активности анонимного пользователя")
    @ApiResponse(responseCode = "200", description = "Активность обновлена")
    public ResponseEntity<Void> updateActivity(
            @Parameter(description = "ID устройства") @RequestParam String deviceId) {
        
        anonymousUserService.updateLastActivity(deviceId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Статистика анонимных пользователей", 
               description = "Получение количества активных анонимных пользователей")
    @ApiResponse(responseCode = "200", description = "Статистика получена")
    public ResponseEntity<Map<String, Object>> getAnonymousStats() {
        long activeCount = anonymousUserService.getActiveAnonymousUsersCount();
        
        return ResponseEntity.ok(Map.of(
                "activeAnonymousUsers", activeCount
        ));
    }

    @PostMapping("/recover")
    @Operation(summary = "Восстановление доступа анонимного пользователя", 
               description = "Пытается найти существующего анонимного пользователя по характеристикам устройства")
    @ApiResponse(responseCode = "200", description = "Доступ восстановлен или создан новый пользователь")
    public ResponseEntity<Map<String, Object>> recoverAnonymousAccess(
            @RequestParam(required = false) String userAgent,
            @RequestParam(required = false) String screenResolution,
            HttpServletRequest request) {
        
        String clientUserAgent = userAgent != null ? userAgent : request.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(request);
        
        String deviceId = generateDeviceIdFromCharacteristics(clientUserAgent, screenResolution, ipAddress);
        
        log.info("Попытка восстановления анонимного доступа для deviceId: {}", deviceId);
        
        Map<String, Object> response = anonymousUserService.createAnonymousToken(deviceId, clientUserAgent, ipAddress);
        response.put("deviceId", deviceId);
        response.put("isRecovered", anonymousUserService.findByDeviceId(deviceId).isPresent());
        
        return ResponseEntity.ok(response);
    }

    private String generateDeviceIdFromCharacteristics(String userAgent, String screenResolution, String ipAddress) {
       String characteristics = String.format("%s-%s-%s",
                userAgent != null ? userAgent.hashCode() : "unknown",
                screenResolution != null ? screenResolution : "unknown",
                ipAddress != null ? ipAddress.hashCode() : "unknown");
        
        return "recovered-" + Math.abs(characteristics.hashCode());
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 