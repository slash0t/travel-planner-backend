package ru.putevod.app.planner.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.User;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    @Query("SELECT t FROM Trip t WHERE t.creator = :user AND t.isDeleted = false")
    Page<Trip> findAllByCreator(@Param("user") User user, Pageable pageable);

    @Query("SELECT t FROM Trip t JOIN t.accesses a WHERE a.user = :user AND t.isDeleted = false")
    Page<Trip> findAllSharedWithUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT t FROM Trip t WHERE t.creator = :user AND t.startDate >= :today AND t.isDeleted = false ORDER BY t.startDate ASC")
    List<Trip> findUpcomingTrips(@Param("user") User user, @Param("today") LocalDate today);

    @Query("SELECT t FROM Trip t WHERE t.creator = :user AND t.startDate <= :today AND t.endDate >= :today AND t.isDeleted = false")
    List<Trip> findOngoingTrips(@Param("user") User user, @Param("today") LocalDate today);

    @Query("SELECT t FROM Trip t WHERE t.creator = :user AND t.endDate < :today AND t.isDeleted = false ORDER BY t.endDate DESC")
    List<Trip> findPastTrips(@Param("user") User user, @Param("today") LocalDate today);

    @Query("SELECT t FROM Trip t WHERE (t.creator = :user OR EXISTS (SELECT a FROM TripAccess a WHERE a.trip = t AND a.user = :user)) AND t.isDeleted = false")
    Page<Trip> findAllAvailableToUser(@Param("user") User user, Pageable pageable);

    /**
     * Подсчитывает общее количество путешествий пользователя (включая созданные им и доступные через доступ)
     *
     * @param user пользователь
     * @return количество путешествий
     */
    @Query("SELECT COUNT(DISTINCT t) FROM Trip t WHERE (t.creator = :user OR EXISTS (SELECT a FROM TripAccess a WHERE a.trip = t AND a.user = :user AND a.invitationStatus = 'accepted')) AND t.isDeleted = false")
    Long countAllUserTrips(@Param("user") User user);

    /**
     * Находит все путешествия анонимного пользователя
     *
     * @param anonymousUserId ID анонимного пользователя
     * @return список путешествий
     */
    @Query("SELECT t FROM Trip t WHERE t.anonymousCreatorId = :anonymousUserId AND t.isDeleted = false")
    List<Trip> findByAnonymousCreatorId(@Param("anonymousUserId") Long anonymousUserId);

    /**
     * Переносит владение путешествиями от анонимного пользователя к зарегистрированному
     *
     * @param anonymousUserId ID анонимного пользователя
     * @param newOwnerId      ID нового владельца
     * @return количество обновленных записей
     */
    @Modifying
    @Query("UPDATE Trip t SET t.creator.userId = :newOwnerId, t.anonymousCreatorId = null, t.updatedAt = CURRENT_TIMESTAMP WHERE t.anonymousCreatorId = :anonymousUserId AND t.isDeleted = false")
    int transferTripOwnership(@Param("anonymousUserId") Long anonymousUserId, @Param("newOwnerId") Long newOwnerId);
} 