package com.proyecto.servicios.exception;

public class GestoPagoAuthException extends GestoPagoIntegrationException {

    public GestoPagoAuthException(String message) {
        super(message, 401);
    }

    public GestoPagoAuthException(String message, Throwable cause) {
        super(message, 401, cause);
    }
}
