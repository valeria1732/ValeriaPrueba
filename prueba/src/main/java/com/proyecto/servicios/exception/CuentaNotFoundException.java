package com.proyecto.servicios.exception;

public class CuentaNotFoundException extends RuntimeException {
    public CuentaNotFoundException(String message) {
        super(message);
    }
}
