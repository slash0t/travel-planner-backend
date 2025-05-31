package ru.putevod.app.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails userDetails;
    private static final String TEST_TOKEN = "test-token";
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        userDetails = new User(TEST_EMAIL, "", Collections.emptyList());
    }

    @Test
    void doFilterInternal_withValidToken_shouldSetAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TEST_TOKEN);
        when(jwtTokenProvider.getEmailFromToken(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);
        when(jwtTokenProvider.validateToken(TEST_TOKEN, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(TEST_EMAIL, SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withoutAuthHeader_shouldNotSetAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withInvalidAuthHeaderFormat_shouldNotSetAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Invalid " + TEST_TOKEN);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withInvalidToken_shouldNotSetAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TEST_TOKEN);
        when(jwtTokenProvider.getEmailFromToken(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);
        when(jwtTokenProvider.validateToken(TEST_TOKEN, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenTokenValidationThrowsException_shouldPropagateException() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TEST_TOKEN);
        when(jwtTokenProvider.getEmailFromToken(TEST_TOKEN)).thenThrow(new RuntimeException("Token validation failed"));

        assertThrows(RuntimeException.class, () ->
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)
        );
    }
}
