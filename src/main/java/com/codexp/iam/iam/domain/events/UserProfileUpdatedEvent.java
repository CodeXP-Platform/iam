package com.codexp.iam.iam.domain.events;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserProfileUpdatedEvent {

    private UUID userId;
    private String nickname;
    private String picture;
    private LocalDateTime occurredAt;

    public static UserProfileUpdatedEvent of(UUID userId, String nickname, String picture) {
        return UserProfileUpdatedEvent.builder()
                .userId(userId)
                .nickname(nickname)
                .picture(picture)
                .occurredAt(LocalDateTime.now())
                .build();
    }
}
