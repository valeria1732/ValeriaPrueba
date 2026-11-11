package com.proyecto.servicios.model.cliente;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ClienteFiltroRequest(
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String curp,
        String rfc,
        String correo,
        Boolean activo,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
}

