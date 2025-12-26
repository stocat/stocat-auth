package com.stocat.authapi.service;

import com.stocat.authapi.exception.AuthErrorCode;
import com.stocat.auth.mysql.domain.user.domain.UsersEntity;
import com.stocat.auth.mysql.domain.user.repository.UserRepository;
import com.stocat.auth.core.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    UserCommandService service;

    @BeforeEach
    void setUp() {
        service = new UserCommandService(userRepository, passwordEncoder);
    }

    @Test
    void 로컬회원_생성시_중복이메일이면_예외() {
        when(userRepository.existsByEmail("cat@stocat.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createLocalUser("고냥이", "cat@stocat.com", "plain"))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.EMAIL_ALREADY_EXISTS);
        verify(userRepository, never()).save(any());
    }

    @Test
    void 로컬회원_생성시_중복닉네임이면_예외() {
        when(userRepository.existsByEmail("cat@stocat.com")).thenReturn(false);
        when(userRepository.existsByNickname("고냥이")).thenReturn(true);

        assertThatThrownBy(() -> service.createLocalUser("고냥이", "cat@stocat.com", "plain"))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.NICKNAME_ALREADY_EXISTS);
        verify(userRepository, never()).save(any());
    }

    @Test
    void 로컬회원_생성시_비밀번호를_인코딩하여_저장() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByNickname(anyString())).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        // echo-back saved entity
        when(userRepository.save(any(UsersEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsersEntity saved = service.createLocalUser("고냥이", "cat@stocat.com", "plain");

        assertThat(saved.getPassword()).isEqualTo("encoded");
        verify(passwordEncoder).encode("plain");
        verify(userRepository).save(any(UsersEntity.class));
    }
}
