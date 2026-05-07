package com.codexp.iam.iam.domain.model.valueobjects;

import java.util.Objects;

public record UserNickname(String value) {

    public UserNickname {
        Objects.requireNonNull(value, "Nickname cannot be null");
        if (value.length() < 3 || value.length() > 20) {
            throw new IllegalArgumentException("Nickname must be between 3 and 20 characters");
        }
    }

    public static UserNickname of(String value) {
        return new UserNickname(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
