package br.com.fiap.mspayments.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

public record QrDtoResponse(
        @JsonProperty("payment_id")
        String paymentId,

        String status,

        String description,

        @JsonProperty("additional_data")
        AdditionalData additionalData
) {
    public record AdditionalData(
            @JsonProperty("transaction_id")
            String transactionId,

            @JsonProperty("qr_code")
            String qrCode,

            @JsonProperty("creation_date_qrcode")
            ZonedDateTime creationDateQrcode,

            @JsonProperty("expiration_date_qrcode")
            ZonedDateTime expirationDateQrcode,

            @JsonProperty("psp_code")
            String pspCode
    ) {}
}
