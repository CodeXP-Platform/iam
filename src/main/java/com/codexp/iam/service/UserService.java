package com.codexp.iam.service;

import com.codexp.iam.dto.request.UpdateProfileRequest;
import com.codexp.iam.dto.response.PublicUserResponse;
import com.codexp.iam.dto.response.UserProfileResponse;
import com.codexp.iam.entity.User;
import com.codexp.iam.event.UserDeletedEvent;
import com.codexp.iam.event.UserProfileUpdatedEvent;
import com.codexp.iam.infrastructure.messaging.DomainEventPublisher;
import com.codexp.iam.infrastructure.security.RefreshTokenService;
import com.codexp.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository       userRepository;
    private final RefreshTokenService  refreshTokenService;
    private final DomainEventPublisher eventPublisher;

    // ── GET /me ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(UUID userId) {
        User user = findOrThrow(userId);
        return UserProfileResponse.from(user);
    }

    // ── PUT /me ───────────────────────────────────────────────────────────────

    @Transactional
    public UserProfileResponse updateMyProfile(UUID userId, UpdateProfileRequest request) {
        User user = findOrThrow(userId);

        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            boolean nicknameConflict = userRepository.existsByNickname(request.getNickname())
                    && !user.getNickname().equals(request.getNickname());
            if (nicknameConflict) {
                throw new IllegalArgumentException("El nickname ya está en uso");
            }
            user.setNickname(request.getNickname());
        }

        if (request.getPicture() != null) {
            user.setPicture(request.getPicture());
        }

        userRepository.save(user);
        log.info("Perfil actualizado para userId={}", userId);

        eventPublisher.publishUserProfileUpdated(
                UserProfileUpdatedEvent.of(user.getId(), user.getNickname(), user.getPicture())
        );

        return UserProfileResponse.from(user);
    }

    // ── DELETE /me ────────────────────────────────────────────────────────────

    @Transactional
    public void deleteMyAccount(UUID userId) {
        User user = findOrThrow(userId);

        // 1. Revocar sesión activa
        refreshTokenService.revoke(userId);

        // 2. Publicar evento ANTES de eliminar (para que otros servicios reaccionen)
        eventPublisher.publishUserDeleted(
                UserDeletedEvent.of(user.getId(), user.getEmail())
        );

        // 3. Eliminar usuario
        userRepository.delete(user);
        log.info("Cuenta eliminada para userId={}", userId);
    }

    // ── GET /public/{userId} ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PublicUserResponse getPublicProfile(UUID userId) {
        User user = findOrThrow(userId);
        return PublicUserResponse.from(user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }
}