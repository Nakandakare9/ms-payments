package br.com.fiap.mspayments.infrastructure.exception;

public class QrCodeException extends RuntimeException {
    public QrCodeException(String message) {
        super(message);
    }

    public QrCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
