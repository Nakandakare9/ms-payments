package br.com.fiap.mspayments.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

public record StatusDto(
        String seller_id,
        String hash_qr_code,
        String status,
        String order_id,
        Double amount,
        PaymentRequest payment_request,
        PaymentResponse payment_response
) {
    public record PaymentRequest(
            String type,
            boolean delayed,
            boolean authenticated,
            boolean pre_authorization,
            boolean save_card_data,
            String transaction_type,
            int number_installments,
            String soft_descriptor,
            int dynamic_mcc
    ) {}

    public record PaymentResponse(
            String payment_id,
            String seller_id,
            Double amount,
            String currency,
            String order_id,
            String status,
            ZonedDateTime received_at,
            Credit credit,
            Debit debit
    ) {}

    public record Credit(
            boolean delayed,
            String authorization_code,
            ZonedDateTime authorized_at,
            String reason_code,
            String reason_message,
            String acquirer,
            String soft_descriptor,
            String brand,
            String terminal_nsu,
            String acquirer_transaction_id,
            String transaction_id
    ) {}

    public record Debit(
            boolean delayed,
            String authorization_code,
            ZonedDateTime authorized_at,
            String reason_code,
            String reason_message,
            String acquirer,
            String soft_descriptor,
            String brand,
            String terminal_nsu,
            String acquirer_transaction_id,
            String transaction_id
    ) {}
}
