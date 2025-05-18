package br.com.fiap.mspayments.infrastructure.exception;

public class PaymentStatusException extends RuntimeException {
  public PaymentStatusException(String message) {
    super(message);
  }

  public PaymentStatusException(String message, Throwable cause) {
    super(message, cause);
  }
}
