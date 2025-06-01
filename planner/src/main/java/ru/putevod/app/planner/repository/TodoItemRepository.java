package ru.putevod.app.planner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.putevod.app.planner.model.TodoItem;
import ru.putevod.app.planner.model.TodoList;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {

    List<TodoItem> findByTodoListOrderByOrderPositionAsc(TodoList todoList);

    Optional<TodoItem> findByTodoListAndItemId(TodoList todoList, Long itemId);

    @Modifying
    @Query("UPDATE TodoItem i SET i.completed = :completed WHERE i.itemId = :itemId AND i.todoList = :todoList")
    void updateCompletionStatus(@Param("todoList") TodoList todoList, @Param("itemId") Long itemId, @Param("completed") boolean completed);

    @Modifying
    @Query("UPDATE TodoItem i SET i.completed = :completed WHERE i.todoList = :todoList")
    void updateAllCompletionStatus(@Param("todoList") TodoList todoList, @Param("completed") boolean completed);

    @Query("SELECT COUNT(i) FROM TodoItem i WHERE i.todoList = :todoList")
    int countByTodoList(@Param("todoList") TodoList todoList);

    @Query("SELECT COUNT(i) FROM TodoItem i WHERE i.todoList = :todoList AND i.completed = true")
    int countCompletedByTodoList(@Param("todoList") TodoList todoList);
} 