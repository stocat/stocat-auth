package com.stocat.auth.mysql.domain.user.repository;

import com.stocat.auth.mysql.domain.user.domain.AuthProvider;
import com.stocat.auth.mysql.domain.user.domain.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UsersEntity, Long> {

    Optional<UsersEntity> findByEmail(String email);

    Optional<UsersEntity> findByProviderAndProviderId(AuthProvider provider, String providerId);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}

