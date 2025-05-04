package ru.putevod.app.planner.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.planner.client.AuthServiceClient;
import ru.putevod.app.planner.dto.UserDto;
import ru.putevod.app.planner.exception.ResourceNotFoundException;
import ru.putevod.app.planner.mapper.UserMapper;
import ru.putevod.app.planner.model.User;
import ru.putevod.app.planner.repository.UserRepository;
import ru.putevod.app.planner.service.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthServiceClient authServiceClient;
    
    @Value("${auth.token}")
    private String serviceToken;

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        // Сначала пробуем получить из локальной БД
        if (userRepository.existsById(userId)) {
            User user = getUserEntityById(userId);
            return userMapper.toDto(user);
        }
        
        // Если пользователь не найден локально, запрашиваем из сервиса auth
        UserDto userDto = authServiceClient.getUserInfo(userId, serviceToken);
        
        // Создаем локальную копию, если нужно
        if (userDto != null && !userRepository.existsById(userId)) {
            saveLocalUserCopy(userDto);
        }
        
        return userDto;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "username", username));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "email", email));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUserProfile(Long userId, UserDto userDto) {
        // Проверяем существование пользователя в сервисе auth
        authServiceClient.getUserInfo(userId, serviceToken);
        
        User user = getUserEntityById(userId);
        
        // Обновляем локальные данные
        userMapper.updateEntityFromDto(userDto, user);
        user = userRepository.save(user);
        
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserEntityById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь", "id", userId));
    }
    
    /**
     * Создает локальную копию пользователя из сервиса auth
     * @param userDto данные пользователя
     * @return сохраненная сущность
     */
    private User saveLocalUserCopy(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        return userRepository.save(user);
    }
} 