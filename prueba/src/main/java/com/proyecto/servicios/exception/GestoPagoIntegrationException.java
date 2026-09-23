package com.proyecto.servicios.exception;

import lombok.Getter;

@Getter
public class GestoPagoIntegrationException extends RuntimeException {

    private final Integer codigo;

    public GestoPagoIntegrationException(String message) {
        super(message);
        this.codigo = 500;
    }

    public GestoPagoIntegrationException(String message, Integer codigo) {
        super(message);
        this.codigo = codigo;
    }

    public GestoPagoIntegrationException(String message, Throwable cause) {
        super(message, cause);
        this.codigo = 500;
    }

    public GestoPagoIntegrationException(String message, Integer codigo, Throwable cause) {
        super(message, cause);
        this.codigo = codigo;
    }
}
