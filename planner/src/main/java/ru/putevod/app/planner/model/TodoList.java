package ru.putevod.app.planner.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "todo_lists")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "list_id")
    private Long listId;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column
    private String description;
    
    @Column(name = "list_type", length = 50)
    private String listType;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "todoList", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TodoItem> items = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public int getItemCount() {
        return items.size();
    }
    
    public int getCompletedCount() {
        return (int) items.stream().filter(TodoItem::isCompleted).count();
    }
} 