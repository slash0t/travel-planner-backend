package ru.putevod.app.planner.service;

import ru.putevod.app.planner.dto.UserDto;
import ru.putevod.app.planner.model.User;

public interface UserService {

    UserDto getUserById(Long userId);

    UserDto findByUsername(String username);

    UserDto findByEmail(String email);

    User getUserEntityById(Long userId);
} 