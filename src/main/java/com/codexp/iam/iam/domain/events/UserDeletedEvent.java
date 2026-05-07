package com.codexp.iam.iam.domain.events;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserDeletedEvent {

    private UUID userId;
    private String email;
    private LocalDateTime occurredAt;

    public static UserDeletedEvent of(UUID userId, String email) {
        return UserDeletedEvent.builder()
                .userId(userId)
                .email(email)
                .occurredAt(LocalDateTime.now())
                .build();
    }
}
