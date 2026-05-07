package com.codexp.iam.iam.domain.services;

import com.codexp.iam.iam.domain.model.commands.*;
import com.codexp.iam.iam.domain.model.entities.User;
import com.codexp.iam.iam.domain.model.valueobjects.AuthResult;

/**
 * Pure domain service interface for write operations.
 * No Spring annotations — dependency inversion boundary.
 */
public interface UserCommandService {

    AuthResult signUp(SignUpCommand command);

    AuthResult signIn(SignInCommand command);

    AuthResult processOAuth(OAuthCommand command);

    AuthResult refresh(RefreshTokenCommand command);

    void logout(LogoutCommand command);

    User updateProfile(UpdateProfileCommand command);

    void deleteAccount(DeleteAccountCommand command);
}
