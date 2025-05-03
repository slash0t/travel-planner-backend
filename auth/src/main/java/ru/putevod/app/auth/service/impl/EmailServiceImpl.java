package ru.putevod.app.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.putevod.app.auth.model.EmailVerificationToken;
import ru.putevod.app.auth.model.PasswordResetToken;
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.repository.EmailVerificationTokenRepository;
import ru.putevod.app.auth.repository.PasswordResetTokenRepository;
import ru.putevod.app.auth.repository.UserRepository;
import ru.putevod.app.auth.service.EmailService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.verification-token-expiration-hours:24}")
    private int verificationTokenExpirationHours;

    @Value("${app.reset-token-expiration-minutes:15}")
    private int resetTokenExpirationMinutes;

    @Override
    @Transactional
    public void sendVerificationEmail(String email, String username, String verificationToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .token(verificationToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(verificationTokenExpirationHours))
                .build();

        emailVerificationTokenRepository.save(token);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Подтверждение регистрации в Travel Planner");
        message.setText(String.format(
                "Здравствуйте, %s!\n\n" +
                "Для подтверждения регистрации в приложении Travel Planner перейдите по ссылке:\n" +
                "%s/verify-email?token=%s\n\n" +
                "Ссылка действительна в течение %d часов.\n\n" +
                "Если вы не регистрировались в нашем приложении, просто проигнорируйте это письмо.",
                username, frontendUrl, verificationToken, verificationTokenExpirationHours
        ));

        mailSender.send(message);
    }

    @Override
    @Transactional
    public User verifyEmailToken(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недействительный токен верификации"));

        if (verificationToken.expiresAt().isBefore(LocalDateTime.now())) {
            emailVerificationTokenRepository.deleteByToken(token);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Срок действия токена истек");
        }
        User user = verificationToken.user();

        emailVerificationTokenRepository.deleteByToken(token);

        return user;
    }

    @Override
    @Transactional
    public void sendPasswordResetEmail(String email, String username, String resetCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        String token = java.util.UUID.randomUUID().toString();
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .resetCode(resetCode)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(resetTokenExpirationMinutes))
                .isUsed(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Сброс пароля в Travel Planner");
        message.setText(String.format(
                "Здравствуйте, %s!\n\n" +
                "Для сброса пароля в приложении Travel Planner используйте этот код:\n" +
                "%s\n\n" +
                "Код действителен в течение %d минут.\n\n" +
                "Если вы не запрашивали сброс пароля, просто проигнорируйте это письмо.",
                username, resetCode, resetTokenExpirationMinutes
        ));

        mailSender.send(message);
    }

    @Override
    @Transactional
    public void storeResetCode(String email, String resetCode) {
    }

    @Override
    @Transactional
    public boolean verifyResetCode(String email, String code) {
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository
                .findByUserEmailAndResetCodeAndIsUsed(email, code, false);

        if (tokenOptional.isEmpty()) {
            return false;
        }

        PasswordResetToken token = tokenOptional.get();

        if (token.expiresAt().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.deleteByToken(token.token());
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public void storeResetToken(String email, String resetToken) {
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository
                .findByUserEmailAndResetCodeAndIsUsed(email, resetToken, false);

        if (tokenOptional.isPresent()) {
            PasswordResetToken token = tokenOptional.get();
            token.token(resetToken);
            token.expiresAt(LocalDateTime.now().plusMinutes(resetTokenExpirationMinutes));
            passwordResetTokenRepository.save(token);
        }
    }

    @Override
    @Transactional
    public String getEmailByResetToken(String resetToken) {
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(resetToken);

        if (tokenOptional.isEmpty()) {
            return null;
        }

        PasswordResetToken token = tokenOptional.get();

        if (token.expiresAt().isBefore(LocalDateTime.now()) || Boolean.TRUE.equals(token.isUsed())) {
            return null;
        }

        return token.user().email();
    }

    @Override
    @Transactional
    public void invalidateResetToken(String resetToken) {
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(resetToken);

        if (tokenOptional.isPresent()) {
            PasswordResetToken token = tokenOptional.get();
            token.isUsed(true);
            passwordResetTokenRepository.save(token);
        }
    }
} 