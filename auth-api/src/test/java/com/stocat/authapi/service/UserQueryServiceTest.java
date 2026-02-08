package com.stocat.authapi.service;

import com.stocat.auth.core.exception.ApiException;
import com.stocat.auth.mysql.domain.user.domain.AuthProvider;
import com.stocat.auth.mysql.domain.user.domain.UserRole;
import com.stocat.auth.mysql.domain.user.domain.UserStatus;
import com.stocat.auth.mysql.domain.user.domain.UsersEntity;
import com.stocat.auth.mysql.domain.user.repository.UserRepository;
import com.stocat.authapi.exception.AuthErrorCode;
import com.stocat.authapi.service.dto.UserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @InjectMocks
    private UserQueryService userQueryService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("ID로 유저 조회 성공")
    void getByIdOrThrow_success() {
        // given
        Long userId = 1L;
        UsersEntity user = UsersEntity.create("test", "test@test.com", "pw", AuthProvider.LOCAL, "", UserStatus.ACTIVE, UserRole.USER);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        UsersEntity result = userQueryService.getByIdOrThrow(userId);

        // then
        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("ID로 유저 조회 실패 - 유저 없음")
    void getByIdOrThrow_fail_user_not_found() {
        // given
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userQueryService.getByIdOrThrow(userId))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("이메일로 유저 조회 성공")
    void getByEmailOrThrow_success() {
        // given
        String email = "test@test.com";
        UsersEntity user = UsersEntity.create("test", email, "pw", AuthProvider.LOCAL, "", UserStatus.ACTIVE, UserRole.USER);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when
        UsersEntity result = userQueryService.getByEmailOrThrow(email);

        // then
        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("이메일로 유저 조회 실패 - 유저 없음")
    void getByEmailOrThrow_fail_user_not_found() {
        // given
        String email = "test@test.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userQueryService.getByEmailOrThrow(email))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("ID로 UserDto 조회 성공")
    void getUserById_success() {
        // given
        Long userId = 1L;
        UsersEntity user = UsersEntity.create("test", "test@test.com", "pw", AuthProvider.LOCAL, "", UserStatus.ACTIVE, UserRole.USER);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        UserDto result = userQueryService.getUserById(userId);

        // then
        assertThat(result.email()).isEqualTo(user.getEmail());
        assertThat(result.nickname()).isEqualTo(user.getNickname());
    }
}
