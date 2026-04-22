package com.codexp.iam.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 3, max = 20, message = "El nickname debe tener entre 3 y 20 caracteres")
    private String nickname;

    // URL de la imagen de perfil — opcional
    private String picture;
}