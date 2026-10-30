package com.proyecto.servicios.model;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record EliminaPersonaRequest(
        String nombre
) implements Serializable {

    public EliminaPersonaRequest {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
    }

    public String getNombre() { return nombre; }
}
