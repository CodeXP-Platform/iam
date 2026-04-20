package com.codexp.iam.repository;

import com.codexp.iam.entity.AuthProvider;
import com.codexp.iam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    /**
     * Usado para detectar colisión OAuth:
     * si el email existe con provider=EMAIL, bloqueamos el flujo OAuth.
     */
    Optional<User> findByEmailAndAuthProvider(String email, AuthProvider authProvider);
}