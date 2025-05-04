package ru.putevod.app.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.putevod.app.auth.config.AppProperties;
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.model.UserSession;
import ru.putevod.app.auth.repository.UserSessionRepository;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;
    
    @Value("${app.jwt.anonymous-token-expiration-ms:1800000}")
    private long anonymousTokenExpirationMs;
    
    @Value("${auth.token:${AUTH_SERVICE_TOKEN:service_token_for_development}}")
    private String serviceToken;

    private final AppProperties appProperties;
    private final UserSessionRepository userSessionRepository;

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getUserId())
                .claim("username", user.getUsername())
                .claim("isAdmin", user.getIsAdmin())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String generateRefreshToken(User user, String deviceInfo, String ipAddress) {
        String token = UUID.randomUUID().toString();

        UserSession session = UserSession.builder()
                .user(user)
                .token(token)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .expiresAt(LocalDateTime.now().plusNanos(refreshTokenExpirationMs))
                .createdAt(LocalDateTime.now())
                .lastActivity(LocalDateTime.now())
                .build();

        userSessionRepository.save(session);

        return token;
    }
    
    public String generateAnonymousToken(String deviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("isAnonymous", true);
        if (deviceId != null && !deviceId.isEmpty()) {
            claims.put("deviceId", deviceId);
        }
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + anonymousTokenExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    public Long getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userId", Long.class));
    }
    
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("username", String.class));
    }
    
    public Boolean isAdminFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("isAdmin", Boolean.class));
    }
    
    public Boolean isAnonymousToken(String token) {
        try {
            Boolean isAnonymous = getClaimFromToken(token, claims -> claims.get("isAnonymous", Boolean.class));
            return isAnonymous != null ? isAnonymous : false;
        } catch (Exception e) {
            return false;
        }
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String email = getEmailFromToken(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    /**
     * Валидирует JWT токен без проверки UserDetails
     * 
     * @param token JWT токен для проверки
     * @return true если токен валидный, false в противном случае
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (SignatureException e) {
            log.error("Неверная подпись JWT: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Неверный формат JWT: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT токен истек: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT токен не поддерживается: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT неверные аргументы: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * Проверяет, соответствует ли переданный токен ожидаемому токену межсервисного взаимодействия
     * 
     * @param providedToken токен для проверки
     * @return true если токен действительный, false в противном случае
     */
    public boolean validateServiceToken(String providedToken) {
        return providedToken != null && providedToken.equals(serviceToken);
    }

    private boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private Key getSigningKey() {
        byte[] keyBytes = appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 