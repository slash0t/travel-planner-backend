package ru.putevod.app.planner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.putevod.app.planner.model.Place;
import ru.putevod.app.planner.model.User;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    /**
     * Подсчитывает количество уникальных мест во всех путешествиях пользователя
     * Места связаны с событиями, которые связаны с днями поездок
     *
     * @param user пользователь
     * @return количество уникальных мест
     */
    @Query("SELECT COUNT(DISTINCT p) FROM Place p " +
           "JOIN p.events e " +
           "JOIN e.day d " +
           "JOIN d.trip t " +
           "WHERE (t.creator = :user OR EXISTS (" +
           "    SELECT a FROM TripAccess a WHERE a.trip = t AND a.user = :user AND a.invitationStatus = 'accepted'" +
           ")) AND t.isDeleted = false")
    Long countUserPlaces(@Param("user") User user);
} 