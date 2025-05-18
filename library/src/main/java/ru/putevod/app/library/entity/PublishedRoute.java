package ru.putevod.app.library.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "published_trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublishedRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "published_id")
    private Long id;

    @Column(name = "trip_id")
    private Long originalRouteId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "country")
    private String country;

    @Column(name = "city")
    private String city;

    @Column(name = "duration_days")
    private Integer duration;

    @Column(name = "cover_photo_id")
    private Long coverPhotoId;

    @Column(name = "tags", columnDefinition = "TEXT[]")
    private String[] tags;

    @Column(name = "is_approved")
    private Boolean isApproved;

    @Column(name = "view_count")
    private Integer viewCount;

    @CreationTimestamp
    @Column(name = "published_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "publishedRoute", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RouteRating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "publishedRoute", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RouteComment> comments = new ArrayList<>();

    @Transient
    private Double averageRating;

    public Double getAverageRating() {
        if (ratings == null || ratings.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (RouteRating rating : ratings) {
            sum += rating.getRating();
        }
        return sum / ratings.size();
    }

    public Integer getReviewsCount() {
        return ratings != null ? ratings.size() : 0;
    }
} 