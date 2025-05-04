package ru.putevod.app.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {

    private Integer id;
    private String email;
    private String username;
    private String avatarUrl;
    private boolean emailVerified;
    private LocalDateTime createdAt;
} 