package ru.putevod.app.library.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long id;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "country")
    private String country;

    @Column(name = "city")
    private String city;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "preview_url")
    private String previewUrl;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "trip_id")
    @Builder.Default
    private List<TripDay> days = new ArrayList<>();

    @Transient
    public Integer getDuration() {
        if (startDate != null && endDate != null) {
            return (int) (endDate.toEpochDay() - startDate.toEpochDay() + 1);
        }
        return days != null ? days.size() : 0;
    }
} 