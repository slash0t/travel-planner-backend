package ru.putevod.app.planner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.putevod.app.planner.model.TemplateItem;
import ru.putevod.app.planner.model.TodoTemplate;

import java.util.List;

@Repository
public interface TemplateItemRepository extends JpaRepository<TemplateItem, Long> {
    
    List<TemplateItem> findByTemplateOrderByOrderPosition(TodoTemplate template);
    
    List<TemplateItem> findByTemplateTemplateIdOrderByOrderPosition(Long templateId);
    
    void deleteByTemplate(TodoTemplate template);
} 