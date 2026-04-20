package com.codexp.iam.dto.response;

import com.codexp.iam.entity.AuthProvider;
import com.codexp.iam.entity.Role;
import com.codexp.iam.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {

    private UUID id;
    private String email;
    private String nickname;
    private String picture;
    private Role role;
    private AuthProvider authProvider;
    private LocalDateTime createdAt;

    public static UserProfileResponse from(User user) {
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
}