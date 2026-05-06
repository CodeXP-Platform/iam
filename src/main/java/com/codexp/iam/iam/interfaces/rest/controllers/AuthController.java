package com.codexp.iam.iam.interfaces.rest.controllers;

import com.codexp.iam.iam.domain.model.commands.*;
import com.codexp.iam.iam.domain.model.valueobjects.AuthResult;
import com.codexp.iam.iam.domain.services.UserCommandService;
import com.codexp.iam.iam.interfaces.rest.requests.*;
import com.codexp.iam.iam.interfaces.rest.responses.AuthResponse;
import com.codexp.iam.iam.interfaces.rest.transformers.UserAssembler;
import com.codexp.iam.iam.interfaces.rest.transformers.UserCommandAssembler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/iam/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserCommandService commandService;

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpCommand command = UserCommandAssembler.toSignUpCommand(request);
        AuthResult    result  = commandService.signUp(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserAssembler.toAuthResponse(result));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody SignInRequest request) {
        SignInCommand command = UserCommandAssembler.toSignInCommand(request);
        AuthResult    result  = commandService.signIn(command);
        return ResponseEntity.ok(UserAssembler.toAuthResponse(result));
    }

    @PostMapping("/oauth/{provider}")
    public ResponseEntity<AuthResponse> oauth(
            @PathVariable String provider,
            @Valid @RequestBody OAuthRequest request
    ) {
        OAuthCommand command = UserCommandAssembler.toOAuthCommand(provider, request);
        AuthResult   result  = commandService.processOAuth(command);
        return ResponseEntity.ok(UserAssembler.toAuthResponse(result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenCommand command = UserCommandAssembler.toRefreshTokenCommand(request);
        AuthResult          result  = commandService.refresh(command);
        return ResponseEntity.ok(UserAssembler.toAuthResponse(result));
    }

    /**
     * Logout: the raw Bearer token is passed as a LogoutCommand so the
     * application service can extract the userId without any infra dependency
     * leaking into this controller.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;
        commandService.logout(new LogoutCommand(token));
        return ResponseEntity.noContent().build();
    }
}
