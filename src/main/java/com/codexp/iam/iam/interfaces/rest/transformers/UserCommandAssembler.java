package com.codexp.iam.iam.interfaces.rest.transformers;

import com.codexp.iam.iam.domain.model.commands.*;
import com.codexp.iam.iam.interfaces.rest.requests.*;

import java.util.UUID;

/**
 * Static assembler: converts REST Request DTOs → Domain Command objects.
 * The controller never passes a Request directly to the application layer.
 */
public class UserCommandAssembler {

    private UserCommandAssembler() { /* utility class */ }

    public static SignUpCommand toSignUpCommand(SignUpRequest request) {
        return new SignUpCommand(
                request.getEmail(),
                request.getNickname(),
                request.getPassword()
        );
    }

    public static SignInCommand toSignInCommand(SignInRequest request) {
        return new SignInCommand(
                request.getEmail(),
                request.getPassword()
        );
    }

    public static OAuthCommand toOAuthCommand(String provider, OAuthRequest request) {
        return new OAuthCommand(
                provider,
                request.getProviderToken(),
                request.getNickname()
        );
    }

    public static RefreshTokenCommand toRefreshTokenCommand(RefreshTokenRequest request) {
        return new RefreshTokenCommand(request.getRefreshToken());
    }

    public static UpdateProfileCommand toUpdateProfileCommand(UUID userId, UpdateProfileRequest request) {
        return new UpdateProfileCommand(
                userId,
                request.getNickname(),
                request.getPicture()
        );
    }
}
