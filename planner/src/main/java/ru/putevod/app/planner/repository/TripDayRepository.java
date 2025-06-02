package ru.putevod.app.planner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.TripDay;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripDayRepository extends JpaRepository<TripDay, Long> {

    List<TripDay> findByTripOrderByDayNumberAsc(Trip trip);

    Optional<TripDay> findByTripAndDayId(Trip trip, Long dayId);

    @Query("SELECT d FROM TripDay d WHERE d.trip = :trip AND d.date = :date")
    Optional<TripDay> findByTripAndDate(@Param("trip") Trip trip, @Param("date") LocalDate date);

    void deleteByTripAndDayId(Trip trip, Long dayId);
} 