package com.codexp.iam.iam.interfaces.rest.responses;

import com.codexp.iam.iam.domain.model.valueobjects.AuthProvider;
import com.codexp.iam.iam.domain.model.valueobjects.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {

    private UUID          id;
    private String        email;
    private String        nickname;
    private String        picture;
    private Role          role;
    private AuthProvider  authProvider;
    private LocalDateTime createdAt;
}
