package com.codexp.iam.controller;

import com.codexp.iam.dto.request.UpdateProfileRequest;
import com.codexp.iam.dto.response.PublicUserResponse;
import com.codexp.iam.dto.response.UserProfileResponse;
import com.codexp.iam.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/iam/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * El principal es el userId (String) almacenado en el SecurityContext
     * por JwtAuthenticationFilter — lo casteamos a UUID.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity.ok(
                userService.getMyProfile(UUID.fromString(userId))
        );
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(
                userService.updateMyProfile(UUID.fromString(userId), request)
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(
            @AuthenticationPrincipal String userId
    ) {
        userService.deleteMyAccount(UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public/{userId}")
    public ResponseEntity<PublicUserResponse> getPublicProfile(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(userService.getPublicProfile(userId));
    }
}