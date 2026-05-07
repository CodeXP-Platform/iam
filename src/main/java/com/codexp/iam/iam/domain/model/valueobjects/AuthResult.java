package com.codexp.iam.iam.domain.model.valueobjects;

import com.codexp.iam.iam.domain.model.entities.User;

import java.util.Objects;

/**
 * Value Object returned by auth commands (signUp, signIn, oAuth, refresh).
 * Carries the authenticated User together with the issued JWT pair.
 */
public record AuthResult(User user, String accessToken, String refreshToken) {

    public AuthResult {
        Objects.requireNonNull(user, "user cannot be null");
        Objects.requireNonNull(accessToken, "accessToken cannot be null");
        Objects.requireNonNull(refreshToken, "refreshToken cannot be null");
    }

    public static AuthResult of(User user, String accessToken, String refreshToken) {
        return new AuthResult(user, accessToken, refreshToken);
    }
}
