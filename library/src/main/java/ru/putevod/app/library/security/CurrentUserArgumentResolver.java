package ru.putevod.app.library.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import ru.putevod.app.library.client.AuthServiceClient;
import ru.putevod.app.library.client.AuthServiceClient.UserInfo;

@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final AuthServiceClient authServiceClient;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) 
            && (parameter.getParameterType().equals(Long.class) || 
                parameter.getParameterType().equals(UserInfo.class));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                               NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        String token = (String) authentication.getCredentials();
        if (token == null) {
            return null;
        }
        
        UserInfo userInfo = authServiceClient.getUserInfo(token);
        if (userInfo == null) {
            return null;
        }
        
        CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
        
        if (parameter.getParameterType().equals(Long.class)) {
            return userInfo.userId();
        } else if (parameter.getParameterType().equals(UserInfo.class) && annotation.info()) {
            return userInfo;
        }
        
        return null;
    }
} 