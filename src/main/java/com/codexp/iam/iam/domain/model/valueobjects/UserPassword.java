package com.codexp.iam.iam.domain.model.valueobjects;

import java.util.Objects;

/**
 * Value Object that wraps an already-BCrypt-encoded password hash.
 * Raw passwords are never stored; encoding happens in the application layer.
 */
public record UserPassword(String hashedValue) {

    public UserPassword {
        Objects.requireNonNull(hashedValue, "Password hash cannot be null");
    }

    public static UserPassword ofHash(String hashedValue) {
        return new UserPassword(hashedValue);
    }
}
