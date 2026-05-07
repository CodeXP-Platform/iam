package com.codexp.iam.iam.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global de OpenAPI / Swagger UI.
 *
 * Define el SecurityScheme "bearerAuth" (HTTP Bearer JWT) para que
 * Swagger UI muestre el botón "Authorize" y adjunte automáticamente
 * el header "Authorization: Bearer <token>" en cada petición protegida.
 *
 * Para usar en Swagger UI:
 *  1. Llama a POST /api/v1/iam/auth/sign-in y copia el accessToken.
 *  2. Haz clic en "Authorize" (candado), pega el token y confirma.
 *  3. Todas las peticiones que requieran autenticación lo incluirán.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title       = "IAM Service — CodeXP Platform",
                version     = "v1",
                description = "Microservicio de Identidad y Acceso: registro, login, OAuth, refresh y gestión de perfil.",
                contact     = @Contact(name = "CodeXP Team")
        ),
        servers = @Server(url = "/", description = "Local / Current")
)
@SecurityScheme(
        name         = "bearerAuth",
        type         = SecuritySchemeType.HTTP,
        scheme       = "bearer",
        bearerFormat = "JWT",
        description  = "Introduce el accessToken obtenido en /auth/sign-in (sin el prefijo 'Bearer ')."
)
public class OpenApiConfig {
    // Spring carga esta clase como @Configuration; las anotaciones
    // de nivel de clase son suficientes para registrar el SecurityScheme.
}
