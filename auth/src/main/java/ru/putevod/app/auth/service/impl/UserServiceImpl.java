package ru.putevod.app.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.auth.dto.UserInfoDto;
import ru.putevod.app.auth.model.User;
import ru.putevod.app.auth.repository.UserRepository;
import ru.putevod.app.auth.service.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public User updateLastLogin(User user) {
        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public UserInfoDto mapToUserInfoDto(User user) {
        return UserInfoDto.builder()
                .id(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .emailVerified(Boolean.TRUE.equals(user.getIsVerified()))
                .createdAt(user.getCreatedAt())
                .build();
    }
} 