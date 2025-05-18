package br.com.fiap.mspayments.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentCardDtoResponse(
        @JsonProperty("payment_id")
        String paymentId,

        String status,

        String description,

        @JsonProperty("authorization_code")
        String authorizationCode,

        Double amount,

        String currency,

        @JsonProperty("order_id")
        String orderId
) {}
