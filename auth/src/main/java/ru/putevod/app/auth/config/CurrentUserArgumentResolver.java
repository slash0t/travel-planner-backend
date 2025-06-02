package ru.putevod.app.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import ru.putevod.app.auth.security.JwtTokenProvider;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) &&
               parameter.getParameterType().equals(Integer.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, 
                                ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, 
                                WebDataBinderFactory binderFactory) throws Exception {
        
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Отсутствует или неверный Authorization header");
            return null;
        }
        
        String token = authHeader.substring(7);
        
        try {
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("Неверный JWT токен");
                return null;
            }
            
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            return userId != null ? userId.intValue() : null;
        } catch (Exception e) {
            log.error("Ошибка при извлечении пользователя из токена: {}", e.getMessage());
            return null;
        }
    }
} 