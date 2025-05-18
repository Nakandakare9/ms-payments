package br.com.fiap.mspayments.infrastructure.controller;

import br.com.fiap.mspayments.application.dto.*;
import br.com.fiap.mspayments.application.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/payment")

public class PaymentsController {

    private final PaymentService service;

    public PaymentsController(PaymentService service) {
        this.service = service;
    }

    //gerenate token
    @PostMapping( value = "/generateToken", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<TokenDtoResponse> generateToken(@RequestBody TokenDto dto) {
        return new ResponseEntity<>(service.generateToken(dto), HttpStatus.OK);
    }

    //generate QR Pix
	@PostMapping( value = "/generateQR", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QrDtoResponse> generateQR(
            @RequestBody QrDto dto,
            @RequestHeader(value = "Authorization") String authorization) {
        return ResponseEntity.ok(service.generateQR(dto, authorization));
    }

    //status payment
    @GetMapping("/{paymentId}")
    public ResponseEntity<StatusDto> getStatus(
            @PathVariable String paymentId,
            @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(service.getStatus(paymentId, authorization));
    }

    //card payment
    @PostMapping("/card")
    public ResponseEntity<PaymentCardDtoResponse> paymentCard(
            @RequestBody PaymentCardDto dto,
            @RequestHeader("Authorization") String authorization) {
        PaymentCardDtoResponse response = service.paymentCard(dto, authorization);
        return ResponseEntity.ok(response);
    }

}