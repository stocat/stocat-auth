package com.stocat.authapi.service;

import com.stocat.authapi.controller.dto.UserSummaryResponse;
import com.stocat.authapi.security.jwt.JwtClaimKeys;
import com.stocat.authapi.security.jwt.JwtProvider;
import com.stocat.authapi.controller.dto.AuthResponse;
import com.stocat.authapi.controller.dto.LoginRequest;
import com.stocat.authapi.controller.dto.SignupRequest;
import com.stocat.authapi.exception.AuthErrorCode;
import com.stocat.authapi.service.dto.UserDto;
import com.stocat.auth.core.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserCommandService commandService;
    private final UserQueryService queryService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * Use case: signup
     *
     * @param request 회원가입 req
     */
    public void signup(SignupRequest request) {
        commandService.createLocalUser(request.nickname(), request.email(), request.password());
    }

    /**
     * Use case: login
     *
     * @param request 로그인 req
     * @return AuthResponse(유저 토큰)
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserDto user = queryService.getUserByEmail(request.email());

        if (user.password() == null || !passwordEncoder.matches(request.password(), user.password())) {
            throw new ApiException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        commandService.markLoginAt(user.id());

        String access = jwtProvider.createAccessToken(
                String.valueOf(user.id()),
                Map.of(
                        JwtClaimKeys.USER_ID, user.id(),
                        JwtClaimKeys.EMAIL, user.email(),
                        JwtClaimKeys.ROLE, user.role().name()
                )
        );
        String refresh = jwtProvider.createRefreshToken(String.valueOf(user.id()));
        return new AuthResponse(access, refresh);
    }

    /**
     * Use case: get user summary
     *
     * @param userId 유저 ID
     * @return UserSummaryResponse
     */
    public UserSummaryResponse getSummary(Long userId) {
        UserDto user = queryService.getUserById(userId);
        return UserSummaryResponse.from(user);
    }
}
