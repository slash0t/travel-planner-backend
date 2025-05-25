package ru.putevod.app.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.putevod.app.auth.security.JwtAuthenticationFilter;
import ru.putevod.app.auth.security.JwtExceptionHandler;
import ru.putevod.app.auth.security.JwtTokenProvider;
import ru.putevod.app.auth.security.JwtUserDetailsService;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }
    
    @Bean
    public JwtExceptionHandler jwtExceptionHandler() {
        return new JwtExceptionHandler(objectMapper);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/login", "/api/v1/register", "/api/v1/verify-email",
                                "/api/v1/resend-verification", "/api/v1/forgot-password",
                                "/api/v1/verify-reset-code", "/api/v1/reset-password",
                                "/api/v1/anonymous-token", "/api/v1/refresh",
                                "/api/v1/auth/validate", "/api/v1/auth/userinfo",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .userDetailsService(userDetailsService)
                .addFilterBefore(jwtExceptionHandler(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        if (appProperties.getSecurity().getAllowedOrigins() != null) {
            configuration.setAllowedOrigins(
                    Arrays.asList(appProperties.getSecurity().getAllowedOrigins().split(",")));
        } else {
            configuration.setAllowedOriginPatterns(List.of("*"));
        }
        
        if (appProperties.getSecurity().getAllowedMethods() != null) {
            configuration.setAllowedMethods(Arrays.asList(appProperties.getSecurity().getAllowedMethods().split(",")));
        } else {
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        }
        
        if (appProperties.getSecurity().getAllowedHeaders() != null) {
            configuration.setAllowedHeaders(Arrays.asList(appProperties.getSecurity().getAllowedHeaders().split(",")));
        } else {
            configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Auth-Token", "X-Service-Token"));
        }
        
        configuration.setExposedHeaders(List.of("X-Auth-Token"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 