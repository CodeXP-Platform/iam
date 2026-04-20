package com.codexp.iam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthProviderService {

    private final RestClient restClient = RestClient.create();

    /**
     * Perfil normalizado extraído de cualquier proveedor OAuth.
     */
    public record OAuthProfile(String email, String name, String picture) {}

    public OAuthProfile fetchProfile(String provider, String providerToken) {
        return switch (provider.toLowerCase()) {
            case "google" -> fetchGoogleProfile(providerToken);
            case "github" -> fetchGithubProfile(providerToken);
            default -> throw new IllegalArgumentException("Proveedor OAuth no soportado: " + provider);
        };
    }

    // ── Google ────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private OAuthProfile fetchGoogleProfile(String idToken) {
        // Google tokeninfo endpoint valida el id_token y retorna el perfil
        Map<String, Object> response = restClient.get()
                .uri("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken)
                .retrieve()
                .body(Map.class);

        if (response == null || response.containsKey("error")) {
            throw new IllegalArgumentException("Token de Google inválido");
        }

        return new OAuthProfile(
                (String) response.get("email"),
                (String) response.get("name"),
                (String) response.get("picture")
        );
    }

    // ── GitHub ────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private OAuthProfile fetchGithubProfile(String accessToken) {
        Map<String, Object> response = restClient.get()
                .uri("https://api.github.com/user")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalArgumentException("Token de GitHub inválido");
        }

        String email = (String) response.get("email");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Tu cuenta de GitHub no tiene email público. Configura uno en GitHub e intenta de nuevo."
            );
        }

        return new OAuthProfile(
                email,
                (String) response.get("name"),
                (String) response.get("avatar_url")
        );
    }
}