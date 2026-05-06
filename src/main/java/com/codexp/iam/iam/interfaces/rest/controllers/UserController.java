package com.codexp.iam.iam.interfaces.rest.controllers;

import com.codexp.iam.iam.domain.model.commands.DeleteAccountCommand;
import com.codexp.iam.iam.domain.model.entities.User;
import com.codexp.iam.iam.domain.model.queries.GetMyProfileQuery;
import com.codexp.iam.iam.domain.model.queries.GetPublicProfileQuery;
import com.codexp.iam.iam.domain.services.UserCommandService;
import com.codexp.iam.iam.domain.services.UserQueryService;
import com.codexp.iam.iam.interfaces.rest.requests.UpdateProfileRequest;
import com.codexp.iam.iam.interfaces.rest.responses.PublicUserResponse;
import com.codexp.iam.iam.interfaces.rest.responses.UserProfileResponse;
import com.codexp.iam.iam.interfaces.rest.transformers.UserAssembler;
import com.codexp.iam.iam.interfaces.rest.transformers.UserCommandAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Users", description = "Gestión de perfil de usuario")
@SecurityRequirement(name = "bearerAuth")   // candado en todos los endpoints del controller
@RestController
@RequestMapping("/api/v1/iam/users")
@RequiredArgsConstructor
public class UserController {

    private final UserCommandService commandService;
    private final UserQueryService   queryService;

    @Operation(summary = "Obtener mi perfil")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId
    ) {
        User user = queryService.getMyProfile(new GetMyProfileQuery(UUID.fromString(userId)));
        return ResponseEntity.ok(UserAssembler.toUserProfileResponse(user));
    }

    @Operation(summary = "Actualizar mi perfil")
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        User user = commandService.updateProfile(
                UserCommandAssembler.toUpdateProfileCommand(UUID.fromString(userId), request)
        );
        return ResponseEntity.ok(UserAssembler.toUserProfileResponse(user));
    }

    @Operation(summary = "Eliminar mi cuenta")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId
    ) {
        commandService.deleteAccount(new DeleteAccountCommand(UUID.fromString(userId)));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Ver perfil público de un usuario", security = {})  // sin candado — endpoint público
    @GetMapping("/public/{userId}")
    public ResponseEntity<PublicUserResponse> getPublicProfile(
            @PathVariable UUID userId
    ) {
        User user = queryService.getPublicProfile(new GetPublicProfileQuery(userId));
        return ResponseEntity.ok(UserAssembler.toPublicUserResponse(user));
    }
}
