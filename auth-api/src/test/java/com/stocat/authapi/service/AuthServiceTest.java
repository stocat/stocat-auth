package com.stocat.authapi.service;

import com.stocat.auth.core.exception.ApiException;
import com.stocat.auth.mysql.domain.user.domain.UserRole;
import com.stocat.authapi.controller.dto.AuthResponse;
import com.stocat.authapi.controller.dto.LoginRequest;
import com.stocat.authapi.controller.dto.SignupRequest;
import com.stocat.authapi.controller.dto.UserSummaryResponse;
import com.stocat.authapi.exception.AuthErrorCode;
import com.stocat.authapi.security.jwt.JwtProvider;
import com.stocat.authapi.service.dto.UserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserCommandService commandService;

    @Mock
    private UserQueryService queryService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // given
        SignupRequest request = new SignupRequest("nickname", "test@example.com", "password");

        // when
        authService.signup(request);

        // then
        verify(commandService).createLocalUser("nickname", "test@example.com", "password");
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "password");
        UserDto userDto = new UserDto(
                1L, "nickname", "test@example.com", "encodedPassword",
                null, null, null, UserRole.USER,
                null, null, null, null
        );

        given(queryService.getUserByEmail(request.email())).willReturn(userDto);
        given(passwordEncoder.matches(request.password(), userDto.password())).willReturn(true);
        given(jwtProvider.createAccessToken(anyString(), any(Map.class))).willReturn("access");
        given(jwtProvider.createRefreshToken(anyString())).willReturn("refresh");

        // when
        AuthResponse response = authService.login(request);

        // then
        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        verify(commandService).markLoginAt(1L);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_invalid_password() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");
        UserDto userDto = new UserDto(
                1L, "nickname", "test@example.com", "encodedPassword",
                null, null, null, UserRole.USER,
                null, null, null, null
        );

        given(queryService.getUserByEmail(request.email())).willReturn(userDto);
        given(passwordEncoder.matches(request.password(), userDto.password())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("내 정보 요약 조회 성공")
    void getSummary_success() {
        // given
        Long userId = 1L;
        UserDto userDto = new UserDto(
                userId, "nickname", "test@example.com", "encodedPassword",
                null, null, null, UserRole.USER,
                null, null, null, null
        );

        given(queryService.getUserById(userId)).willReturn(userDto);

        // when
        UserSummaryResponse response = authService.getSummary(userId);

        // then
        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.nickname()).isEqualTo("nickname");
    }
}
