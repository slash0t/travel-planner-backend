package ru.putevod.app.planner.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("SELECT t FROM TodoList t WHERE t.user = :user")
    Page<TodoList> findAllActiveByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT t FROM TodoList t WHERE t.user = :user")
    Page<TodoList> findAllByUserSimple(@Param("user") User user, Pageable pageable);

    Page<TodoList> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Optional<TodoList> findByUserAndListId(User user, Long listId);

    Optional<TodoList> findByTripAndListId(Trip trip, Long listId);

    /**
     * Находит все TODO листы анонимного пользователя
     *
     * @param anonymousUserId ID анонимного пользователя
     * @return список TODO листов
     */
    @Query("SELECT t FROM TodoList t WHERE t.anonymousUserId = :anonymousUserId")
    List<TodoList> findByAnonymousUserId(@Param("anonymousUserId") Long anonymousUserId);

    /**
     * Переносит владение TODO листами от анонимного пользователя к зарегистрированному
     *
     * @param anonymousUserId ID анонимного пользователя
     * @param newOwnerId ID нового владельца
     * @return количество обновленных записей
     */
    @Modifying
    @Query("UPDATE TodoList t SET t.user.userId = :newOwnerId, t.anonymousUserId = null, t.updatedAt = CURRENT_TIMESTAMP WHERE t.anonymousUserId = :anonymousUserId")
    int transferTodoListOwnership(@Param("anonymousUserId") Long anonymousUserId, @Param("newOwnerId") Long newOwnerId);
} 