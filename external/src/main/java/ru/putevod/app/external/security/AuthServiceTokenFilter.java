package ru.putevod.app.external.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.putevod.app.external.client.AuthServiceClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthServiceTokenFilter extends OncePerRequestFilter {

    private final AuthServiceClient authServiceClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (token != null && authServiceClient.validateToken(token)) {
                Map<String, Object> userInfo = authServiceClient.getUserInfoFromToken(token);
                if (userInfo != null) {
                    setAuthenticationContext(userInfo, token);
                }
            }
        } catch (Exception ex) {
            log.error("Не удалось установить аутентификацию пользователя: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void setAuthenticationContext(Map<String, Object> userInfo, String token) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        if (userInfo.containsKey("roles") && userInfo.get("roles") instanceof List) {
            List<String> roles = (List<String>) userInfo.get("roles");
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        }
        
        if (Boolean.TRUE.equals(userInfo.get("isAdmin"))) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        String email = (String) userInfo.get("email");
        User user = new User(email, "", authorities);
        
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(user, token, authorities);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
} 