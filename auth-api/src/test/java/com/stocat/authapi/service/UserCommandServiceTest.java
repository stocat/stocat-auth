package com.stocat.authapi.service;

import com.stocat.auth.core.exception.ApiException;
import com.stocat.auth.mysql.domain.user.domain.AuthProvider;
import com.stocat.auth.mysql.domain.user.domain.UserRole;
import com.stocat.auth.mysql.domain.user.domain.UserStatus;
import com.stocat.auth.mysql.domain.user.domain.UsersEntity;
import com.stocat.auth.mysql.domain.user.repository.UserRepository;
import com.stocat.authapi.exception.AuthErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @InjectMocks
    private UserCommandService userCommandService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("로컬 유저 생성 성공")
    void createLocalUser_success() {
        // given
        String nickname = "testUser";
        String email = "test@example.com";
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";

        given(userRepository.existsByEmail(email)).willReturn(false);
        given(userRepository.existsByNickname(nickname)).willReturn(false);
        given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);
        given(userRepository.save(any(UsersEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        UsersEntity result = userCommandService.createLocalUser(nickname, email, rawPassword);

        // then
        assertThat(result.getNickname()).isEqualTo(nickname);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getPassword()).isEqualTo(encodedPassword);
        assertThat(result.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("로컬 유저 생성 실패 - 이메일 중복")
    void createLocalUser_fail_email_exists() {
        // given
        String nickname = "testUser";
        String email = "test@example.com";
        String rawPassword = "password";

        given(userRepository.existsByEmail(email)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userCommandService.createLocalUser(nickname, email, rawPassword))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("로컬 유저 생성 실패 - 닉네임 중복")
    void createLocalUser_fail_nickname_exists() {
        // given
        String nickname = "testUser";
        String email = "test@example.com";
        String rawPassword = "password";

        given(userRepository.existsByEmail(email)).willReturn(false);
        given(userRepository.existsByNickname(nickname)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userCommandService.createLocalUser(nickname, email, rawPassword))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.NICKNAME_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("로그인 시간 갱신 성공")
    void markLoginAt_success() {
        // given
        Long userId = 1L;
        UsersEntity user = UsersEntity.create("test", "test@test.com", "pw", AuthProvider.LOCAL, "", UserStatus.ACTIVE, UserRole.USER);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userCommandService.markLoginAt(userId);

        // then
        assertThat(user.getLastLoginAt()).isNotNull();
    }

    @Test
    @DisplayName("로그인 시간 갱신 실패 - 유저 없음")
    void markLoginAt_fail_user_not_found() {
        // given
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userCommandService.markLoginAt(userId))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.USER_NOT_FOUND);
    }
}
