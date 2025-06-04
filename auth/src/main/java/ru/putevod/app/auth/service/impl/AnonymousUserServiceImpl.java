package ru.putevod.app.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.auth.config.AppProperties;
import ru.putevod.app.auth.model.AnonymousUser;
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.repository.AnonymousUserRepository;
import ru.putevod.app.auth.security.JwtTokenProvider;
import ru.putevod.app.auth.service.AnonymousUserService;
import ru.putevod.app.auth.service.DataMigrationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnonymousUserServiceImpl implements AnonymousUserService {

    private final AnonymousUserRepository anonymousUserRepository;
    private final JwtTokenProvider tokenProvider;
    private final AppProperties appProperties;
    private final DataMigrationService dataMigrationService;

    @Override
    @Transactional
    public AnonymousUser createOrGetAnonymousUser(String deviceId, String deviceInfo, String ipAddress) {
        Optional<AnonymousUser> existingUser = anonymousUserRepository.findByDeviceId(deviceId);

        if (existingUser.isPresent()) {
            AnonymousUser user = existingUser.get();
            user.setLastActivity(LocalDateTime.now());
            if (deviceInfo != null) {
                user.setDeviceInfo(deviceInfo);
            }
            if (ipAddress != null) {
                user.setIpAddress(ipAddress);
            }
            return anonymousUserRepository.save(user);
        }

        AnonymousUser newUser = AnonymousUser.builder()
                .deviceId(deviceId)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();

        log.info("Создан новый анонимный пользователь с device ID: {}", deviceId);
        return anonymousUserRepository.save(newUser);
    }

    @Override
    public Optional<AnonymousUser> findByDeviceId(String deviceId) {
        return anonymousUserRepository.findByDeviceId(deviceId);
    }

    @Override
    @Transactional
    public Map<String, Object> createAnonymousToken(String deviceId, String deviceInfo, String ipAddress) {
        AnonymousUser anonymousUser = createOrGetAnonymousUser(deviceId, deviceInfo, ipAddress);

        String anonymousToken = tokenProvider.generateAnonymousToken(deviceId, anonymousUser.getAnonymousUserId());

        Map<String, Object> response = new HashMap<>();
        response.put("anonymousToken", anonymousToken);
        response.put("expiresIn", (int) (appProperties.getJwt().getAnonymousTokenExpirationMs() / 1000));
        response.put("anonymousUserId", anonymousUser.getAnonymousUserId());

        return response;
    }

    @Override
    @Transactional
    public void updateLastActivity(String deviceId) {
        anonymousUserRepository.findByDeviceId(deviceId)
                .ifPresent(user -> {
                    user.setLastActivity(LocalDateTime.now());
                    anonymousUserRepository.save(user);
                });
    }

    @Override
    @Transactional
    public void migrateAnonymousUserToRegistered(String deviceId, User registeredUser) {
        Optional<AnonymousUser> anonymousUserOpt = anonymousUserRepository.findByDeviceId(deviceId);

        if (anonymousUserOpt.isPresent()) {
            AnonymousUser anonymousUser = anonymousUserOpt.get();

            // Отмечаем анонимного пользователя как мигрированного
            anonymousUser.setMigrated(true);
            anonymousUser.setMigratedToUserId(registeredUser.getUserId().longValue());
            anonymousUser.setMigrationDate(LocalDateTime.now());
            anonymousUserRepository.save(anonymousUser);

            // Обновляем информацию о миграции в модели User
            registeredUser.setMigratedFromAnonymousId(anonymousUser.getAnonymousUserId());

            log.info("Анонимный пользователь {} мигрирован к зарегистрированному пользователю {}",
                    deviceId, registeredUser.getUserId());

            // Вызываем сервис для миграции данных (путешествий, todo листов)
            try {
                dataMigrationService.migrateAnonymousUserData(anonymousUser.getAnonymousUserId(), registeredUser.getUserId());
                log.info("Данные анонимного пользователя {} успешно мигрированы к пользователю {}",
                        anonymousUser.getAnonymousUserId(), registeredUser.getUserId());
            } catch (Exception e) {
                log.error("Ошибка при миграции данных анонимного пользователя {} к пользователю {}: {}",
                        anonymousUser.getAnonymousUserId(), registeredUser.getUserId(), e.getMessage(), e);
                // Не прерываем процесс регистрации из-за ошибки миграции данных
                // Пользователь все равно будет успешно зарегистрирован
            }
        }
    }

    @Override
    @Transactional
    public void cleanupInactiveAnonymousUsers() {
        // Удаляем анонимных пользователей неактивных более 30 дней
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<AnonymousUser> inactiveUsers = anonymousUserRepository.findInactiveAnonymousUsers(cutoffDate);

        for (AnonymousUser user : inactiveUsers) {
            log.info("Удаляем неактивного анонимного пользователя: {}", user.getDeviceId());
            anonymousUserRepository.delete(user);
        }

        log.info("Удалено {} неактивных анонимных пользователей", inactiveUsers.size());
    }

    @Override
    public long getActiveAnonymousUsersCount() {
        return anonymousUserRepository.countActiveAnonymousUsers();
    }
} 