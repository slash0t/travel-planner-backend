package ru.putevod.app.auth.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import ru.putevod.app.auth.annotation.RequireRole;
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.model.UserRole;
import ru.putevod.app.auth.security.JwtTokenProvider;
import ru.putevod.app.auth.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleCheckAspectTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserService userService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private RoleCheckAspect roleCheckAspect;

    private static final String TEST_TOKEN = "test-token";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_METHOD_NAME = "testMethod";

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + TEST_TOKEN);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void checkRole_ValidUserWithRequiredRole() throws Throwable {
        RequireRole requireRole = createRequireRoleAnnotation(UserRole.USER);
        User user = createUser(UserRole.USER);
        Object expectedResult = new Object();

        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(true);
        when(tokenProvider.isAnonymousToken(TEST_TOKEN)).thenReturn(false);
        when(tokenProvider.getEmailFromToken(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(joinPoint.proceed()).thenReturn(expectedResult);

        Object result = roleCheckAspect.checkRole(joinPoint, requireRole);

        assertEquals(expectedResult, result);
        verify(joinPoint).proceed();
    }

    @Test
    void checkRole_ValidUserWithAdminRole() throws Throwable {
        RequireRole requireRole = createRequireRoleAnnotation(UserRole.ADMIN);
        User user = createUser(UserRole.ADMIN);
        Object expectedResult = new Object();

        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(true);
        when(tokenProvider.isAnonymousToken(TEST_TOKEN)).thenReturn(false);
        when(tokenProvider.getEmailFromToken(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(joinPoint.proceed()).thenReturn(expectedResult);

        Object result = roleCheckAspect.checkRole(joinPoint, requireRole);

        assertEquals(expectedResult, result);
        verify(joinPoint).proceed();
    }

    @Test
    void checkRole_ValidAnonymousUser() throws Throwable {
        RequireRole requireRole = createRequireRoleAnnotation(UserRole.ANONYMOUS);
        Object expectedResult = new Object();

        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(true);
        when(tokenProvider.isAnonymousToken(TEST_TOKEN)).thenReturn(true);
        when(joinPoint.proceed()).thenReturn(expectedResult);

        Object result = roleCheckAspect.checkRole(joinPoint, requireRole);

        assertEquals(expectedResult, result);
        verify(joinPoint).proceed();
    }

    @Test
    void checkRole_AnonymousUserDenied() {
        RequireRole requireRole = createRequireRoleAnnotation(new UserRole[]{UserRole.ANONYMOUS}, true);

        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(true);
        when(tokenProvider.isAnonymousToken(TEST_TOKEN)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> roleCheckAspect.checkRole(joinPoint, requireRole));
        assertEquals(403, exception.getStatusCode().value());
        assertEquals(requireRole.anonymousMessage(), exception.getReason());
    }

    @Test
    void checkRole_InvalidToken() {
        RequireRole requireRole = createRequireRoleAnnotation(UserRole.USER);

        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> roleCheckAspect.checkRole(joinPoint, requireRole));
        assertEquals(401, exception.getStatusCode().value());
        assertEquals("Недействительный токен", exception.getReason());
    }

    @Test
    void checkRole_UserNotFound() {
        RequireRole requireRole = createRequireRoleAnnotation(UserRole.USER);

        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(true);
        when(tokenProvider.isAnonymousToken(TEST_TOKEN)).thenReturn(false);
        when(tokenProvider.getEmailFromToken(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> roleCheckAspect.checkRole(joinPoint, requireRole));
        assertEquals(401, exception.getStatusCode().value());
        assertEquals("Пользователь не найден", exception.getReason());
    }

    @Test
    void checkRole_InsufficientRole() {
        RequireRole requireRole = createRequireRoleAnnotation(UserRole.ADMIN);
        User user = createUser(UserRole.USER);

        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(true);
        when(tokenProvider.isAnonymousToken(TEST_TOKEN)).thenReturn(false);
        when(tokenProvider.getEmailFromToken(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> roleCheckAspect.checkRole(joinPoint, requireRole));
        assertEquals(403, exception.getStatusCode().value());
        assertEquals("Недостаточно прав доступа", exception.getReason());
    }

    @Test
    void checkRole_NoToken() {
        RequireRole requireRole = createRequireRoleAnnotation(UserRole.USER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> roleCheckAspect.checkRole(joinPoint, requireRole));
        assertEquals(401, exception.getStatusCode().value());
        assertEquals("Токен не предоставлен", exception.getReason());
    }

    @Test
    void checkRole_NoRequestContext() {
        RequireRole requireRole = createRequireRoleAnnotation(UserRole.USER);
        RequestContextHolder.resetRequestAttributes();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> roleCheckAspect.checkRole(joinPoint, requireRole));
        assertEquals(401, exception.getStatusCode().value());
        assertEquals("Невозможно получить контекст запроса", exception.getReason());
    }

    private RequireRole createRequireRoleAnnotation(UserRole... roles) {
        return createRequireRoleAnnotation(roles, false);
    }

    private RequireRole createRequireRoleAnnotation(UserRole[] roles, boolean denyAnonymous) {
        return new RequireRole() {
            @Override
            public UserRole[] value() {
                return roles;
            }

            @Override
            public boolean denyAnonymous() {
                return denyAnonymous;
            }

            @Override
            public String anonymousMessage() {
                return "Для выполнения этого действия необходимо зарегистрироваться";
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return RequireRole.class;
            }
        };
    }

    private User createUser(UserRole role) {
        User user = new User();
        user.setEmail(TEST_EMAIL);
        user.setRole(role);
        return user;
    }
} 