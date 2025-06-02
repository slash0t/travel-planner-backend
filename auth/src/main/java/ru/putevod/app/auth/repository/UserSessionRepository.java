package ru.putevod.app.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.model.UserSession;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByToken(String token);

    List<UserSession> findByUser(User user);

    void deleteByToken(String token);

    void deleteByUser(User user);
} 