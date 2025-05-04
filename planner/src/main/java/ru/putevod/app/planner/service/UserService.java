package ru.putevod.app.planner.service;

import ru.putevod.app.planner.dto.UserDto;
import ru.putevod.app.planner.model.User;

import java.util.Optional;

public interface UserService {
    
    UserDto getUserById(Long userId);
    
    UserDto findByUsername(String username);
    
    UserDto findByEmail(String email);
    
    UserDto updateUserProfile(Long userId, UserDto userDto);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    User getUserEntityById(Long userId);
} 