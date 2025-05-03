package ru.putevod.app.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
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
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;
    
    @Value("${app.jwt.anonymous-token-expiration-ms:1800000}")
    private long anonymousTokenExpirationMs;

    private final AppProperties appProperties;
    private final UserSessionRepository userSessionRepository;

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.email())
                .claim("userId", user.userId())
                .claim("username", user.username())
                .claim("isAdmin", user.isAdmin())
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

    private boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private Key getSigningKey() {
        byte[] keyBytes = appProperties.jwt().secret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 