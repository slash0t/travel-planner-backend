package ru.putevod.app.auth.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.putevod.app.auth.config.AppProperties;
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.repository.UserSessionRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock(lenient = true)
    private AppProperties appProperties;

    @Mock
    private UserSessionRepository userSessionRepository;

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private AppProperties.Jwt jwt;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwt = new AppProperties.Jwt();
        jwt.setSecret("testSecretKeyThatIsLongEnoughForHS256Algoritm12345");
        jwt.setAccessTokenExpirationMs(3600000L);
        jwt.setRefreshTokenExpirationMs(86400000L);
        jwt.setAnonymousTokenExpirationMs(1800000L);
        
        when(appProperties.getJwt()).thenReturn(jwt);

        testUser = new User();
        testUser.setUserId(1);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setIsAdmin(false);
    }

    @Test
    void generateAccessToken_shouldCreateValidToken() {
        String token = jwtTokenProvider.generateAccessToken(testUser);
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(testUser.getEmail(), jwtTokenProvider.getEmailFromToken(token));
        assertEquals(testUser.getUserId().longValue(), jwtTokenProvider.getUserIdFromToken(token));
        assertEquals(testUser.getUsername(), jwtTokenProvider.getUsernameFromToken(token));
        assertEquals(testUser.getIsAdmin(), jwtTokenProvider.isAdminFromToken(token));
    }

    @Test
    void generateAnonymousToken_shouldCreateValidToken() {
        String deviceId = "test-device";
        String token = jwtTokenProvider.generateAnonymousToken(deviceId);
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertTrue(jwtTokenProvider.isAnonymousToken(token));
    }

    @Test
    void validateToken_withExpiredToken_shouldReturnFalse() {
        jwt = new AppProperties.Jwt();
        jwt.setSecret("testSecretKeyThatIsLongEnoughForHS256Algoritm12345");
        jwt.setAccessTokenExpirationMs(-3600000L);
        when(appProperties.getJwt()).thenReturn(jwt);
        
        String token = jwtTokenProvider.generateAccessToken(testUser);
        
        assertThrows(ExpiredJwtException.class, () -> jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_withInvalidSignature_shouldThrowSignatureException() {
        String token = jwtTokenProvider.generateAccessToken(testUser);
        jwt.setSecret("differentSecretKeyThatIsLongEnoughForHS256Algoritm12345");
        when(appProperties.getJwt()).thenReturn(jwt);
        
        assertThrows(SignatureException.class, () -> jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateServiceToken_withValidToken_shouldReturnTrue() {
        String serviceToken = "valid-service-token";
        when(appProperties.getAuthToken()).thenReturn(serviceToken);
        
        assertTrue(jwtTokenProvider.validateServiceToken(serviceToken));
    }

    @Test
    void validateServiceToken_withInvalidToken_shouldReturnFalse() {
        String serviceToken = "valid-service-token";
        when(appProperties.getAuthToken()).thenReturn(serviceToken);
        
        assertFalse(jwtTokenProvider.validateServiceToken("invalid-token"));
    }

    @Test
    void generateRefreshToken_shouldCreateValidTokenAndSaveSession() {
        String deviceInfo = "test-device";
        String ipAddress = "127.0.0.1";
        
        String token = jwtTokenProvider.generateRefreshToken(testUser, deviceInfo, ipAddress);
        
        assertNotNull(token);
        assertTrue(token.length() > 0);
        verify(userSessionRepository).save(any());
    }
}
