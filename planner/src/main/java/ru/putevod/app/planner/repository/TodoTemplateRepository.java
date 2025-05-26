package ru.putevod.app.planner.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.putevod.app.planner.model.TodoTemplate;
import ru.putevod.app.planner.model.User;

import java.util.List;

@Repository
public interface TodoTemplateRepository extends JpaRepository<TodoTemplate, Long> {
    
    List<TodoTemplate> findByCategory(String category);
    
    List<TodoTemplate> findByIsSystem(boolean isSystem);
    
    List<TodoTemplate> findByCreatedBy(User user);
    
    @Query("SELECT t FROM TodoTemplate t WHERE t.isSystem = true OR t.createdBy = :user")
    List<TodoTemplate> findAllAvailableToUser(User user);
    
    @Query("SELECT t FROM TodoTemplate t WHERE t.category = :category AND (t.isSystem = true OR t.createdBy = :user)")
    List<TodoTemplate> findByCategoryAndAvailableToUser(String category, User user);
}
