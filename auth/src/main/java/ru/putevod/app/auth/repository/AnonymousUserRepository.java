package ru.putevod.app.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.putevod.app.auth.model.AnonymousUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnonymousUserRepository extends JpaRepository<AnonymousUser, Long> {
    
    Optional<AnonymousUser> findByDeviceId(String deviceId);
    
    boolean existsByDeviceId(String deviceId);
    
    List<AnonymousUser> findByIsMigratedFalse();
    
    @Query("SELECT au FROM AnonymousUser au WHERE au.lastActivity < :cutoffDate AND au.isMigrated = false")
    List<AnonymousUser> findInactiveAnonymousUsers(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT COUNT(au) FROM AnonymousUser au WHERE au.isMigrated = false")
    long countActiveAnonymousUsers();
} 