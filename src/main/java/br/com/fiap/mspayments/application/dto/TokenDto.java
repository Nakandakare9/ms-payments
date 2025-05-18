package br.com.fiap.mspayments.application.dto;

public record TokenDto(
        String grant_type,
        String client_id,
        String client_secret,
        String scope
) {}
