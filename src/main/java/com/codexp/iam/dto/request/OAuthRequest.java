package com.codexp.iam.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OAuthRequest {

    /**
     * Token de identidad emitido por el proveedor OAuth (Google, GitHub, etc.)
     * El IAM lo valida contra el proveedor para extraer el perfil del usuario.
     */
    @NotBlank(message = "El token del proveedor es obligatorio")
    private String providerToken;

    /**
     * Nickname deseado — solo requerido en el primer login (registro implícito).
     * En logins subsecuentes se ignora.
     */
    private String nickname;
}