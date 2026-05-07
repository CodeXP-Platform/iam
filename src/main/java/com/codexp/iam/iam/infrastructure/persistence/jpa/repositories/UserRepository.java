package com.codexp.iam.iam.infrastructure.persistence.jpa.repositories;

import com.codexp.iam.iam.domain.model.entities.User;
import com.codexp.iam.iam.domain.model.valueobjects.AuthProvider;
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
     * Used to detect OAuth/EMAIL collision:
     * if the email exists with provider=EMAIL we block the OAuth flow.
     */
    Optional<User> findByEmailAndAuthProvider(String email, AuthProvider authProvider);
}
