package com.codexp.iam.iam.interfaces.rest.transformers;

import com.codexp.iam.iam.domain.model.entities.User;
import com.codexp.iam.iam.domain.model.valueobjects.AuthResult;
import com.codexp.iam.iam.interfaces.rest.responses.AuthResponse;
import com.codexp.iam.iam.interfaces.rest.responses.PublicUserResponse;
import com.codexp.iam.iam.interfaces.rest.responses.UserProfileResponse;

/**
 * Static assembler: converts Domain objects → REST Response DTOs.
 * No MapStruct, no reflection — explicit manual mapping.
 */
public class UserAssembler {

    private UserAssembler() { /* utility class */ }

    public static AuthResponse toAuthResponse(AuthResult result) {
        return AuthResponse.builder()
                .accessToken(result.accessToken())
                .refreshToken(result.refreshToken())
                .tokenType("Bearer")
                .user(toUserProfileResponse(result.user()))
                .build();
    }

    public static UserProfileResponse toUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .picture(user.getPicture())
                .role(user.getRole())
                .authProvider(user.getAuthProvider())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static PublicUserResponse toPublicUserResponse(User user) {
        return PublicUserResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .picture(user.getPicture())
                .role(user.getRole())
                .build();
    }
}
