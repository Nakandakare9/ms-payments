package br.com.fiap.mspayments.application.service;

import br.com.fiap.mspayments.application.dto.*;
import br.com.fiap.mspayments.application.mapper.PaymentMapper;
import br.com.fiap.mspayments.infrastructure.exception.AuthenticationException;
import br.com.fiap.mspayments.infrastructure.exception.PaymentStatusException;
import br.com.fiap.mspayments.infrastructure.exception.QrCodeException;
import br.com.fiap.mspayments.infrastructure.persistence.entity.PaymentEntity;
import br.com.fiap.mspayments.infrastructure.persistence.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final RestTemplate restTemplate;

    @Value("${auth.server.url}")
    private String authServerUrl;

    @Value("${auth.server.credentials}")
    private String authCredentials;

    @Value("${payment.api.url}")
    private String paymentApiUrl;

    @Value("${payment.seller.id}")
    private String sellerId;

    @Value("${payment.qrcode.expiration}")
    private String qrCodeExpiration;


    public PaymentService(PaymentRepository repository,
                          PaymentMapper mapper,
                          RestTemplate restTemplate) {
        this.repository = repository;
        this.mapper = mapper;
        this.restTemplate = restTemplate;
    }

    public TokenDtoResponse generateToken(TokenDto dto) {
        try {
            HttpHeaders headers = createHeaders();
            MultiValueMap<String, String> requestBody = createRequestBody(dto);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<TokenDtoResponse> response = restTemplate.postForEntity(
                    authServerUrl,
                    request,
                    TokenDtoResponse.class
            );

            if (response.getBody() == null) {
                throw new AuthenticationException("Empty response from authentication server");
            }

            return response.getBody();

        } catch (RestClientException e) {
            throw new AuthenticationException("Error generating token: " + e.getMessage(), e);
        }
    }

    public QrDtoResponse generateQR(QrDto dto, String authorization) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("seller_id", sellerId);
            headers.set("x-qrcode-expiration-time", qrCodeExpiration);
            headers.set("Authorization", authorization);

            HttpEntity<QrDto> request = new HttpEntity<>(dto, headers);
            ResponseEntity<QrDtoResponse> response = restTemplate.exchange(
                    paymentApiUrl + "/v1/payments/qrcode/pix",
                    HttpMethod.POST,
                    request,
                    QrDtoResponse.class
            );

            if (response.getBody() == null) {
                throw new QrCodeException("Empty response from payment service");
            }

            return response.getBody();

        } catch (RestClientException e) {
            throw new QrCodeException("Error generating QR Code: " + e.getMessage(), e);
        }
    }

    public StatusDto getStatus(String paymentId, String authorization) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", authorization);
            headers.set("seller_id", sellerId);

            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<StatusDto> response = restTemplate.exchange(
                    paymentApiUrl + "/v1/payments/qrcode/" + paymentId,
                    HttpMethod.GET,
                    request,
                    StatusDto.class
            );

            if (response.getBody() == null) {
                throw new PaymentStatusException("Empty response from payment service");
            }

            return response.getBody();

        } catch (RestClientException e) {
            throw new PaymentStatusException("Error while checking payment status: " + e.getMessage(), e);
        }
    }

    public PaymentCardDtoResponse paymentCard(PaymentCardDto dto, String authorization) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", authorization);
            headers.set("seller_id", sellerId);

            // Configurando o DTO com o seller_id do ambiente
            PaymentCardDto requestDto = new PaymentCardDto(
                    sellerId,
                    dto.amount(),
                    dto.currency(),
                    dto.order(),
                    dto.customer(),
                    dto.credit()
            );

            HttpEntity<PaymentCardDto> request = new HttpEntity<>(requestDto, headers);

            ResponseEntity<PaymentCardDtoResponse> response = restTemplate.exchange(
                    paymentApiUrl + "/v1/payments/credit",
                    HttpMethod.POST,
                    request,
                    PaymentCardDtoResponse.class
            );

            if (response.getBody() == null) {
                throw new PaymentStatusException("Empty response from payment service");
            }

            PaymentEntity payment = new PaymentEntity();
            payment.setPaymentValue(dto.amount());
            payment.setDate(new Date());
            payment.setMethod("CREDIT_CARD");
            payment.setStatus(response.getBody().status());
            payment.setIdClient(Long.parseLong(dto.customer().customerId()));
            payment.setIdProduct(Long.parseLong(dto.order().orderId()));

            repository.save(payment);

            return response.getBody();

        } catch (RestClientException e) {
            throw new PaymentStatusException("Error processing card payment: " + e.getMessage(), e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", authCredentials);
        return headers;
    }

    private MultiValueMap<String, String> createRequestBody(TokenDto dto) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", dto.grant_type());
        map.add("client_id", dto.client_id());
        map.add("client_secret", dto.client_secret());
        map.add("scope", dto.scope());
        return map;
    }
}
