package ru.putevod.app.planner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.TripAccess;
import ru.putevod.app.planner.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripAccessRepository extends JpaRepository<TripAccess, Long> {

    List<TripAccess> findByTrip(Trip trip);

    Optional<TripAccess> findByTripAndUser(Trip trip, User user);

    @Query("SELECT a FROM TripAccess a WHERE a.user = :user AND a.invitationStatus = 'pending'")
    List<TripAccess> findPendingInvitationsForUser(@Param("user") User user);

    @Modifying
    @Query("UPDATE TripAccess a SET a.invitationStatus = :status WHERE a.trip = :trip AND a.user = :user")
    int updateInvitationStatus(@Param("trip") Trip trip, @Param("user") User user, @Param("status") String status);

    boolean existsByTripAndUser(Trip trip, User user);

    void deleteByTripAndUser(Trip trip, User user);
} 