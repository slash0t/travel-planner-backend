package ru.putevod.app.library.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.putevod.app.library.entity.PublishedRoute;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublishedRouteRepository extends JpaRepository<PublishedRoute, Long> {

    @Query("SELECT p FROM PublishedRoute p WHERE p.isApproved = true ORDER BY p.createdAt DESC")
    Page<PublishedRoute> findAllApproved(Pageable pageable);

    @Query(value = "SELECT * FROM published_trips p WHERE p.is_approved = true AND " +
            "(:country IS NULL OR p.country = :country) AND " +
            "(:city IS NULL OR p.city = :city) AND " +
            "(:durationMin IS NULL OR p.duration_days >= :durationMin) AND " +
            "(:durationMax IS NULL OR p.duration_days <= :durationMax) AND " +
            "(:tag IS NULL OR :tag = ANY(p.tags)) " +
            "ORDER BY p.published_at DESC",
            countQuery = "SELECT COUNT(*) FROM published_trips p WHERE p.is_approved = true AND " +
                    "(:country IS NULL OR p.country = :country) AND " +
                    "(:city IS NULL OR p.city = :city) AND " +
                    "(:durationMin IS NULL OR p.duration_days >= :durationMin) AND " +
                    "(:durationMax IS NULL OR p.duration_days <= :durationMax) AND " +
                    "(:tag IS NULL OR :tag = ANY(p.tags))",
            nativeQuery = true)
    Page<PublishedRoute> findWithFilters(
            @Param("country") String country,
            @Param("city") String city,
            @Param("durationMin") Integer durationMin,
            @Param("durationMax") Integer durationMax,
            @Param("tag") String tag,
            Pageable pageable);

    @Query("SELECT p FROM PublishedRoute p WHERE p.isApproved = true AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.country) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.city) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY p.createdAt DESC")
    Page<PublishedRoute> searchByQuery(@Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM PublishedRoute p WHERE p.isApproved = true ORDER BY p.viewCount DESC")
    Page<PublishedRoute> findPopular(Pageable pageable);

    @Query("SELECT p FROM PublishedRoute p WHERE p.isApproved = true ORDER BY SIZE(p.ratings) DESC, p.viewCount DESC")
    Page<PublishedRoute> findMostRated(Pageable pageable);

    Optional<PublishedRoute> findByIdAndIsApprovedTrue(Long id);

    List<PublishedRoute> findByUserIdAndIsApprovedTrue(Long userId);

    Optional<PublishedRoute> findByOriginalRouteIdAndIsApprovedTrue(Long originalRouteId);

    boolean existsByOriginalRouteId(Long originalRouteId);
} 