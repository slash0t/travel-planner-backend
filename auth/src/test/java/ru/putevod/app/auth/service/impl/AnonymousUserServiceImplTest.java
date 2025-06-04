package ru.putevod.app.auth.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.putevod.app.auth.config.AppProperties;
import ru.putevod.app.auth.model.AnonymousUser;
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.repository.AnonymousUserRepository;
import ru.putevod.app.auth.security.JwtTokenProvider;
import ru.putevod.app.auth.service.DataMigrationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AnonymousUserServiceImplTest {

    @Mock
    private AnonymousUserRepository anonymousUserRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Jwt jwtConfig;

    @Mock
    private DataMigrationService dataMigrationService;

    @InjectMocks
    private AnonymousUserServiceImpl anonymousUserService;

    @Captor
    private ArgumentCaptor<AnonymousUser> anonymousUserCaptor;

    private static final String DEVICE_ID = "test-device-id";
    private static final String DEVICE_INFO = "test-device-info";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final Long ANONYMOUS_USER_ID = 1L;
    private static final String ANONYMOUS_TOKEN = "test-anonymous-token";

    @BeforeEach
    void setUp() {
        when(appProperties.getJwt()).thenReturn(jwtConfig);
        when(jwtConfig.getAnonymousTokenExpirationMs()).thenReturn(1800000L);
    }

    @Test
    void createOrGetAnonymousUser_NewUser() {
        when(anonymousUserRepository.findByDeviceId(DEVICE_ID)).thenReturn(Optional.empty());
        when(anonymousUserRepository.save(any(AnonymousUser.class))).thenAnswer(i -> {
            AnonymousUser user = i.getArgument(0);
            user.setAnonymousUserId(ANONYMOUS_USER_ID);
            user.setCreatedAt(LocalDateTime.now());
            user.setLastActivity(LocalDateTime.now());
            return user;
        });

        AnonymousUser result = anonymousUserService.createOrGetAnonymousUser(DEVICE_ID, DEVICE_INFO, IP_ADDRESS);

        verify(anonymousUserRepository).save(anonymousUserCaptor.capture());
        AnonymousUser savedUser = anonymousUserCaptor.getValue();

        assertNotNull(result);
        assertEquals(DEVICE_ID, savedUser.getDeviceId());
        assertEquals(DEVICE_INFO, savedUser.getDeviceInfo());
        assertEquals(IP_ADDRESS, savedUser.getIpAddress());
        assertFalse(savedUser.isMigrated());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getLastActivity());
    }

    @Test
    void createOrGetAnonymousUser_ExistingUser() {
        LocalDateTime oldLastActivity = LocalDateTime.now().minusHours(1);
        AnonymousUser existingUser = AnonymousUser.builder()
                .anonymousUserId(ANONYMOUS_USER_ID)
                .deviceId(DEVICE_ID)
                .deviceInfo("old-device-info")
                .ipAddress("old-ip")
                .createdAt(LocalDateTime.now().minusDays(1))
                .lastActivity(oldLastActivity)
                .build();

        when(anonymousUserRepository.findByDeviceId(DEVICE_ID)).thenReturn(Optional.of(existingUser));
        when(anonymousUserRepository.save(any(AnonymousUser.class))).thenAnswer(i -> {
            AnonymousUser user = i.getArgument(0);
            user.setLastActivity(LocalDateTime.now());
            return user;
        });

        AnonymousUser result = anonymousUserService.createOrGetAnonymousUser(DEVICE_ID, DEVICE_INFO, IP_ADDRESS);

        verify(anonymousUserRepository).save(anonymousUserCaptor.capture());
        AnonymousUser updatedUser = anonymousUserCaptor.getValue();

        assertNotNull(result);
        assertEquals(ANONYMOUS_USER_ID, updatedUser.getAnonymousUserId());
        assertEquals(DEVICE_ID, updatedUser.getDeviceId());
        assertEquals(DEVICE_INFO, updatedUser.getDeviceInfo());
        assertEquals(IP_ADDRESS, updatedUser.getIpAddress());
        assertTrue(updatedUser.getLastActivity().isAfter(oldLastActivity));
    }

    @Test
    void findByDeviceId() {
        AnonymousUser user = AnonymousUser.builder()
                .anonymousUserId(ANONYMOUS_USER_ID)
                .deviceId(DEVICE_ID)
                .build();

        when(anonymousUserRepository.findByDeviceId(DEVICE_ID)).thenReturn(Optional.of(user));

        Optional<AnonymousUser> result = anonymousUserService.findByDeviceId(DEVICE_ID);

        assertTrue(result.isPresent());
        assertEquals(ANONYMOUS_USER_ID, result.get().getAnonymousUserId());
        assertEquals(DEVICE_ID, result.get().getDeviceId());
    }

    @Test
    void createAnonymousToken() {
        AnonymousUser user = AnonymousUser.builder()
                .anonymousUserId(ANONYMOUS_USER_ID)
                .deviceId(DEVICE_ID)
                .build();

        when(anonymousUserRepository.findByDeviceId(DEVICE_ID)).thenReturn(Optional.of(user));
        when(anonymousUserRepository.save(any(AnonymousUser.class))).thenAnswer(i -> i.getArgument(0));
        when(tokenProvider.generateAnonymousToken(DEVICE_ID, ANONYMOUS_USER_ID)).thenReturn(ANONYMOUS_TOKEN);

        Map<String, Object> result = anonymousUserService.createAnonymousToken(DEVICE_ID, DEVICE_INFO, IP_ADDRESS);

        assertNotNull(result);
        assertEquals(ANONYMOUS_TOKEN, result.get("anonymousToken"));
        assertEquals(1800, result.get("expiresIn"));
        assertEquals(ANONYMOUS_USER_ID, result.get("anonymousUserId"));
    }

    @Test
    void updateLastActivity() {
        LocalDateTime oldLastActivity = LocalDateTime.now().minusHours(1);
        AnonymousUser user = AnonymousUser.builder()
                .anonymousUserId(ANONYMOUS_USER_ID)
                .deviceId(DEVICE_ID)
                .lastActivity(oldLastActivity)
                .build();

        when(anonymousUserRepository.findByDeviceId(DEVICE_ID)).thenReturn(Optional.of(user));
        when(anonymousUserRepository.save(any(AnonymousUser.class))).thenAnswer(i -> {
            AnonymousUser savedUser = i.getArgument(0);
            savedUser.setLastActivity(LocalDateTime.now());
            return savedUser;
        });

        anonymousUserService.updateLastActivity(DEVICE_ID);

        verify(anonymousUserRepository).save(anonymousUserCaptor.capture());
        AnonymousUser updatedUser = anonymousUserCaptor.getValue();

        assertTrue(updatedUser.getLastActivity().isAfter(oldLastActivity));
    }

    @Test
    void migrateAnonymousUserToRegistered() {
        AnonymousUser anonymousUser = AnonymousUser.builder()
                .anonymousUserId(ANONYMOUS_USER_ID)
                .deviceId(DEVICE_ID)
                .isMigrated(false)
                .build();

        User registeredUser = new User();
        registeredUser.setUserId(100);

        when(anonymousUserRepository.findByDeviceId(DEVICE_ID)).thenReturn(Optional.of(anonymousUser));
        when(anonymousUserRepository.save(any(AnonymousUser.class))).thenAnswer(i -> i.getArgument(0));

        anonymousUserService.migrateAnonymousUserToRegistered(DEVICE_ID, registeredUser);

        verify(anonymousUserRepository).save(anonymousUserCaptor.capture());
        AnonymousUser migratedUser = anonymousUserCaptor.getValue();

        assertTrue(migratedUser.isMigrated());
        assertEquals(100L, migratedUser.getMigratedToUserId());
        assertNotNull(migratedUser.getMigrationDate());
        verify(dataMigrationService).migrateAnonymousUserData(ANONYMOUS_USER_ID, 100);
    }

    @Test
    void cleanupInactiveAnonymousUsers() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<AnonymousUser> inactiveUsers = List.of(
                AnonymousUser.builder().anonymousUserId(1L).deviceId("device1").build(),
                AnonymousUser.builder().anonymousUserId(2L).deviceId("device2").build()
        );

        when(anonymousUserRepository.findInactiveAnonymousUsers(cutoffDate)).thenReturn(inactiveUsers);

        anonymousUserService.cleanupInactiveAnonymousUsers();

        verify(anonymousUserRepository, times(2)).delete(any(AnonymousUser.class));
    }

    @Test
    void getActiveAnonymousUsersCount() {
        when(anonymousUserRepository.countActiveAnonymousUsers()).thenReturn(5L);

        long count = anonymousUserService.getActiveAnonymousUsersCount();

        assertEquals(5L, count);
        verify(anonymousUserRepository).countActiveAnonymousUsers();
    }
} 