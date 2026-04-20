package com.codexp.iam.controller;

import com.codexp.iam.dto.request.OAuthRequest;
import com.codexp.iam.dto.request.RefreshTokenRequest;
import com.codexp.iam.dto.request.SignInRequest;
import com.codexp.iam.dto.request.SignUpRequest;
import com.codexp.iam.dto.response.AuthResponse;
import com.codexp.iam.infrastructure.security.JwtUtil;
import com.codexp.iam.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/iam/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil     jwtUtil;

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.signUp(request));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody SignInRequest request) {
        return ResponseEntity.ok(authService.signIn(request));
    }

    @PostMapping("/oauth/{provider}")
    public ResponseEntity<AuthResponse> oauth(
            @PathVariable String provider,
            @Valid @RequestBody OAuthRequest request
    ) {
        return ResponseEntity.ok(authService.oAuth(provider, request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    /**
     * Logout: extraemos el userId del Bearer token del header.
     * El endpoint es público en SecurityConfig, pero necesitamos
     * el token para saber a quién revocar.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isAccessTokenValid(token)) {
                UUID userId = jwtUtil.extractUserId(token);
                authService.logout(userId);
            }
        }
        // Siempre 204 — no revelamos si el token era válido o no
        return ResponseEntity.noContent().build();
    }
}