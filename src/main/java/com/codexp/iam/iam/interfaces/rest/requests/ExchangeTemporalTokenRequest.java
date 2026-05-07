package com.codexp.iam.iam.interfaces.rest.requests;

import jakarta.validation.constraints.NotBlank;

public record ExchangeTemporalTokenRequest(
        @NotBlank String temporalToken
) {}
