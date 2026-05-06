package com.codexp.iam.iam.interfaces.rest.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OAuthRequest {

    @NotBlank(message = "El token del proveedor es obligatorio")
    private String providerToken;

    private String nickname;
}
