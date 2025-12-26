package com.stocat.authapi.service;

import com.stocat.authapi.exception.AuthErrorCode;
import com.stocat.authapi.service.dto.UserDto;
import com.stocat.auth.mysql.domain.user.domain.AuthProvider;
import com.stocat.auth.mysql.domain.user.domain.UsersEntity;
import com.stocat.auth.mysql.domain.user.domain.UserRole;
import com.stocat.auth.mysql.domain.user.domain.UserStatus;
import com.stocat.auth.mysql.domain.user.repository.UserRepository;
import com.stocat.auth.core.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new UserQueryService(userRepository);
    }

    @Test
    void 이메일로_조회하면_DTO로_변환한다() {
        UsersEntity user = UsersEntity.builder()
                .id(1L)
                .nickname("고냥이")
                .email("cat@stocat.com")
                .password("encoded")
                .provider(AuthProvider.LOCAL)
                .providerId("pid")
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();
        when(userRepository.findByEmail("cat@stocat.com")).thenReturn(Optional.of(user));

        UserDto dto = queryService.getUserByEmail("cat@stocat.com");

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.email()).isEqualTo("cat@stocat.com");
        assertThat(dto.password()).isEqualTo("encoded");
    }

    @Test
    void 존재하지_않는_회원이면_예외를_던진다() {
        when(userRepository.findByEmail("cat@stocat.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.getUserByEmail("cat@stocat.com"))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.USER_NOT_FOUND);
    }
}
