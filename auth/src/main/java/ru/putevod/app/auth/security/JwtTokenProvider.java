package ru.putevod.app.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final AppProperties appProperties;
    private final UserSessionRepository userSessionRepository;

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getUserId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + appProperties.getJwt().getAccessTokenExpirationMs()))
                .signWith(Keys.hmacShaKeyFor(appProperties.getJwt().getSecret().getBytes()))
                .compact();
    }

    public String generateRefreshToken(User user, String deviceInfo, String ipAddress) {
        String token = UUID.randomUUID().toString();

        UserSession session = UserSession.builder()
                .user(user)
                .token(token)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .expiresAt(LocalDateTime.now()
                        .plusSeconds(appProperties.getJwt()
                                .getRefreshTokenExpirationMs() / 1000))
                .createdAt(LocalDateTime.now()).lastActivity(LocalDateTime.now())
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
                .setExpiration(new Date(System.currentTimeMillis() + appProperties.getJwt().getAnonymousTokenExpirationMs()))
                .signWith(Keys.hmacShaKeyFor(appProperties.getJwt().getSecret().getBytes()))
                .compact();
    }

    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Long getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userId", Long.class));
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
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
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
     * @throws SignatureException       если подпись токена неверна
     * @throws MalformedJwtException    если формат токена неверен
     * @throws ExpiredJwtException      если срок действия токена истек
     * @throws UnsupportedJwtException  если токен не поддерживается
     * @throws IllegalArgumentException если токен не содержит claims
     */
    public boolean validateToken(String token) throws SignatureException, MalformedJwtException,
            ExpiredJwtException, UnsupportedJwtException,
            IllegalArgumentException {
        JwtParser jwtParser = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(appProperties.getJwt().getSecret().getBytes()))
                .build();

        jwtParser.parseClaimsJws(token);
        return !isTokenExpired(token);
    }

    /**
     * Проверяет, соответствует ли переданный токен ожидаемому токену межсервисного взаимодействия
     *
     * @param providedToken токен для проверки
     * @return true если токен действительный, false в противном случае
     */
    public boolean validateServiceToken(String providedToken) {
        return providedToken != null && providedToken.equals(appProperties.getAuthToken());
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private Key getSigningKey() {
        byte[] keyBytes = appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 