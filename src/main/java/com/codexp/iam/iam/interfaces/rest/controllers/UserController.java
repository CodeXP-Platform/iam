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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/iam/users")
@RequiredArgsConstructor
public class UserController {

    private final UserCommandService commandService;
    private final UserQueryService   queryService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal String userId
    ) {
        User user = queryService.getMyProfile(new GetMyProfileQuery(UUID.fromString(userId)));
        return ResponseEntity.ok(UserAssembler.toUserProfileResponse(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        User user = commandService.updateProfile(
                UserCommandAssembler.toUpdateProfileCommand(UUID.fromString(userId), request)
        );
        return ResponseEntity.ok(UserAssembler.toUserProfileResponse(user));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(
            @AuthenticationPrincipal String userId
    ) {
        commandService.deleteAccount(new DeleteAccountCommand(UUID.fromString(userId)));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public/{userId}")
    public ResponseEntity<PublicUserResponse> getPublicProfile(
            @PathVariable UUID userId
    ) {
        User user = queryService.getPublicProfile(new GetPublicProfileQuery(userId));
        return ResponseEntity.ok(UserAssembler.toPublicUserResponse(user));
    }
}
