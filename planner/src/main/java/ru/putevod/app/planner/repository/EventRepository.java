package ru.putevod.app.planner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.putevod.app.planner.model.Event;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.TripDay;
import ru.putevod.app.planner.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByDayOrderByOrderPositionAsc(TripDay day);

    /**
     * Получить все события дня, отсортированные по orderPosition
     * (сквозная нумерация для всех типов событий)
     */
    @Query("SELECT e FROM Event e WHERE e.day = :day ORDER BY e.orderPosition ASC")
    List<Event> findByDayOrderByTimeAndPosition(@Param("day") TripDay day);

    /**
     * Получить только события со временем для дня, отсортированные по времени
     */
    @Query("SELECT e FROM Event e WHERE e.day = :day AND e.hasSpecificTime = true AND e.startTime IS NOT NULL ORDER BY e.startTime ASC")
    List<Event> findTimedEventsByDay(@Param("day") TripDay day);

    /**
     * Получить только события без времени для дня, отсортированные по orderPosition
     */
    @Query("SELECT e FROM Event e WHERE e.day = :day AND (e.hasSpecificTime = false OR e.startTime IS NULL) ORDER BY e.orderPosition ASC")
    List<Event> findUntimedEventsByDay(@Param("day") TripDay day);

    @Query("SELECT e FROM Event e WHERE e.day.trip = :trip ORDER BY e.day.dayNumber ASC, e.orderPosition ASC")
    List<Event> findAllByTrip(@Param("trip") Trip trip);

    @Query("SELECT e FROM Event e JOIN e.day d WHERE d.trip.creator = :user AND e.hasSpecificTime = true AND FUNCTION('DATE_ADD', CURRENT_DATE(), :days, 'DAY') = d.date")
    List<Event> findUpcomingEventsForUser(@Param("user") User user, @Param("days") int days);

    @Query("SELECT e FROM Event e JOIN e.reminders r WHERE r.remindAt BETWEEN :startTime AND :endTime AND r.sent = false")
    List<Event> findEventsWithRemindersInTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * Подсчитывает количество событий во всех путешествиях пользователя
     * События связаны с днями поездок, которые связаны с путешествиями
     *
     * @param user пользователь
     * @return количество событий
     */
    @Query("SELECT COUNT(e) FROM Event e " +
            "JOIN e.day d " +
            "JOIN d.trip t " +
            "WHERE (t.creator = :user OR EXISTS (" +
            "    SELECT a FROM TripAccess a WHERE a.trip = t AND a.user = :user AND a.invitationStatus = 'accepted'" +
            ")) AND t.isDeleted = false")
    Long countUserEvents(@Param("user") User user);
} 