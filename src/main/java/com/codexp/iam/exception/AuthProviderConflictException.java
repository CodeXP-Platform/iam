package com.codexp.iam.exception;

public class AuthProviderConflictException extends RuntimeException {
    public AuthProviderConflictException(String message) {
        super(message);
    }
}
