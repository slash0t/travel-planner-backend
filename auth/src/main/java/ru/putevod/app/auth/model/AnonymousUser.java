package ru.putevod.app.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "anonymous_users")
public class AnonymousUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "anonymous_user_id")
    private Long anonymousUserId;

    @Column(name = "device_id", nullable = false, unique = true)
    private String deviceId;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "is_migrated")
    private boolean isMigrated;

    @Column(name = "migrated_to_user_id")
    private Long migratedToUserId;

    @Column(name = "migration_date")
    private LocalDateTime migrationDate;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActivity = LocalDateTime.now();
        isMigrated = false;
    }

    @PreUpdate
    protected void onUpdate() {
        lastActivity = LocalDateTime.now();
    }
} 