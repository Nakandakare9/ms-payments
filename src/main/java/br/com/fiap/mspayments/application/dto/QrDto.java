package br.com.fiap.mspayments.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record QrDto(
        BigDecimal amount,
        String currency,
        String orderId,
        String customerId
) {}
