package ru.putevod.app.planner.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.putevod.app.planner.service.EventService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {
    
    private final EventService eventService;
    
    @Value("${app.notifications.reminder-check-interval:60000}")
    private long reminderCheckInterval;
    
    @Scheduled(fixedDelayString = "${app.notifications.reminder-check-interval:60000}")
    public void processReminders() {
        log.debug("Starting scheduled reminder processing");
        try {
            eventService.processReminders();
        } catch (Exception e) {
            log.error("Error during processing reminders", e);
        }
        log.debug("Finished scheduled reminder processing");
    }
} 