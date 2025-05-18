package br.com.fiap.mspayments.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenDtoResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        Integer expiresIn,

        String scope
) {}
