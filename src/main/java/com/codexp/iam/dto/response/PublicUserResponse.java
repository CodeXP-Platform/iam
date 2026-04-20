package com.codexp.iam.dto.response;

import com.codexp.iam.entity.Role;
import com.codexp.iam.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PublicUserResponse {

    private UUID id;
    private String nickname;
    private String picture;
    private Role role;

    public static PublicUserResponse from(User user) {
        return PublicUserResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .picture(user.getPicture())
                .role(user.getRole())
                .build();
    }
}