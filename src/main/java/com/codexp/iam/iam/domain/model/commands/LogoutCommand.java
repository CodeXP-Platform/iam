package com.codexp.iam.iam.domain.model.commands;

/**
 * Carries the raw Bearer access token string so the application service
 * can extract the userId and revoke the refresh token.
 * The token may be null when the client sends no Authorization header.
 */
public record LogoutCommand(String accessToken) {}
