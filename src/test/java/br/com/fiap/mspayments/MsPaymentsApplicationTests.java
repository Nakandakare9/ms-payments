package br.com.fiap.mspayments;

import br.com.fiap.mspayments.application.dto.*;
import br.com.fiap.mspayments.application.mapper.PaymentMapper;
import br.com.fiap.mspayments.application.service.PaymentService;
import br.com.fiap.mspayments.infrastructure.exception.AuthenticationException;
import br.com.fiap.mspayments.infrastructure.exception.PaymentStatusException;
import br.com.fiap.mspayments.infrastructure.persistence.entity.PaymentEntity;
import br.com.fiap.mspayments.infrastructure.persistence.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class MsPaymentsApplicationTests {

    @Mock
    private PaymentRepository repository;

    @Mock
    private PaymentMapper mapper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(paymentService, "authServerUrl", "http://auth-server");
        ReflectionTestUtils.setField(paymentService, "authCredentials", "Basic credentials");
        ReflectionTestUtils.setField(paymentService, "paymentApiUrl", "http://payment-api");
        ReflectionTestUtils.setField(paymentService, "sellerId", "seller123");
        ReflectionTestUtils.setField(paymentService, "qrCodeExpiration", "60");
    }

    @Test
    void generateToken_Success() {
        // Arrange
        TokenDto tokenDto = new TokenDto("client_credentials", "client123", "secret123", "payments");
        TokenDtoResponse expectedResponse = new TokenDtoResponse("access_token_123", "bearer", 3600);

        when(restTemplate.postForEntity(
                anyString(),
                any(),
                eq(TokenDtoResponse.class)
        )).thenReturn(ResponseEntity.ok(expectedResponse));
        TokenDtoResponse result = paymentService.generateToken(tokenDto);

        assertNotNull(result);
        assertEquals("access_token_123", result.accessToken());
        assertEquals("bearer", result.tokenType());
        assertEquals(3600, result.expiresIn());
    }

    @Test
    void generateToken_WhenErrorOccurs_ThrowsAuthenticationException() {
        // Arrange
        TokenDto tokenDto = new TokenDto("client_credentials", "client123", "secret123", "payments");

        when(restTemplate.postForEntity(
                anyString(),
                any(),
                eq(TokenDtoResponse.class)
        )).thenThrow(new RestClientException("Connection error"));

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> paymentService.generateToken(tokenDto));
    }

    @Test
    void generateQR_Success() {
        // Arrange
        QrDto qrDto = new QrDto(
                new BigDecimal("123.45"),
                "BRL",
                "1",
                "1"
        );

        QrDtoResponse expectedResponse = new QrDtoResponse("qr_code_123", "PENDING", "123456");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(),
                eq(QrDtoResponse.class)
        )).thenReturn(ResponseEntity.ok(expectedResponse));

        when(repository.save(any())).thenReturn(new PaymentEntity());

        QrDtoResponse result = paymentService.generateQR(qrDto, "Bearer token123");

        assertNotNull(result);
    }

    @Test
    void getStatus_Success() {

        StatusDto.PaymentRequest paymentRequest = new StatusDto.PaymentRequest(
                "PIX",          // type
                false,          // delayed
                true,          // authenticated
                false,         // pre_authorization
                false,         // save_card_data
                "PAYMENT",     // transaction_type
                1,            // number_installments
                "LOJA",       // soft_descriptor
                1234         // dynamic_mcc
        );


        StatusDto.Credit credit = new StatusDto.Credit(
                false,                      // delayed
                "123456",                   // authorization_code
                ZonedDateTime.now(),        // authorized_at
                "00",                       // reason_code
                "Success",                  // reason_message
                "BANCO",                    // acquirer
                "LOJA",                    // soft_descriptor
                "BRAND",                   // brand
                "123456",                 // terminal_nsu
                "789123",                 // acquirer_transaction_id
                "TRX123"                  // transaction_id
        );

        StatusDto.PaymentResponse paymentResponse = new StatusDto.PaymentResponse(
                "PAY123",                   // payment_id
                "SELLER123",                // seller_id
                100.00,                     // amount
                "BRL",                      // currency
                "ORDER123",                 // order_id
                "COMPLETED",                // status
                ZonedDateTime.now(),        // received_at
                credit,                     // credit
                null                        // debit
        );

        StatusDto expectedStatus = new StatusDto(
                "SELLER123",                // seller_id
                "QRCODE123",               // hash_qr_code
                "COMPLETED",               // status
                "ORDER123",                // order_id
                100.00,                    // amount
                paymentRequest,            // payment_request
                paymentResponse           // payment_response
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(StatusDto.class)
        )).thenReturn(ResponseEntity.ok(expectedStatus));

        // Act
        StatusDto result = paymentService.getStatus("payment123", "Bearer token123");

        // Assert
        assertNotNull(result);
    }

    @Test
    void getStatus_WhenErrorOccurs_ThrowsPaymentStatusException() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(StatusDto.class)
        )).thenThrow(new RestClientException("Connection error"));

        // Act & Assert
        assertThrows(PaymentStatusException.class,
                () -> paymentService.getStatus("payment123", "Bearer token123"));
    }
}
