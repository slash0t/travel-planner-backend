package ru.putevod.app.planner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.putevod.app.planner.model.File;
import ru.putevod.app.planner.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByUser(User user);

    Optional<File> findByFileIdAndUser(Long fileId, User user);

    @Query("SELECT f FROM File f JOIN f.tripFiles tf WHERE tf.trip.tripId = :tripId")
    List<File> findByTripId(@Param("tripId") Long tripId);

    @Query("SELECT f FROM File f JOIN f.eventFiles ef WHERE ef.event.eventId = :eventId")
    List<File> findByEventId(@Param("eventId") Long eventId);

    boolean existsByFileIdAndUser(Long fileId, User user);
} 