package ru.putevod.app.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.putevod.app.auth.model.PasswordResetToken;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByToken(String token);

    List<PasswordResetToken> findByUserEmailAndResetCodeAndIsUsed(String email, String resetCode, Boolean isUsed);

    List<PasswordResetToken> findByUserEmailAndIsUsed(String email, Boolean isUsed);

    void deleteByToken(String token);
} 