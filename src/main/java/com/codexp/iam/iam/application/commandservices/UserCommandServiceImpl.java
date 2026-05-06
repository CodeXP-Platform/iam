package com.codexp.iam.iam.application.commandservices;

import com.codexp.iam.iam.domain.events.UserDeletedEvent;
import com.codexp.iam.iam.domain.events.UserProfileUpdatedEvent;
import com.codexp.iam.iam.domain.events.UserRegisteredEvent;
import com.codexp.iam.iam.domain.model.commands.*;
import com.codexp.iam.iam.domain.model.entities.User;
import com.codexp.iam.iam.domain.model.valueobjects.AuthProvider;
import com.codexp.iam.iam.domain.model.valueobjects.AuthResult;
import com.codexp.iam.iam.domain.model.valueobjects.Role;
import com.codexp.iam.iam.domain.services.UserCommandService;
import com.codexp.iam.iam.infrastructure.messaging.DomainEventPublisher;
import com.codexp.iam.iam.infrastructure.oauth.OAuthProviderService;
import com.codexp.iam.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.codexp.iam.iam.infrastructure.security.JwtUtil;
import com.codexp.iam.iam.infrastructure.security.RefreshTokenService;
import com.codexp.iam.shared.exceptions.AuthProviderConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;
    private final JwtUtil              jwtUtil;
    private final RefreshTokenService  refreshTokenService;
    private final DomainEventPublisher eventPublisher;
    private final OAuthProviderService oAuthProviderService;

    // ── Sign Up ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResult signUp(SignUpCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        if (userRepository.existsByNickname(command.nickname())) {
            throw new IllegalArgumentException("El nickname ya está en uso");
        }

        User user = User.builder()
                .email(command.email())
                .nickname(command.nickname())
                .username(command.nickname())
                .password(passwordEncoder.encode(command.password()))
                .picture("")
                .role(Role.ROLE_STUDENT)
                .authProvider(AuthProvider.EMAIL)
                .build();

        userRepository.save(user);
        log.info("Usuario registrado: {}", user.getEmail());

        eventPublisher.publishUserRegistered(
                UserRegisteredEvent.of(user.getId(), user.getEmail(), user.getNickname(), user.getRole().name())
        );

        return buildAuthResult(user);
    }

    // ── Sign In ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AuthResult signIn(SignInCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (user.getAuthProvider() != AuthProvider.EMAIL) {
            throw new AuthProviderConflictException(
                    "Esta cuenta usa " + user.getAuthProvider() + ". Inicia sesión con ese proveedor."
            );
        }

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        log.info("Login exitoso: {}", user.getEmail());
        return buildAuthResult(user);
    }

    // ── OAuth ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResult processOAuth(OAuthCommand command) {
        OAuthProviderService.OAuthProfile profile =
                oAuthProviderService.fetchProfile(command.provider(), command.providerToken());

        userRepository.findByEmailAndAuthProvider(profile.email(), AuthProvider.EMAIL)
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Este email ya está registrado con contraseña. " +
                                    "Por favor inicia sesión con tu email y contraseña."
                    );
                });

        User user = userRepository.findByEmail(profile.email())
                .orElseGet(() -> {
                    String nickname = resolveNickname(command.nickname(), profile.email());
                    User newUser = User.builder()
                            .email(profile.email())
                            .nickname(nickname)
                            .username(profile.name() != null ? profile.name() : nickname)
                            .picture(profile.picture() != null ? profile.picture() : "")
                            .role(Role.ROLE_STUDENT)
                            .authProvider(AuthProvider.OAUTH)
                            .build();
                    userRepository.save(newUser);
                    log.info("Usuario OAuth creado: {}", newUser.getEmail());
                    eventPublisher.publishUserRegistered(
                            UserRegisteredEvent.of(newUser.getId(), newUser.getEmail(),
                                    newUser.getNickname(), newUser.getRole().name())
                    );
                    return newUser;
                });

        return buildAuthResult(user);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AuthResult refresh(RefreshTokenCommand command) {
        String token = command.refreshToken();

        if (!jwtUtil.isRefreshTokenValid(token)) {
            throw new IllegalArgumentException("Refresh token inválido o expirado");
        }

        UUID userId = UUID.fromString(jwtUtil.getSubjectFromRefreshToken(token));

        if (!refreshTokenService.isValid(userId, token)) {
            throw new IllegalArgumentException("Refresh token no reconocido. Inicia sesión nuevamente.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        refreshTokenService.revoke(userId);
        return buildAuthResult(user);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Override
    public void logout(LogoutCommand command) {
        if (command.accessToken() != null && jwtUtil.isAccessTokenValid(command.accessToken())) {
            UUID userId = jwtUtil.extractUserId(command.accessToken());
            refreshTokenService.revoke(userId);
            log.info("Logout para userId={}", userId);
        }
    }

    // ── Update Profile ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public User updateProfile(UpdateProfileCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (command.nickname() != null && !command.nickname().isBlank()) {
            boolean conflict = userRepository.existsByNickname(command.nickname())
                    && !user.getNickname().equals(command.nickname());
            if (conflict) {
                throw new IllegalArgumentException("El nickname ya está en uso");
            }
            user.setNickname(command.nickname());
        }

        if (command.picture() != null) {
            user.setPicture(command.picture());
        }

        userRepository.save(user);
        log.info("Perfil actualizado para userId={}", command.userId());

        eventPublisher.publishUserProfileUpdated(
                UserProfileUpdatedEvent.of(user.getId(), user.getNickname(), user.getPicture())
        );

        return user;
    }

    // ── Delete Account ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteAccount(DeleteAccountCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        refreshTokenService.revoke(command.userId());

        eventPublisher.publishUserDeleted(
                UserDeletedEvent.of(user.getId(), user.getEmail())
        );

        userRepository.delete(user);
        log.info("Cuenta eliminada para userId={}", command.userId());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AuthResult buildAuthResult(User user) {
        String accessToken  = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        refreshTokenService.save(user.getId(), refreshToken);
        return AuthResult.of(user, accessToken, refreshToken);
    }

    private String resolveNickname(String requested, String email) {
        if (requested != null && !requested.isBlank()) {
            return userRepository.existsByNickname(requested)
                    ? requested + "_" + UUID.randomUUID().toString().substring(0, 5)
                    : requested;
        }
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
        return userRepository.existsByNickname(base)
                ? base + "_" + UUID.randomUUID().toString().substring(0, 5)
                : base;
    }
}
