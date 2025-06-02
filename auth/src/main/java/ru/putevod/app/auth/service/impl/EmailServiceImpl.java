package ru.putevod.app.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.putevod.app.auth.config.AppProperties;
import ru.putevod.app.auth.model.EmailVerificationToken;
import ru.putevod.app.auth.model.PasswordResetToken;
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.repository.EmailVerificationTokenRepository;
import ru.putevod.app.auth.repository.PasswordResetTokenRepository;
import ru.putevod.app.auth.repository.UserRepository;
import ru.putevod.app.auth.service.EmailService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public void sendVerificationEmail(String email, String username, String verificationToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .token(verificationToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(appProperties.getVerificationTokenExpirationHours()))
                .build();

        emailVerificationTokenRepository.save(token);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(appProperties.getEmail().getFrom());
        message.setTo(email);
        message.setSubject("Подтверждение регистрации в Travel Planner");
        message.setText(String.format(
                "Здравствуйте, %s!\n\n" +
                        "Для подтверждения регистрации в приложении Travel Planner перейдите по ссылке:\n" +
                        "%s/verify-email?token=%s\n\n" +
                        "Ссылка действительна в течение %d часов.\n\n" +
                        "Если вы не регистрировались в нашем приложении, просто проигнорируйте это письмо.",
                username, appProperties.getFrontendUrl(), verificationToken, appProperties.getVerificationTokenExpirationHours()
        ));

        mailSender.send(message);
    }

    @Override
    @Transactional
    public User verifyEmailToken(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недействительный токен верификации"));

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            emailVerificationTokenRepository.deleteByToken(token);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Срок действия токена истек");
        }
        User user = verificationToken.getUser();

        emailVerificationTokenRepository.deleteByToken(token);

        return user;
    }

    @Override
    @Transactional
    public void sendPasswordResetEmail(String email, String username, String resetCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        storeResetCode(email, resetCode);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(appProperties.getEmail().getFrom());
        message.setTo(email);
        message.setSubject("Сброс пароля в Travel Planner");
        message.setText(String.format(
                "Здравствуйте, %s!\n\n" +
                        "Для сброса пароля в приложении Travel Planner используйте этот код:\n" +
                        "%s\n\n" +
                        "Код действителен в течение %d минут.\n\n" +
                        "Если вы не запрашивали сброс пароля, просто проигнорируйте это письмо.",
                username, resetCode, appProperties.getResetTokenExpirationMinutes()
        ));

        mailSender.send(message);
    }

    @Override
    @Transactional
    public void storeResetCode(String email, String resetCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        String token = java.util.UUID.randomUUID().toString();

        List<PasswordResetToken> existingTokens = passwordResetTokenRepository.findByUserEmailAndIsUsed(email, false);
        for (PasswordResetToken existingToken : existingTokens) {
            existingToken.setIsUsed(true);
            passwordResetTokenRepository.save(existingToken);
        }

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .resetCode(resetCode)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(appProperties.getResetTokenExpirationMinutes()))
                .isUsed(false)
                .build();

        passwordResetTokenRepository.save(resetToken);
    }

    @Override
    @Transactional
    public boolean verifyResetCode(String email, String code) {
        List<PasswordResetToken> tokens = passwordResetTokenRepository
                .findByUserEmailAndResetCodeAndIsUsed(email, code, false)
                .stream()
                .toList();

        if (tokens.isEmpty()) {
            return false;
        }

        PasswordResetToken token = tokens.get(0);
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            token.setIsUsed(true);
            passwordResetTokenRepository.save(token);
            return false;
        }

        if (tokens.size() > 1) {
            tokens.stream()
                    .skip(1)
                    .forEach(oldToken -> {
                        oldToken.setIsUsed(true);
                        passwordResetTokenRepository.save(oldToken);
                    });
        }

        return true;
    }

    @Override
    @Transactional
    public void storeResetToken(String email, String resetToken) {
        List<PasswordResetToken> tokens = passwordResetTokenRepository
                .findByUserEmailAndIsUsed(email, false);

        if (!tokens.isEmpty()) {
            PasswordResetToken token = tokens.get(0);
            token.setToken(resetToken);
            token.setExpiresAt(LocalDateTime.now().plusMinutes(appProperties.getResetTokenExpirationMinutes()));
            passwordResetTokenRepository.save(token);

            if (tokens.size() > 1) {
                tokens.stream()
                        .skip(1)
                        .forEach(oldToken -> {
                            oldToken.setIsUsed(true);
                            passwordResetTokenRepository.save(oldToken);
                        });
            }
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

        if (token.getExpiresAt().isBefore(LocalDateTime.now()) || Boolean.TRUE.equals(token.getIsUsed())) {
            return null;
        }

        return token.getUser().getEmail();
    }

    @Override
    @Transactional
    public void invalidateResetToken(String resetToken) {
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(resetToken);

        if (tokenOptional.isPresent()) {
            PasswordResetToken token = tokenOptional.get();
            token.setIsUsed(true);
            passwordResetTokenRepository.save(token);

            User user = token.getUser();
            List<PasswordResetToken> otherTokens = passwordResetTokenRepository.findByUserEmailAndIsUsed(user.getEmail(), false);

            for (PasswordResetToken otherToken : otherTokens) {
                if (!otherToken.getToken().equals(resetToken)) {
                    otherToken.setIsUsed(true);
                    passwordResetTokenRepository.save(otherToken);
                }
            }
        }
    }
} 