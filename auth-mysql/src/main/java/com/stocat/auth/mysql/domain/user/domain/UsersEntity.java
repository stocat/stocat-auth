package com.stocat.auth.mysql.domain.user.domain;

import com.stocat.auth.mysql.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(name = "users")
@Entity
public class UsersEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nickname;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(name = "password", length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public static UsersEntity create(String nickname, String email, String password, AuthProvider provider, String providerId, UserStatus status, UserRole role) {
        return UsersEntity.builder()
                .nickname(nickname)
                .email(email)
                .password(password)
                .provider(provider)
                .providerId(providerId)
                .status(status)
                .role(role)
                .build();
    }

    public void markLoggedInNow() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void changeStatus(UserStatus status) {
        this.status = status;
    }
}

