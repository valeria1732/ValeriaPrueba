package com.proyecto.servicios.model;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record PersonasRequest(
        String nombre,
        String apellidoP,
        String apellidoMaterno
) implements Serializable {

    public PersonasRequest {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
    }

    public String getNombre() { return nombre; }
    public String getApellidoP() { return apellidoP; }
    public String getApellidoMaterno() { return apellidoMaterno; }
}
