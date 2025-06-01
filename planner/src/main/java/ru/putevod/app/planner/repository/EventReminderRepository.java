package ru.putevod.app.planner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.putevod.app.planner.model.Event;
import ru.putevod.app.planner.model.EventReminder;
import ru.putevod.app.planner.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventReminderRepository extends JpaRepository<EventReminder, Long> {

    List<EventReminder> findByEvent(Event event);

    List<EventReminder> findByEventAndUser(Event event, User user);

    @Query("SELECT r FROM EventReminder r WHERE r.remindAt BETWEEN :start AND :end AND r.sent = false")
    List<EventReminder> findDueReminders(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT r FROM EventReminder r WHERE r.remindAt BETWEEN :start AND :end AND r.sent = false")
    List<EventReminder> findUpcomingReminders(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Modifying
    @Query("UPDATE EventReminder r SET r.sent = true WHERE r.reminderId IN :ids")
    void markAsSent(@Param("ids") List<Long> reminderIds);

    void deleteByEvent(Event event);

    void deleteByEventAndUser(Event event, User user);
} 