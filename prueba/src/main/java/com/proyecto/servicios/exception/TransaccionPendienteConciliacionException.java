package com.proyecto.servicios.exception;

import lombok.Getter;

@Getter
public class TransaccionPendienteConciliacionException extends GestoPagoIntegrationException {

    private final String folioInterno;

    public TransaccionPendienteConciliacionException(String message, String folioInterno, Throwable cause) {
        super(message, 504, cause);
        this.folioInterno = folioInterno;
    }

    public TransaccionPendienteConciliacionException(String message, String folioInterno) {
        super(message, 504);
        this.folioInterno = folioInterno;
    }
}

