package ru.putevod.app.planner.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_files", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"event_id", "file_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_file_id")
    private Long eventFileId;
    
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    
    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private File file;
    
    @Column
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 