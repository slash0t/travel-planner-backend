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

    @Query("SELECT e FROM Event e WHERE e.day.trip = :trip ORDER BY e.day.dayNumber ASC, e.orderPosition ASC")
    List<Event> findAllByTrip(@Param("trip") Trip trip);

    @Query("SELECT e FROM Event e JOIN e.day d WHERE d.trip.creator = :user AND e.hasSpecificTime = true AND FUNCTION('DATE_ADD', CURRENT_DATE(), :days, 'DAY') = d.date")
    List<Event> findUpcomingEventsForUser(@Param("user") User user, @Param("days") int days);

    @Query("SELECT e FROM Event e JOIN e.reminders r WHERE r.remindAt BETWEEN :startTime AND :endTime AND r.sent = false")
    List<Event> findEventsWithRemindersInTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
} 