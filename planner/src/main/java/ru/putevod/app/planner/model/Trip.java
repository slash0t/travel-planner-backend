package ru.putevod.app.planner.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trips")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long tripId;
    
    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column
    private String description;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(length = 100)
    private String country;
    
    @Column(length = 100)
    private String city;
    
    @Column(name = "is_public")
    private boolean published;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_deleted")
    private boolean isDeleted;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TripDay> days = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TripAccess> accesses = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TripFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TodoList> todoLists = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public String getStatus() {
        LocalDate now = LocalDate.now();
        if (startDate != null && endDate != null) {
            if (now.isBefore(startDate)) {
                return "upcoming";
            } else if (now.isAfter(endDate)) {
                return "past";
            } else {
                return "ongoing";
            }
        }
        return "unknown";
    }
} 