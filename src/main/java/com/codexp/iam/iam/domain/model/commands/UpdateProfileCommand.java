package com.codexp.iam.iam.domain.model.commands;

import java.util.UUID;

public record UpdateProfileCommand(UUID userId, String nickname, String picture) {}
