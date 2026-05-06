package com.codexp.iam.iam.domain.model.valueobjects;

import java.util.Objects;

public record UserEmail(String value) {

    public UserEmail {
        Objects.requireNonNull(value, "Email cannot be null");
        if (value.isBlank() || !value.contains("@")) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }

    public static UserEmail of(String value) {
        return new UserEmail(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
