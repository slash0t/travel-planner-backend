package ru.putevod.app.auth.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.putevod.app.auth.service.AnonymousUserService;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnonymousUserCleanupScheduler {

    private final AnonymousUserService anonymousUserService;

    /**
     * Запускается каждый день в 2:00 утра для очистки неактивных анонимных пользователей
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupInactiveAnonymousUsers() {
        log.info("Запуск планового задания очистки неактивных анонимных пользователей");
        
        try {
            anonymousUserService.cleanupInactiveAnonymousUsers();
            log.info("Плановое задание очистки анонимных пользователей завершено успешно");
        } catch (Exception e) {
            log.error("Ошибка при выполнении планового задания очистки анонимных пользователей", e);
        }
    }

    /**
     * Запускается каждый час для логирования статистики
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void logAnonymousUserStats() {
        try {
            long activeCount = anonymousUserService.getActiveAnonymousUsersCount();
            log.info("Текущее количество активных анонимных пользователей: {}", activeCount);
        } catch (Exception e) {
            log.warn("Ошибка при получении статистики анонимных пользователей", e);
        }
    }
} 