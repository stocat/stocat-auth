package com.stocat.authapi.service;

import com.stocat.authapi.exception.AuthErrorCode;
import com.stocat.auth.mysql.domain.user.domain.AuthProvider;
import com.stocat.auth.mysql.domain.user.domain.UsersEntity;
import com.stocat.auth.mysql.domain.user.domain.UserRole;
import com.stocat.auth.mysql.domain.user.domain.UserStatus;
import com.stocat.auth.mysql.domain.user.repository.UserRepository;
import com.stocat.auth.core.exception.ApiException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UsersEntity createLocalUser(@NonNull String nickname,
                                       @NonNull String email,
                                       @NonNull String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new ApiException(AuthErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        String encoded = passwordEncoder.encode(rawPassword);
        UsersEntity user = UsersEntity.create(
                nickname,
                email,
                encoded,
                AuthProvider.LOCAL,
                "",
                UserStatus.ACTIVE,
                UserRole.USER
        );
        return userRepository.save(user);
    }

    public void markLoginAt(@NonNull Long userId) {
        UsersEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(AuthErrorCode.USER_NOT_FOUND));
        user.markLoggedInNow();
    }

    // Update: status change
    public void changeStatus(@NonNull Long userId, @NonNull UserStatus status) {
        UsersEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(AuthErrorCode.USER_NOT_FOUND));
        user.changeStatus(status);
        userRepository.save(user);
    }

    // Delete
    public void delete(@NonNull Long userId) {
        userRepository.deleteById(userId);
    }
}
