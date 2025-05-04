package ru.putevod.app.auth.service;

import ru.putevod.app.auth.dto.UserInfoDto;
import ru.putevod.app.auth.model.User;

import java.util.Optional;

public interface UserService {

    /**
     * Находит пользователя по email
     * 
     * @param email Email пользователя
     * @return Optional с найденным пользователем или пустой
     */
    Optional<User> findByEmail(String email);

    /**
     * Находит пользователя по username
     * 
     * @param username Username пользователя
     * @return Optional с найденным пользователем или пустой
     */
    Optional<User> findByUsername(String username);

    /**
     * Преобразует сущность User в DTO для передачи клиенту
     * 
     * @param user Сущность пользователя
     * @return DTO с информацией о пользователе
     */
    UserInfoDto mapToUserInfoDto(User user);

    /**
     * Обновляет время последнего входа пользователя
     * 
     * @param user Пользователь, для которого нужно обновить время
     * @return Обновленный пользователь
     */
    User updateLastLogin(User user);
} 