package com.stocat.authapi.service;

import com.stocat.authapi.exception.AuthErrorCode;
import com.stocat.authapi.service.dto.UserDto;
import com.stocat.auth.mysql.domain.user.domain.UsersEntity;
import com.stocat.auth.mysql.domain.user.repository.UserRepository;
import com.stocat.auth.core.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    public Optional<UsersEntity> getById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<UsersEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    // Exception-throwing getters for simpler read use-cases
    public UsersEntity getByIdOrThrow(Long id) {
        return getById(id).orElseThrow(() -> new ApiException(AuthErrorCode.USER_NOT_FOUND));
    }

    public UsersEntity getByEmailOrThrow(String email) {
        return findByEmail(email).orElseThrow(() -> new ApiException(AuthErrorCode.USER_NOT_FOUND));
    }

    // View mappers to detach persistence concerns
    public UserDto getUserById(Long id) {
        UsersEntity user = getByIdOrThrow(id);
        return UserDto.from(user);
    }

    public UserDto getUserByEmail(String email) {
        UsersEntity user = getByEmailOrThrow(email);
        return UserDto.from(user);
    }
}
