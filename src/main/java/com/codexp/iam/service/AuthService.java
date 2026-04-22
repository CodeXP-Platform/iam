package com.codexp.iam.service;

import com.codexp.iam.dto.request.OAuthRequest;
import com.codexp.iam.dto.request.RefreshTokenRequest;
import com.codexp.iam.dto.request.SignInRequest;
import com.codexp.iam.dto.request.SignUpRequest;
import com.codexp.iam.dto.response.AuthResponse;
import com.codexp.iam.dto.response.UserProfileResponse;
import com.codexp.iam.entity.AuthProvider;
import com.codexp.iam.entity.Role;
import com.codexp.iam.entity.User;
import com.codexp.iam.event.UserRegisteredEvent;
import com.codexp.iam.exception.AuthProviderConflictException;
import com.codexp.iam.infrastructure.messaging.DomainEventPublisher;
import com.codexp.iam.infrastructure.security.JwtUtil;
import com.codexp.iam.infrastructure.security.RefreshTokenService;
import com.codexp.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;
    private final JwtUtil              jwtUtil;
    private final RefreshTokenService  refreshTokenService;
    private final DomainEventPublisher eventPublisher;
    private final OAuthProviderService oAuthProviderService;

    // ── Sign Up ───────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("El nickname ya está en uso");
        }

        User user = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .username(request.getNickname())   // username = nickname en registro EMAIL
                .password(passwordEncoder.encode(request.getPassword()))
                .picture("")                       // picture vacío por defecto
                .role(Role.ROLE_STUDENT)
                .authProvider(AuthProvider.EMAIL)
                .build();

        userRepository.save(user);
        log.info("Usuario registrado: {}", user.getEmail());

        eventPublisher.publishUserRegistered(
                UserRegisteredEvent.of(user.getId(), user.getEmail(),
                        user.getNickname(), user.getRole().name())
        );

        return buildAuthResponse(user);
    }

    // ── Sign In ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AuthResponse signIn(SignInRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (user.getAuthProvider() != AuthProvider.EMAIL) {
            throw new AuthProviderConflictException(
                    "Esta cuenta usa " + user.getAuthProvider() +
                            ". Inicia sesión con ese proveedor."
            );
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        log.info("Login exitoso: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    // ── OAuth ─────────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse oAuth(String providerName, OAuthRequest request) {
        OAuthProviderService.OAuthProfile profile =
                oAuthProviderService.fetchProfile(providerName, request.getProviderToken());

        // Regla de negocio: bloquear colisión con cuenta EMAIL
        userRepository.findByEmailAndAuthProvider(profile.email(), AuthProvider.EMAIL)
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Este email ya está registrado con contraseña. " +
                                    "Por favor inicia sesión con tu email y contraseña."
                    );
                });

        AuthProvider provider = AuthProvider.valueOf(providerName.toUpperCase());

        User user = userRepository.findByEmail(profile.email())
                .orElseGet(() -> {
                    String nickname = resolveNickname(request.getNickname(), profile.email());
                    User newUser = User.builder()
                            .email(profile.email())
                            .nickname(nickname)
                            .username(profile.name() != null ? profile.name() : nickname)
                            .picture(profile.picture() != null ? profile.picture() : "")
                            .role(Role.ROLE_STUDENT)
                            .authProvider(provider)
                            .build();
                    userRepository.save(newUser);
                    log.info("Usuario OAuth creado: {}", newUser.getEmail());
                    eventPublisher.publishUserRegistered(
                            UserRegisteredEvent.of(newUser.getId(), newUser.getEmail(),
                                    newUser.getNickname(), newUser.getRole().name())
                    );
                    return newUser;
                });

        return buildAuthResponse(user);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        if (!jwtUtil.isRefreshTokenValid(token)) {
            throw new IllegalArgumentException("Refresh token inválido o expirado");
        }

        UUID userId = UUID.fromString(jwtUtil.getSubjectFromRefreshToken(token));

        if (!refreshTokenService.isValid(userId, token)) {
            throw new IllegalArgumentException(
                    "Refresh token no reconocido. Inicia sesión nuevamente."
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        refreshTokenService.revoke(userId);
        return buildAuthResponse(user);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    public void logout(UUID userId) {
        refreshTokenService.revoke(userId);
        log.info("Logout para userId={}", userId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        refreshTokenService.save(user.getId(), refreshToken);
        return AuthResponse.of(accessToken, refreshToken, UserProfileResponse.from(user));
    }

    private String resolveNickname(String requested, String email) {
        if (requested != null && !requested.isBlank()) {
            if (userRepository.existsByNickname(requested)) {
                return requested + "_" + UUID.randomUUID().toString().substring(0, 5);
            }
            return requested;
        }
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
        return userRepository.existsByNickname(base)
                ? base + "_" + UUID.randomUUID().toString().substring(0, 5)
                : base;
    }
}