package br.com.fiap.mspayments.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentCardDto(
        @JsonProperty("seller_id")
        String sellerId,

        Double amount,

        String currency,

        Order order,

        Customer customer,

        Credit credit
) {
    public record Order(
            @JsonProperty("order_id")
            String orderId
    ) {}

    public record Customer(
            @JsonProperty("customer_id")
            String customerId
    ) {}

    public record Credit(
            Card card
    ) {
        public record Card(
                @JsonProperty("number_token")
                String numberToken
        ) {}
    }
}
