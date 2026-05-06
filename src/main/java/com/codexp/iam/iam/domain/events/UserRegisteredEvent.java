package com.codexp.iam.iam.domain.events;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserRegisteredEvent {

    private UUID userId;
    private String email;
    private String nickname;
    private String role;
    private LocalDateTime occurredAt;

    public static UserRegisteredEvent of(UUID userId, String email, String nickname, String role) {
        return UserRegisteredEvent.builder()
                .userId(userId)
                .email(email)
                .nickname(nickname)
                .role(role)
                .occurredAt(LocalDateTime.now())
                .build();
    }
}
