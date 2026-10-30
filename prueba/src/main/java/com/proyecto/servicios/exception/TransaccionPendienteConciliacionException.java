package com.proyecto.servicios.exception;

import lombok.Getter;

@Getter
public class TransaccionPendienteConciliacionException extends GestoPagoTimeoutException {

    private final String folioInterno;

    public TransaccionPendienteConciliacionException(String message, String folioInterno, Throwable cause) {
        super(message, cause);
        this.folioInterno = folioInterno;
    }

    public TransaccionPendienteConciliacionException(String message, String folioInterno) {
        super(message);
        this.folioInterno = folioInterno;
    }
}

