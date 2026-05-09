package com.codexp.iam.iam.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration-seconds}")
    private long refreshExpirationMs;

    /**
     * userId → (refreshToken, expiresAt)
     * ConcurrentHashMap is thread-safe for a single node.
     * Replace with Redis for multi-node / production deployments.
     */
    private final Map<UUID, TokenEntry> store = new ConcurrentHashMap<>();

    private record TokenEntry(String token, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    public void save(UUID userId, String refreshToken) {
        store.put(userId, new TokenEntry(
                refreshToken,
                Instant.now().plusMillis(refreshExpirationMs)
        ));
        log.debug("Refresh token guardado para userId={}", userId);
    }

    public boolean isValid(UUID userId, String refreshToken) {
        TokenEntry entry = store.get(userId);
        if (entry == null || entry.isExpired()) {
            store.remove(userId);
            return false;
        }
        return entry.token().equals(refreshToken);
    }

    public void revoke(UUID userId) {
        store.remove(userId);
        log.debug("Refresh token revocado para userId={}", userId);
    }
}
