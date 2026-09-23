package com.proyecto.servicios.exception;

public class GestoPagoTimeoutException extends GestoPagoIntegrationException {

    public GestoPagoTimeoutException(String message) {
        super(message, 504);
    }

    public GestoPagoTimeoutException(String message, Throwable cause) {
        super(message, 504, cause);
    }
}
