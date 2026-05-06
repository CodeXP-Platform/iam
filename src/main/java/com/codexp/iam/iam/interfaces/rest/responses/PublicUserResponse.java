package com.codexp.iam.iam.interfaces.rest.responses;

import com.codexp.iam.iam.domain.model.valueobjects.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PublicUserResponse {

    private UUID   id;
    private String nickname;
    private String picture;
    private Role   role;
}
