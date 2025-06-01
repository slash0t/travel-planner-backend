package ru.putevod.app.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.server.ResponseStatusException;
import ru.putevod.app.auth.config.AppProperties;
import ru.putevod.app.auth.model.EmailVerificationToken;
import ru.putevod.app.auth.model.PasswordResetToken;
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.repository.EmailVerificationTokenRepository;
import ru.putevod.app.auth.repository.PasswordResetTokenRepository;
import ru.putevod.app.auth.repository.UserRepository;
import ru.putevod.app.auth.service.impl.EmailServiceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Email emailProperties;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    private User testUser;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_TOKEN = "test-token";
    private static final String TEST_RESET_CODE = "123456";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1)
                .email(TEST_EMAIL)
                .username(TEST_USERNAME)
                .build();
    }

    @Test
    void sendVerificationEmail_shouldSaveTokenAndSendEmail() {
        when(appProperties.getEmail()).thenReturn(emailProperties);
        when(emailProperties.getFrom()).thenReturn("noreply@example.com");
        when(appProperties.getVerificationTokenExpirationHours()).thenReturn(24);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        emailService.sendVerificationEmail(TEST_EMAIL, TEST_USERNAME, TEST_TOKEN);

        verify(emailVerificationTokenRepository).save(any(EmailVerificationToken.class));
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();
        assertEquals(TEST_EMAIL, message.getTo()[0]);
        assertNotNull(message.getSubject());
        assertNotNull(message.getText());
    }

    @Test
    void sendVerificationEmail_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                emailService.sendVerificationEmail(TEST_EMAIL, TEST_USERNAME, TEST_TOKEN));
    }

    @Test
    void verifyEmailToken_whenTokenValid_shouldReturnUser() {
        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(testUser)
                .token(TEST_TOKEN)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        when(emailVerificationTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(token));

        User result = emailService.verifyEmailToken(TEST_TOKEN);

        assertEquals(testUser, result);
    }

    @Test
    void verifyEmailToken_whenTokenExpired_shouldThrowException() {
        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(testUser)
                .token(TEST_TOKEN)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        when(emailVerificationTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(token));

        assertThrows(ResponseStatusException.class, () -> emailService.verifyEmailToken(TEST_TOKEN));
    }

    @Test
    void sendPasswordResetEmail_shouldSendEmail() {
        when(appProperties.getEmail()).thenReturn(emailProperties);
        when(emailProperties.getFrom()).thenReturn("noreply@example.com");
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        emailService.sendPasswordResetEmail(TEST_EMAIL, TEST_USERNAME, TEST_RESET_CODE);

        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();
        assertEquals(TEST_EMAIL, message.getTo()[0]);
        assertNotNull(message.getSubject());
        assertNotNull(message.getText());
    }

    @Test
    void storeResetCode_shouldSaveToken() {
        when(appProperties.getResetTokenExpirationMinutes()).thenReturn(30);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        emailService.storeResetCode(TEST_EMAIL, TEST_RESET_CODE);

        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void verifyResetCode_whenCodeValid_shouldReturnTrue() {
        PasswordResetToken token = PasswordResetToken.builder()
                .user(testUser)
                .resetCode(TEST_RESET_CODE)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .isUsed(false)
                .build();

        when(passwordResetTokenRepository.findByUserEmailAndResetCodeAndIsUsed(TEST_EMAIL, TEST_RESET_CODE, false))
                .thenReturn(Arrays.asList(token));

        boolean result = emailService.verifyResetCode(TEST_EMAIL, TEST_RESET_CODE);

        assertTrue(result);
    }

    @Test
    void verifyResetCode_whenCodeExpired_shouldReturnFalse() {
        PasswordResetToken token = PasswordResetToken.builder()
                .user(testUser)
                .resetCode(TEST_RESET_CODE)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .isUsed(false)
                .build();

        when(passwordResetTokenRepository.findByUserEmailAndResetCodeAndIsUsed(TEST_EMAIL, TEST_RESET_CODE, false))
                .thenReturn(Arrays.asList(token));

        boolean result = emailService.verifyResetCode(TEST_EMAIL, TEST_RESET_CODE);

        assertFalse(result);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void getEmailByResetToken_whenTokenValid_shouldReturnEmail() {
        PasswordResetToken token = PasswordResetToken.builder()
                .user(testUser)
                .token(TEST_TOKEN)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .isUsed(false)
                .build();

        when(passwordResetTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(token));

        String result = emailService.getEmailByResetToken(TEST_TOKEN);

        assertEquals(TEST_EMAIL, result);
    }

    @Test
    void getEmailByResetToken_whenTokenExpired_shouldReturnNull() {
        PasswordResetToken token = PasswordResetToken.builder()
                .user(testUser)
                .token(TEST_TOKEN)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .isUsed(false)
                .build();

        when(passwordResetTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(token));

        String result = emailService.getEmailByResetToken(TEST_TOKEN);

        assertNull(result);
    }

    @Test
    void invalidateResetToken_shouldMarkTokenAsUsed() {
        PasswordResetToken token = PasswordResetToken.builder()
                .user(testUser)
                .token(TEST_TOKEN)
                .isUsed(false)
                .build();

        when(passwordResetTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(token));
        when(passwordResetTokenRepository.findByUserEmailAndIsUsed(TEST_EMAIL, false))
                .thenReturn(Arrays.asList(token));

        emailService.invalidateResetToken(TEST_TOKEN);

        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }
} 