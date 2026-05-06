package com.codexp.iam.iam.interfaces.rest.requests;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 3, max = 20, message = "El nickname debe tener entre 3 y 20 caracteres")
    private String nickname;

    private String picture;
}
