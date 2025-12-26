package com.stocat.authapi.service.dto;

import com.stocat.auth.mysql.domain.user.domain.AuthProvider;
import com.stocat.auth.mysql.domain.user.domain.UsersEntity;
import com.stocat.auth.mysql.domain.user.domain.UserRole;
import com.stocat.auth.mysql.domain.user.domain.UserStatus;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String nickname,
        String email,
        String password,
        AuthProvider provider,
        String providerId,
        UserStatus status,
        UserRole role,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
    public static UserDto from(UsersEntity m) {
        return new UserDto(
                m.getId(),
                m.getNickname(),
                m.getEmail(),
                m.getPassword(),
                m.getProvider(),
                m.getProviderId(),
                m.getStatus(),
                m.getRole(),
                m.getLastLoginAt(),
                m.getCreatedAt(),
                m.getUpdatedAt(),
                m.getDeletedAt()
        );
    }
}
