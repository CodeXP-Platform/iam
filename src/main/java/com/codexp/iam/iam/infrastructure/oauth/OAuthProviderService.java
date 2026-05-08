package com.codexp.iam.iam.infrastructure.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OAuthProviderService {

    private final RestClient restClient = RestClient.create();

    public record OAuthProfile(String providerUserId, String email, String name, String picture) {}

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
        Map<String, Object> response = restClient.get()
                .uri("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken)
                .retrieve()
                .body(Map.class);

        if (response == null || response.containsKey("error")) {
            throw new IllegalArgumentException("Token de Google inválido");
        }

        return new OAuthProfile(
                (String) response.get("sub"),
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

        String providerUserId = String.valueOf(response.get("id"));
        String email = (String) response.get("email");

        // /user returns null email when it's private; try /user/emails as fallback
        if (email == null || email.isBlank()) {
            email = fetchGithubPrimaryEmail(accessToken);
        }

        // email may still be null — that's fine, we identify the user by providerUserId
        return new OAuthProfile(
                providerUserId,
                (email != null && !email.isBlank()) ? email : null,
                (String) response.get("name"),
                (String) response.get("avatar_url")
        );
    }

    @SuppressWarnings("unchecked")
    private String fetchGithubPrimaryEmail(String accessToken) {
        List<Map<String, Object>> emails = restClient.get()
                .uri("https://api.github.com/user/emails")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(List.class);

        if (emails == null) return null;

        return emails.stream()
                .filter(e -> Boolean.TRUE.equals(e.get("primary")) && Boolean.TRUE.equals(e.get("verified")))
                .map(e -> (String) e.get("email"))
                .findFirst()
                .orElse(null);
    }
}
