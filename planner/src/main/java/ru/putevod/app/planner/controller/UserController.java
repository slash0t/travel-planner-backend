package ru.putevod.app.planner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.putevod.app.planner.config.CurrentUser;
import ru.putevod.app.planner.dto.UserDto;
import ru.putevod.app.planner.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API для управления пользователями")
public class UserController {
    private final UserService userService;
    
    @GetMapping("/me")
    @Operation(summary = "Получить информацию о текущем пользователе")
    public ResponseEntity<UserDto> getCurrentUser(
            @CurrentUser Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "Получить пользователя по ID")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }
    
    @GetMapping("/by-username/{username}")
    @Operation(summary = "Найти пользователя по логину")
    public ResponseEntity<UserDto> findByUsername(
            @PathVariable String username) {
        return ResponseEntity.ok(userService.findByUsername(username));
    }
    
    @GetMapping("/by-email/{email}")
    @Operation(summary = "Найти пользователя по email")
    public ResponseEntity<UserDto> findByEmail(
            @PathVariable String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }
    
    @PutMapping("/me")
    @Operation(summary = "Обновить профиль текущего пользователя")
    public ResponseEntity<UserDto> updateUserProfile(
            @CurrentUser Long userId,
            @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.updateUserProfile(userId, userDto));
    }
} 