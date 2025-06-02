package ru.putevod.app.planner.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.putevod.app.planner.model.TodoList;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoListRepository extends JpaRepository<TodoList, Long> {

    Page<TodoList> findByUser(User user, Pageable pageable);

    List<TodoList> findByTrip(Trip trip);

    @Query("SELECT t FROM TodoList t WHERE t.user = :user AND (t.trip IS NULL OR t.trip.isDeleted = false)")
    Page<TodoList> findAllActiveByUser(@Param("user") User user, Pageable pageable);

    Optional<TodoList> findByUserAndListId(User user, Long listId);

    Optional<TodoList> findByTripAndListId(Trip trip, Long listId);
} 