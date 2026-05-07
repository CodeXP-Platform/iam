package com.codexp.iam.shared.exceptions;

public class AuthProviderConflictException extends RuntimeException {
    public AuthProviderConflictException(String message) {
        super(message);
    }
}
