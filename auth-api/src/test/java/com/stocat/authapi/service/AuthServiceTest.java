package com.stocat.authapi.service;

import com.stocat.authapi.security.jwt.JwtClaimKeys;
import com.stocat.authapi.security.jwt.JwtProvider;
import com.stocat.authapi.controller.dto.AuthResponse;
import com.stocat.authapi.controller.dto.LoginRequest;
import com.stocat.authapi.controller.dto.SignupRequest;
import com.stocat.authapi.exception.AuthErrorCode;
import com.stocat.authapi.service.dto.*;
import com.stocat.auth.mysql.domain.user.domain.AuthProvider;
import com.stocat.auth.mysql.domain.user.domain.UserRole;
import com.stocat.auth.mysql.domain.user.domain.UserStatus;
import com.stocat.auth.core.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserCommandService commandService;
    @Mock
    private UserQueryService queryService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(commandService, queryService, passwordEncoder, jwtProvider);
    }

    @Test
    void 회원가입은_CommandService에_위임한다() {
        SignupRequest request = new SignupRequest("고냥이", "cat@stocat.com", "plain");

        authService.signup(request);

        verify(commandService, times(1))
                .createLocalUser(eq("고냥이"), eq("cat@stocat.com"), eq("plain"));
        verifyNoInteractions(queryService);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void 회원가입_예외는_CommandService에서_던진다() {
        SignupRequest request = new SignupRequest("고냥이", "cat@stocat.com", "plain");
        doThrow(new ApiException(AuthErrorCode.EMAIL_ALREADY_EXISTS))
                .when(commandService).createLocalUser(anyString(), anyString(), anyString());

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    void 로그인은_비밀번호가_일치하면_토큰을_발급한다() {
        LoginRequest request = new LoginRequest("cat@stocat.com", "plain");
        UserDto user = userDto(1L, request.email(), "encoded", UserRole.USER);
        when(queryService.getUserByEmail(request.email())).thenReturn(user);
        when(passwordEncoder.matches("plain", "encoded")).thenReturn(true);
        when(jwtProvider.createAccessToken(anyString(), anyMap())).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(anyString())).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(commandService).markLoginAt(1L);

        ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(jwtProvider).createAccessToken(eq("1"), claimsCaptor.capture());
        assertThat(claimsCaptor.getValue()).containsEntry(JwtClaimKeys.EMAIL, request.email());
        assertThat(claimsCaptor.getValue()).containsEntry(JwtClaimKeys.ROLE, UserRole.USER.name());
        verify(jwtProvider).createRefreshToken("1");
    }

    @Test
    void 로그인시_비밀번호가_틀리면_예외를_던진다() {
        LoginRequest request = new LoginRequest("cat@stocat.com", "plain");
        UserDto user = userDto(1L, request.email(), "encoded", UserRole.USER);
        when(queryService.getUserByEmail(request.email())).thenReturn(user);
        when(passwordEncoder.matches("plain", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_CREDENTIALS);
        verify(commandService, never()).markLoginAt(anyLong());
        verify(jwtProvider, never()).createAccessToken(any(), anyMap());
    }

    @Test
    void 로그인시_DB비밀번호가_null이면_예외를_던진다() {
        LoginRequest request = new LoginRequest("cat@stocat.com", "plain");
        UserDto user = userDto(1L, request.email(), null, UserRole.USER);
        when(queryService.getUserByEmail(request.email())).thenReturn(user);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_CREDENTIALS);
    }

    private UserDto userDto(Long id, String email, String password, UserRole role) {
        return new UserDto(
                id,
                "고냥이",
                email,
                password,
                AuthProvider.LOCAL,
                "",
                UserStatus.ACTIVE,
                role,
                null,
                null,
                null,
                null
        );
    }
}
